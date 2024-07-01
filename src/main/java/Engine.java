import annotations.FactAnalyzerAnnotations;
import entry.StrutsAction;
import exceptions.*;
import factAnalyzer.FactAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project.BaseProjectAnalyzer;
import project.entry.Config;
import project.entry.Project;
import project.entry.Projects;
import reporting.ReportGenerator;
import reporting.ReporterFactory;
import soot.SootClass;
import utils.Command;
import utils.CoreClassLoader;
import entry.Settings;
import utils.Utils;
import utils.YamlUtil;
import entry.Fact;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Engine {
    private static final Logger LOGGER = LoggerFactory.getLogger(Engine.class);
    public final ClassLoader FACTANALYZER_CLASSLOADER = new CoreClassLoader(Engine.class.getClassLoader());
    private Command command = new Command();

    private Collection<Fact> factChain = new ArrayList<>();
    private Collection<StrutsAction> actionChain = new ArrayList<>();
    private BaseProjectAnalyzer baseProjectAnalyzer = new BaseProjectAnalyzer();

    private Project project;
    private Projects projects;

    private Settings settings;

    Collection<FactAnalyzer> classFactAnalyzer = new ArrayList<>();
    Collection<FactAnalyzer> configFactAnalyzer = new ArrayList<>();
    Collection<FactAnalyzer> unionFactAnalyzer = new ArrayList<>();
    private Collection<FactAnalyzer> factAnalyzers;


    /**
     * First Step, parse command-line argument
     */
    protected void parseCommand(String[] args) throws Exception {
        command.parse(args);
        Utils.command = command;
    }

    /**
     * Second Step, load yaml settings that contains Analyzer and output information
     */
    protected Settings loadSettings() throws LoadSettingsException {
        try {
            LOGGER.info("Load Settings");
            String settingPath = command.getSettingPath();
            settings = (Settings) YamlUtil.readYaml(settingPath, Settings.class);
            if (command.getOutPut() != null && !command.getOutPut().equals("")) {
                settings.setOutPutDirectory(command.getOutPut());
            }
            return settings;
        } catch (Exception e) {
            throw new LoadSettingsException(e.getMessage());
        }
    }

    /**
     * Third Step, Analyze whether the folder contains multiple projects
     */
    protected void analyzeFolder() throws Exception{
        projects = new Projects();
        LOGGER.info("Analysis Folders");
        baseProjectAnalyzer.initialize(command, settings, project);
        int count = 0;
        List<String> targetFolders = baseProjectAnalyzer.analysisSubModule();
        if( targetFolders != null && !targetFolders.isEmpty()) {
            for (String tf : targetFolders) {
                Project project = new Project();
                project.setName(command.getProjectPath() + File.separator + tf); // 默认只扫源码的第一层目录结构
                projects.addProject(project);
                count = count + 1;
            }
            projects.setProjectCount(count);
        }else {
            Project project =new Project();
            project.setName(command.getProjectPath());
            projects.addProject(project);
            projects.setProjectCount(1);
        }
    }

    /**
     * Fourth Step, Analyze project (Config, Jar, Class)
     */
    protected void analysisProject() throws Exception {
        LOGGER.info("Analysis Project");
        Set<Project> projectSet = projects.getProjects();
        factAnalyzers = loadFactAnalyzer(project);
        initFact();
        for (Project project : projectSet) {
            factChain.clear();
            actionChain.clear();
            baseProjectAnalyzer.initialize(command, settings, project);
            baseProjectAnalyzer.analysis(project);
            evaluateFact(project);
            List<Fact> newFactChain = new ArrayList<>();
            newFactChain.addAll(factChain);
            projects.addFactChain(project, (List<Fact>) newFactChain);
            List<StrutsAction> newActionChain = new ArrayList<>();
            newActionChain.addAll(actionChain);
            projects.addActionChain(project,newActionChain);
        }
    }

    // TODO：这个其实也要改，不需要和project绑定，因为多项目结构，每个project实际都要用这个处理，相当于跑了N遍。应该设置成全局的
    protected Collection<FactAnalyzer> loadFactAnalyzer(Project project) throws LoadFactAnalyzerException {
        Collection<FactAnalyzer> factAnalyzerCollection = new ArrayList<>();
        try {
            List<String> analyzers = settings.getFactAnalyzers().get("default"); // 原来是.get(project.getService())
            // 获取factAnalyzer文件夹下的Analyzer解析器
            Map<String, Class> factAnalyzerNameToClass = scanFactAnalyzer();
            for (String analyzer : analyzers) {
                Class clazz = factAnalyzerNameToClass.get(analyzer);
                if (clazz != null) {
                    try {
                        FactAnalyzer factAnalyzer = (FactAnalyzer) clazz.newInstance();
                        factAnalyzer.initialize(project, settings);
                        factAnalyzerCollection.add(factAnalyzer);
                    } catch (Exception e) {
                        LOGGER.debug(e.getMessage());
                    }
                }
            }
            LOGGER.info(String.format("Load FactAnalyzers(%s)", analyzers.size()));
        } catch (Exception e) {
            throw new LoadFactAnalyzerException(e.getMessage());
        }
        return factAnalyzerCollection;
    }

    private Map<String, Class> scanFactAnalyzer() throws Exception {
        Map<String, Class> factAnalyzerNameToClass = new HashMap<>();
        try {
            URL url = Engine.class.getResource("/factAnalyzer/");
            ArrayList<Class> destList = new ArrayList<>();
            scanClass(url.toURI(), "factAnalyzer", FactAnalyzer.class, FactAnalyzerAnnotations.class, destList);
            destList.forEach(clazz -> { // 扫描Analyzer列表
                String clazzName = clazz.getSimpleName();
                factAnalyzerNameToClass.put(clazzName, clazz);
            });
            return factAnalyzerNameToClass;
        } catch (Exception e) {
            throw e;
        }
    }

    private void scanClass(URI uri, String packageName, Class<?> parentClass, Class<?> annotationClass, ArrayList<Class> destList) throws IOException, ClassNotFoundException {
        try {
            String jarFileString;
            // 项目生成的RouteCheck.jar文件，找到factAnalyzer文件夹路径，扫描FactAnalyzer所有的子类，生成子类列表destList
            if ((jarFileString = Utils.getJarFileByClass(Engine.class)) != null) {
                scanClassByJar(new File(jarFileString), packageName, parentClass, annotationClass, destList);
            } else {
                File file = new File(uri);
                File[] file2 = file.listFiles();
                for (int i = 0; i < file2.length; i++) {
                    File objectClassFile = file2[i];
                    if (objectClassFile.getPath().endsWith(".class"))
                        try {
                            String objectClassName = String.format("%s.%s", new Object[]{packageName, objectClassFile.getName().substring(0, objectClassFile.getName().length() - ".class".length())});
                            Class<?> objectClass = Class.forName(objectClassName, true, FACTANALYZER_CLASSLOADER);
                            if (parentClass.isAssignableFrom(objectClass) && objectClass.isAnnotationPresent((Class) annotationClass)) {
                                destList.add(objectClass);
                            }
                        } catch (Exception e) {
                            LOGGER.debug(String.format("When scan class %s occur error: %", new Object[]{objectClassFile, e.getMessage()}));
                        }
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private void scanClassByJar(File srcJarFile, String packageName, Class<?> parentClass, Class<?> annotationClass, ArrayList<Class> destList) throws IOException, ClassNotFoundException {
        try {
            JarFile jarFile = new JarFile(srcJarFile);
            Enumeration<JarEntry> jarFiles = jarFile.entries();
            packageName = packageName.replace(".", "/");
            while (jarFiles.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) jarFiles.nextElement();
                String name = jarEntry.getName();
                if (name.startsWith(packageName) && name.endsWith(".class")) {
                    name = name.replace("/", ".");
                    name = name.substring(0, name.length() - 6);
                    Class objectClass = Class.forName(name, true, FACTANALYZER_CLASSLOADER);
                    try {
                        if (parentClass.isAssignableFrom(objectClass) && objectClass.isAnnotationPresent(annotationClass)) {
                            destList.add(objectClass);
                        }
                    } catch (Exception e) {
                        LOGGER.debug(String.format("When scan class %s occur error: %", new Object[]{objectClass, e.getMessage()}));
                    }
                }
            }
            jarFile.close();
        } catch (Exception ex) {
            throw ex;
        }
    }


    protected void initFact() throws Exception{
        for (FactAnalyzer factAnalyzer : factAnalyzers) {
            if (factAnalyzer.getType().toLowerCase(Locale.ROOT).equals("class")) {
                classFactAnalyzer.add(factAnalyzer);
            } else if (factAnalyzer.getType().toLowerCase(Locale.ROOT).equals("config")) {
                configFactAnalyzer.add(factAnalyzer);
            } else if (factAnalyzer.getType().toLowerCase(Locale.ROOT).equals("union")) {
                unionFactAnalyzer.add(factAnalyzer);
            }
        }
    }

    /**
     * Fifth Step, Evaluate Fact
     */
    protected void evaluateFact(Project project) throws FactAnalyzerException {
        LOGGER.info("Evaluate Fact");
        // 单一结构改成多项目结构
        Set<SootClass> sootClassSet = project.getClasses();
        Collection<Config> configs = project.getConfigs();
        try {
            for (Config config : configs) {
                for (FactAnalyzer fa : configFactAnalyzer) {
                    try {
                        fa.prepare(config);
                        if (fa.isEnable()) {
                            LOGGER.info("Config FactAnalyzer");
                            fa.analysis(config, factChain);
                            LOGGER.info(config.getFileName() + ": " + fa.getName() + " Done");
                        }
                    } catch (Exception e) {
                        LOGGER.debug(config.getFileName() + ": " + fa.getName() + "occur error: " + e.getMessage());
                    }
                }
            }

            for (SootClass sootClass : sootClassSet) {
                for (FactAnalyzer fa : classFactAnalyzer) {
                    try {
                        fa.prepare(sootClass);
                        if (fa.isEnable()) {
                            LOGGER.info("Class FactAnalyzer");
                            if (fa.getName().equals("factAnalyzer.StrutsActionFactAnalyzer")){
                                fa.analysis(sootClass, factChain, actionChain);
                            }else {
                                fa.analysis(sootClass,factChain);
                            }

                            LOGGER.info(sootClass.getName() + ": " + fa.getName() + " Done");
                        }
                    } catch (Exception e) {
                        LOGGER.debug(sootClass.getName() + ": " +
                                fa.getName() + "occur error: " + e.getMessage());
                    }
                }

            }

            // TODO: 这部分有待分析
            for (FactAnalyzer ufa : unionFactAnalyzer) {
                try {
                    LOGGER.info("Union FactAnalyzer");
                    ufa.analysis(null, factChain); // 如果此时的factChain为空，analysis会抛出异常java.lang.NullPointerException
                } catch (Exception ex) {
                    ex.printStackTrace();
                    LOGGER.error(String.format("When execute %s occur error", ufa.getName()));
                }
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Seventh Step, Scan jsp Files
     */
    protected void scanJsp() throws ReportingException {
        LOGGER.info("Starting Scan Jsp");
        String ProjectPath = command.getProjectPath();
        List<String> jspFiles = findJSPFiles(ProjectPath, ProjectPath);
        projects.setJSPPaths(jspFiles);
        LOGGER.info("Scan Jsp Done");
    }

    public static List<String> findJSPFiles(String folderPath,String rootFolderPath) {
        List<String> jspFiles = new ArrayList<>();
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".jsp")) {
                    // 计算相对路径
                    String relativePath = file.getAbsolutePath().substring(rootFolderPath.length());
                    jspFiles.add(relativePath);
                } else if (file.isDirectory()) {
                    jspFiles.addAll(findJSPFiles(file.getAbsolutePath(), rootFolderPath));
                }
            }
        }

        return jspFiles;
    }

    /**
     * Eighth Step, Write Report
     */
    protected void writeReport() throws ReportingException {
        LOGGER.info("Starting Write Report");
        String type = settings.getReportType();
        Map<String, ReportGenerator> reportGeneratorMap = ReporterFactory.getReportGenerator(type);
        ReportGenerator reportGenerator = null;
        if (!type.equals("all")){
            reportGenerator = reportGeneratorMap.get(type);
            reportGenerator.initialize(projects,settings);
            reportGenerator.write();
        }else {
            for (Map.Entry<String, ReportGenerator> entry : reportGeneratorMap.entrySet()) {
                ReportGenerator value = entry.getValue();
                value.initialize(projects,settings);
                value.write();
            }

        }

        LOGGER.info("Write Report Done");
    }

    protected void run(String[] args) {
        System.out.println(
                ".______        ______    __    __  .___________. _______   ______  __    __   _______   ______  __  ___ \n" +
                        "|   _  \\      /  __  \\  |  |  |  | |           ||   ____| /      ||  |  |  | |   ____| /      ||  |/  / \n" +
                        "|  |_)  |    |  |  |  | |  |  |  | `---|  |----`|  |__   |  ,----'|  |__|  | |  |__   |  ,----'|  '  /  \n" +
                        "|      /     |  |  |  | |  |  |  |     |  |     |   __|  |  |     |   __   | |   __|  |  |     |    <   \n" +
                        "|  |\\  \\----.|  `--'  | |  `--'  |     |  |     |  |____ |  `----.|  |  |  | |  |____ |  `----.|  .  \\  \n" +
                        "| _| `._____| \\______/   \\______/      |__|     |_______| \\______||__|  |__| |_______| \\______||__|\\__\\ \n" +
                        "                                                                                                        ");
        final long analysisStart = System.currentTimeMillis();
        try {
            parseCommand(args);
            loadSettings();
            analyzeFolder();
            analysisProject();
//            factAnalyzers = loadFactAnalyzer();
//            evaluateFact();
            scanJsp();
            writeReport();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Analysis occur error: " + e.getMessage());
        }
        LOGGER.info("\n----------------------------------------------------\nEND ANALYSIS\n----------------------------------------------------");
        final long analysisDurationSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - analysisStart);
        LOGGER.info("Analysis Complete ({} seconds)", analysisDurationSeconds);
    }

    public static void main(String[] args) {
        Engine engine = new Engine();
        engine.run(args);
    }
}

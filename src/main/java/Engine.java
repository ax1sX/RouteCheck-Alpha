import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import entry.Settings;
import entry.StrutsAction;
import exceptions.LoadFactAnalyzerException;
import exceptions.LoadSettingsException;
import exceptions.ReportingException;
import factAnalyzer.FactAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project.BaseProjectAnalyzer;
import project.ModuleAnalyzer;
import project.entry.Config;
import project.entry.Module;
import project.entry.Project;
import reporting.ReportGenerator;
import reporting.ReporterFactory;
import soot.SootClass;
import utils.Command;
import utils.Utils;
import utils.YamlUtil;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Engine {
    private static final Logger LOGGER = LoggerFactory.getLogger(Engine.class);
    private Command command = new Command();
    private List<FactAnalyzer> classFactAnalyzer = new ArrayList<>();
    private List<FactAnalyzer> configFactAnalyzer = new ArrayList<>();
    private List<FactAnalyzer> unionFactAnalyzer = new ArrayList<>();
    private List<FactAnalyzer> factAnalyzers = new ArrayList<>();

    private BaseProjectAnalyzer baseProjectAnalyzer = null;
    private Project project;

    private Settings settings;

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
    protected void analyzeProject() throws Exception{
        project = new Project(this.command.getProjectPath());
        LOGGER.info("Analyze Project");
        baseProjectAnalyzer = new BaseProjectAnalyzer(project);
        baseProjectAnalyzer.analysis();
    }

    /**
     * Fourth Step, Analyze project (Config, Jar, Class)
     */
    protected void analyzeModules() throws Exception {
        List<Module> modules = project.getAllModule();
        for (Module module : modules) {
            LOGGER.info("Analyze Module: " + module.getName());
            new ModuleAnalyzer(module).analysis();
            List<Fact> factChain = new ArrayList<>();
            List<StrutsAction> actionChain = new ArrayList<>();
            evaluateFact(module, factAnalyzers, factChain, actionChain);
            project.addFactChainByModule(module, factChain);
            project.addActionChainByModule(module, actionChain);
        }
    }

    protected List<FactAnalyzer> loadFactAnalyzer() throws LoadFactAnalyzerException {
        List<FactAnalyzer> factAnalyzerCollection = new ArrayList<>();
        try {
            Map<String, Class> factAnalyzerNameToClass = scanFactAnalyzer();
            for (Class clazz : factAnalyzerNameToClass.values()) {
                if (clazz != null) {
                    try {
                        FactAnalyzer factAnalyzer = (FactAnalyzer) clazz.newInstance();
                        factAnalyzer.initialize(null, settings);
                        factAnalyzerCollection.add(factAnalyzer);
                    } catch (Exception e) {
                        LOGGER.debug(e.getMessage());
                    }
                }
            }
            LOGGER.info(String.format("Load FactAnalyzers(%s)", factAnalyzerCollection.size()));
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
            Utils.scanClass(url.toURI(), "factAnalyzer", FactAnalyzer.class, FactAnalyzerAnnotations.class, destList);
            destList.forEach(clazz -> { // 扫描Analyzer列表
                String clazzName = clazz.getSimpleName();
                factAnalyzerNameToClass.put(clazzName, clazz);
            });
            return factAnalyzerNameToClass;
        } catch (Exception e) {
            throw e;
        }
    }

    protected void initFact(List<FactAnalyzer> factAnalyzers, List<FactAnalyzer> classFactAnalyzer, List<FactAnalyzer> configFactAnalyzer, List<FactAnalyzer> unionFactAnalyzer) throws Exception{
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

    protected void resetFactOfModule(Module module, List<FactAnalyzer> factAnalyzers){
        for (FactAnalyzer factAnalyzer:
             factAnalyzers) {
            factAnalyzer.resetModule(module);
        }

    }

    /**
     * Fifth Step, Evaluate Fact
     */
    protected void evaluateFact(Module module,List<FactAnalyzer> factAnalyzers,  List<Fact> factChain, List<StrutsAction> actionChain) throws Exception {
        LOGGER.info("Evaluate Fact");
        List<SootClass> sootClassList = module.getAllSootClass();
        Collection<Config> configs = module.getConfigs();
        resetFactOfModule(module, factAnalyzers);  // 重置事实分析器的module
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

            for (SootClass sootClass : sootClassList) {
                for (FactAnalyzer fa : classFactAnalyzer) {
                    try {
                        fa.prepare(sootClass);
                        if (fa.isEnable()) {
                            LOGGER.info("Class FactAnalyzer");
                            fa.analysis(sootClass, factChain, actionChain);
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
        project.setJSPPaths(jspFiles);
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
            reportGenerator.initialize(project,settings);
            reportGenerator.write();
        }else {
            for (Map.Entry<String, ReportGenerator> entry : reportGeneratorMap.entrySet()) {
                ReportGenerator value = entry.getValue();
                value.initialize(project,settings);
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
            factAnalyzers = loadFactAnalyzer();
            initFact(factAnalyzers, classFactAnalyzer, configFactAnalyzer, unionFactAnalyzer); // 将事实分析器分类
            analyzeProject();
            analyzeModules();
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

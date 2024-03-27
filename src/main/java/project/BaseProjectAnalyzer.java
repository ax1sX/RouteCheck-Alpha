package project;

import exceptions.ProjectAnalyzerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project.entry.Config;
import project.entry.Jar;
import project.entry.Project;
import project.entry.Projects;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;
import entry.Settings;
import utils.Command;

import java.io.File;
import java.util.*;

import static soot.SootClass.SIGNATURES;

public class BaseProjectAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseProjectAnalyzer.class);

    private final List<String> CONFIG_SUFFIXES = Arrays.asList("xml", "yaml", "wsdl", "wsdd");

    private final String JRE_DIR = System.getProperty("java.home")+ File.separator+
            "lib" + File.separator + "rt.jar";

    static LinkedList<String> excludeList;
    private List<String> libs = new ArrayList<>();
    private List<String> classFilePaths = new ArrayList<>();
    private List<String> jarFilePaths = new ArrayList<>();
    private List<String> targetFolders = new ArrayList<>(); // 源码的项目是多目标项目还是单目标项目

    private Project project;

    private Settings settings;
    private Command command;
    private int tempClassPathLength;

    /**
     * 扫描项目有哪些文件，分为Config配置文件、jar文件、Class文件，然后将Class文件放入Soot
     */

    public void analysis(Project project) throws ProjectAnalyzerException {
        String projectPath = project.getName();
        scanConfig(new File(projectPath));
        // 在scanJars之前要先把jarFilePaths清空，不然会把每个子项目的lib都添加进去
        jarFilePaths.clear();
        libs.clear();
        scanJars(new File(projectPath));

        // analysisClasses应该也只对子项目对扫描
        analysisClasses(projectPath);
    }

    private void analysisClasses(String classPath){
        List<String> classPaths = new ArrayList<>();
        classPaths.add(classPath+"/WEB-INF/classes/");
        libs.addAll(jarFilePaths);
        libs.add(JRE_DIR);
        excludeJDKLibrary();
        String sootClassPath = String.join(File.pathSeparator, classPaths) + File.pathSeparator +
                String.join(File.pathSeparator, libs);
        // TODO: 执行loadNecessaryClasses时某类会报错 This operation requires resolving level SIGNATURES
        //  but xx类 is at resolving level HIERARCHY. 暂时用ignore_resolving_levels解决
        try{
            Options.v().set_whole_program(true);
            Options.v().set_app(true);
            Options.v().set_allow_phantom_refs(true);
            Options.v().set_process_dir(classPaths);
            Options.v().set_include_all(true); // 包含所有的类
            Options.v().set_ignore_resolving_levels(true);
            classFilePaths.clear(); //清空classFilePath，不然scanClass会把之前子项目的也计算一遍
//            scanClass(new File(classPath));
            scanClass(new File(classPath+"/WEB-INF/classes/"));
            Scene.v().setPhantomRefs(true);
            Scene.v().setSootClassPath(sootClassPath);
            Scene.v().loadNecessaryClasses();
//            buildSootClass();
        }catch (Exception e){
            LOGGER.info(e.getMessage());
        }
        try{
            String clp;
            if (!classFilePaths.isEmpty()){
                clp = classFilePaths.get(0);
                int startIndex = clp.indexOf("/WEB-INF/classes/");
                if (startIndex != -1) {
                    int length = "/WEB-INF/classes".length();
//                    String extractedPath = clp.substring(startIndex + "/WEB-INF/classes/".length());
                    this.tempClassPathLength = startIndex + length;
                }else{
                    this.tempClassPathLength = clp.length();
                }
            }else {
                this.tempClassPathLength = project.getName().length();
            }
            buildSootClass();
        }catch (Exception e){
            LOGGER.info(e.getMessage());
        }
    }

    public void initialize(Command command, Settings settings, Project project){
        this.command = command;
        this.settings = settings;
        this.project = project;
    }

    private void scanConfig(File file){
        if (file.isFile()) {
            String fileName = file.getName();
            if(fileName.lastIndexOf(".") == -1)return;
            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
            if(CONFIG_SUFFIXES.contains(suffix)) {
                String filePath = file.getAbsolutePath();
                Config config = new Config(fileName, filePath, suffix);
                project.addConfig(config);
                project.setConfigMap(filePath, config);
            }
        }
        else if (file.isDirectory()) {
            for (File f : file.listFiles())
                scanConfig(f);
        }
    }

    private void scanClass(File file){
        if (file.isFile()) {
            String fileName = file.getName();
            if(fileName.endsWith(".class")) {
                String filePath = file.getAbsolutePath();
                classFilePaths.add(filePath);
            }
        }
        else if (file.isDirectory()) {
            for (File f : file.listFiles())
                scanClass(f);
        }
    }

    private void scanJars(File file){
        if (file.isFile()) {
            String fileName = file.getName();
            if(fileName.endsWith(".jar")) {
                String filePath = file.getAbsolutePath();
                Jar jar = new Jar(fileName, filePath);
                String newFileName = fileName.split("-\\d+(.\\d+)*")[0];
                project.addJar(jar);
                project.setJarMap(newFileName, jar);
                jarFilePaths.add(filePath);
            }
        }
        else if (file.isDirectory()) {
            for (File f : file.listFiles())
                scanJars(f);
        }
    }

    /**
     * 这段代码要改，tempClassPathLength不通用，通过它截取出类的package name，一旦截取的和实际package name不同，就无法解析类文件
     */
    private void buildSootClass(){
        Set<SootClass> sootClassSet = new HashSet<>();
        for (String classFilePath: classFilePaths) {
            String path = classFilePath.substring(this.tempClassPathLength);
            // 如果path以/开头，substring(1,x)。如果是字母开头，substring(0)。这部分写的有点死
            String newPath = path.substring(1, path.lastIndexOf("."));
            newPath = newPath.replace(File.separator, ".");
            Scene.v().addBasicClass(newPath,SIGNATURES);
            SootClass sootClass = Scene.v().loadClassAndSupport(newPath);
            if(!sootClass.isJavaLibraryClass()){
                sootClassSet.add(sootClass);
                project.setClassesToPath(sootClass, classFilePath);
            }
        }
        project.setClasses(sootClassSet);

    }

    private static LinkedList<String> excludeList()
    {
        if(excludeList==null)
        {
            excludeList = new LinkedList<String> ();

            excludeList.add("java.*");
            excludeList.add("javax.*");
            excludeList.add("sun.*");
            excludeList.add("sunw.*");
            excludeList.add("com.sun.*");
            excludeList.add("com.ibm.*");
            excludeList.add("com.apple.*");
            excludeList.add("apple.awt.*");
            excludeList.add("jdk.internal.*");
        }
        return excludeList;
    }

    private static void excludeJDKLibrary()
    {
        Options.v().set_exclude(excludeList());
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
    }

    public Project getProject() {
        return project;
    }

    public List<String> getTargetFolders() {
        return targetFolders;
    }

    /**
     * 扫描源码层次结构
     */

    // 新增，扫描子文件夹
    public List<String> analysisSubModule(){
        String projectPath = command.getProjectPath();
        File rootFolder = new File(projectPath);

        List<String> subFolderNames = ScanFirstSubFolderNames(rootFolder);
        if (subFolderNames != null){
            for (String folderName : subFolderNames) {
                boolean containsWebXml = containsWebXmlInWebInf(new File(projectPath+"/"+folderName));
                if (containsWebXml){
                    targetFolders.add(folderName);
                }
            }
        }
        return targetFolders;
    }

    // 扫描源码的第一层文件夹目录，如果第一层目录中就有WEB-INF目录，说明是单项目结构
    public static List<String> ScanFirstSubFolderNames(File folder) {
        List<String> subFolderNames = new ArrayList<>();

        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        if (file.getName().equalsIgnoreCase("WEB-INF")){
                            return null;
                        }
                        subFolderNames.add(file.getName());
                    }
                }
            }
        }

        return subFolderNames;
    }

    // 判断子文件夹中是否包含/WEB-INF/文件夹和web.xml文件
    public static boolean containsWebXmlInWebInf(File folder) {
        if (!folder.isDirectory()) {
            return false;
        }

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && file.getName().equalsIgnoreCase("WEB-INF")) {
                    // 如果包含名为 WEB-INF 的文件夹，继续判断其中是否包含 web.xml 文件
                    return containsWebXml(file);
                }
            }
        }

        // 没有找到名为 WEB-INF 的文件夹，或者找到了但是其中不包含 web.xml 文件
        return false;
    }

    public static boolean containsWebXml(File webInfFolder) {
        File[] files = webInfFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().equalsIgnoreCase("web.xml")) {
                    // 如果 WEB-INF 文件夹下包含名为 web.xml 的文件，返回 true
                    return true;
                }
            }
        }

        // 没有找到名为 web.xml 的文件
        return false;
    }
}

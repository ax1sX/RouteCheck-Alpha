package project;

import exceptions.ModuleAnalyzerExecption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project.entry.Config;
import project.entry.Jar;
import project.entry.Module;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;
import utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static soot.SootClass.SIGNATURES;

public class ModuleAnalyzer {

    private Module module;
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleAnalyzer.class);

    private final List<String> CONFIG_SUFFIXES = Arrays.asList("xml", "yaml", "wsdl", "wsdd");

    private final String JRE_DIR = System.getProperty("java.home")+ File.separator+
            "lib" + File.separator + "rt.jar";

    static LinkedList<String> excludeList;

    public ModuleAnalyzer(Module module) {
        this.module = module;
    }

    public void analysis() throws ModuleAnalyzerExecption {
        String modulePath = this.module.getName();
        File moduleFile = new File(modulePath);
        scanConfig(moduleFile);
        scanJars(moduleFile);
        loadSootClass();
    }

    private void loadSootClass(){
        List<String> libs = new ArrayList<>();
        for (Jar jar:
             this.module.getJars()) {
            libs.add(jar.getFilePath());
        }
        libs.add(JRE_DIR);
        excludeJDKLibrary();

        List<String> classDirs = new ArrayList<>();
        classDirs.add( this.module.getClassPath());
        String sootClassPath = String.join(File.pathSeparator, classDirs) + File.pathSeparator +
                String.join(File.pathSeparator, libs);
        // TODO: 执行loadNecessaryClasses时某类会报错 This operation requires resolving level SIGNATURES
        //  but xx类 is at resolving level HIERARCHY. 暂时用ignore_resolving_levels解决
        try{
            Options.v().set_whole_program(true);
            Options.v().set_app(true);
            Options.v().set_allow_phantom_refs(true);
            Options.v().set_process_dir(classDirs);
            Options.v().set_include_all(false); // 包含所有的类
            Options.v().set_ignore_resolving_levels(true);
            Scene.v().setPhantomRefs(true);
            Scene.v().setSootClassPath(sootClassPath);
            Scene.v().loadNecessaryClasses();
            buildSootClass();
        }catch (Exception e){
            LOGGER.info(e.getMessage());
        }
    }


    private void scanConfig(File file){
        if (file.isFile()) {
            String fileName = file.getName();
            if(fileName.lastIndexOf(".") == -1)return;
            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
            if(CONFIG_SUFFIXES.contains(suffix)) {
                String filePath = file.getAbsolutePath();
                Config config = new Config(fileName, filePath, suffix);
                module.addConfig(config);
                module.setConfigMap(filePath, config);
            }
        }
        else if (file.isDirectory()) {
            for (File f : file.listFiles())
                scanConfig(f);
        }
    }


    private void scanJars(File file){
        if (file.isFile()) {
            String fileName = file.getName();
            if(fileName.endsWith(".jar")) {
                String filePath = file.getAbsolutePath();
                Jar jar = new Jar(fileName, filePath);
                String newFileName = fileName.split("-\\d+(.\\d+)*")[0];
                module.addJar(jar);
                module.setJarMap(newFileName, jar);
            }
        }
        else if (file.isDirectory()) {
            for (File f : file.listFiles())
                scanJars(f);
        }
    }


    private void buildSootClass(){
        for (String classFilePath: this.module.getAllClass()) {
            String fullyQualifiedName = Utils.getFullyQualifiedName(classFilePath);
            fullyQualifiedName = fullyQualifiedName.replace(File.separator, ".");
            Scene.v().addBasicClass(fullyQualifiedName,SIGNATURES);
            SootClass sootClass = Scene.v().loadClassAndSupport(fullyQualifiedName);
            if(!sootClass.isJavaLibraryClass()){
                module.addSootClassAndPath(sootClass, classFilePath);
            }
        }
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
}

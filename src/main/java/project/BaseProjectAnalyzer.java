package project;

import exceptions.ProjectAnalyzerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project.entry.Config;
import project.entry.Jar;
import project.entry.Module;
import project.entry.Project;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;
import entry.Settings;
import utils.Command;
import utils.Utils;

import java.io.File;
import java.util.*;

import static soot.SootClass.SIGNATURES;

public class BaseProjectAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseProjectAnalyzer.class);
    private Project project;

    private Settings settings;
    private Command command;

    /**
     * 扫描项目有哪些文件，分为Config配置文件、jar文件、Class文件，然后将Class文件放入Soot
     */

    public BaseProjectAnalyzer(Project project){
        this.project = project;
    }

    public void analysis() throws ProjectAnalyzerException {
        String projectPath = this.project.getName();
        scanModuleAndClasses(new File(projectPath));
        // TODO: 如果没有扫到module，大致说明当前项目中没有class文件，
        //  业务代码打包在jar中，后续陆续支持对jar的解析
        if (this.project.getModuleCount() == 0){
            Module module = new Module(projectPath);
            this.project.addModule(module);
        }
    }


    private void scanModuleAndClasses(File file){
        if (file.isFile()) {
            String fileName = file.getName();
            if(fileName.endsWith(".class")) {
                String filePath = file.getAbsolutePath();
                String classDir = Utils.getClassDir(filePath);
                String modulePath = Utils.getModulePath(classDir);
                Module module = this.project.getModuleByName(modulePath);
                if (module == null){
                    module = new Module(classDir);
                    this.project.addModule(module);
                }else{
                    module.addClass(filePath);
                }
            }
        }
        else if (file.isDirectory()) {
            for (File f : file.listFiles())
                scanModuleAndClasses(f);
        }
    }
}
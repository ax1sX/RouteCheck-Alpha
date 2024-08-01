package utils;


import org.apache.commons.cli.*;

import java.io.File;
import java.io.InputStream;
import java.net.URLDecoder;


public class Command {
    private String settingPath;

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getPackagePrefix() {
        return packagePrefix;
    }

    public void setPackagePrefix(String packagePrefix) {
        this.packagePrefix = packagePrefix;
    }

    private String packagePrefix;
    private String projectPath;

    public String getSettingPath() {
        return settingPath;
    }

    public void setSettingPath(String settingPath) {
        this.settingPath = settingPath;
    }

    public String getOutPut() {
        return outPut;
    }

    public void setOutPut(String outPut) {
        this.outPut = outPut;
    }

    private String outPut;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    private String projectName;

    public Command() {
    }

    public void parse(String[] args) throws Exception {
        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        options.addOption("h", "help", false, "打印命令行帮助信息");
        options.addOption("pp", "project-path", true, "项目路径"); // 必选项，项目根路径
        options.addOption("pf", "package-prefix", false, "项目的包名特征前缀"); // 必选项，项目根路径

        CommandLine commandLine = parser.parse(options, args);
        HelpFormatter helpFormatter = new HelpFormatter();
        if (commandLine.hasOption("h")) {
            helpFormatter.printHelp("java -jar RouteCheck.jar", options, true);
            System.exit(0);
        }

        try{
            ClassLoader classLoader = Command.class.getClassLoader();
            this.setSettingPath(URLDecoder.decode(classLoader.getResource("settings.yaml").getPath(), "UTF-8"));
        }catch (Exception e) {
            throw new Exception(e.getMessage());
        }


        if (commandLine.hasOption("pp")) { // 源码的根目录
            this.setProjectPath(commandLine.getOptionValue("pp"));
        }

        if (commandLine.hasOption("pf")) {
            this.setPackagePrefix(commandLine.getOptionValue("pf"));
        }

        String ProjectPath = this.getProjectPath();
        int lastIndex = ProjectPath.lastIndexOf(File.separator);
        String lastPart = ProjectPath.substring(lastIndex + 1);
        this.setProjectName(lastPart);



        this.setOutPut("output");

    }
}

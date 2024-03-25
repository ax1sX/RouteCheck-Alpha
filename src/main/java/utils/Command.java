package utils;


import org.apache.commons.cli.*;

import java.io.InputStream;


public class Command {
    private String classPath;
    private String libPath;
    private String settingPath;

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    private String projectPath;

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String getLibPath() {
        return libPath;
    }

    public void setLibPath(String libPath) {
        this.libPath = libPath;
    }

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
        options.addOption("pn", "project-name", false, "项目名称");
        options.addOption("pp", "project-path", true, "项目路径"); // 必选项，项目根路径
        options.addOption("cp", "class-path", false, "类文件地址");
        options.addOption("lp", "lib-path", false, "库文件地址");
        options.addOption("sp", "setting-path", false, "设置配置文件地址");
        options.addOption("o", "outPut", false, "结果保存目录"); // 改为false，和settings保持一致

        CommandLine commandLine = parser.parse(options, args);
        HelpFormatter helpFormatter = new HelpFormatter();
        if (commandLine.hasOption("h")) {
            helpFormatter.printHelp("java -jar RouteCheck.jar", options, true);
            System.exit(0);
        }

        if (commandLine.hasOption("cp")) {
            this.setClassPath(commandLine.getOptionValue("cp"));
        }

        if (commandLine.hasOption("lp")) {
            this.setLibPath(commandLine.getOptionValue("lp"));
        }

        if (commandLine.hasOption("sp")) { // 如果没有设置settings.yaml文件，默认从resources文件夹下读取
            this.setSettingPath(commandLine.getOptionValue("sp"));
        }else {
            try{
                ClassLoader classLoader = Command.class.getClassLoader();
                this.setSettingPath(classLoader.getResource("settings.yaml").getPath());
            }catch (Exception e) {
                throw new Exception(e.getMessage());
            }
        }

        if (commandLine.hasOption("pp")) { // 源码的根目录
            this.setProjectPath(commandLine.getOptionValue("pp"));
        }

        if (commandLine.hasOption("pn")) { // 如果没有设置pn项目名称，提取源码根目录最后一个 "/" 后的内容为项目名称
            this.setProjectName(commandLine.getOptionValue("pn"));
        }else {
            String ProjectPath = this.getProjectPath();
            int lastIndex = ProjectPath.lastIndexOf("/");
            String lastPart = ProjectPath.substring(lastIndex + 1);
            this.setProjectName(lastPart);
        }

        if (commandLine.hasOption("o")) { // 如果没有设置output文件夹，默认为settings.yaml文件中的outPutDirectory路径
            this.setOutPut(commandLine.getOptionValue("o"));
        }else {
            this.setOutPut(null);
        }
    }
}

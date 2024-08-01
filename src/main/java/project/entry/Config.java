package project.entry;

public class Config {
    private String fileName;
    private String filePath;
    private String suffix;
    private boolean is_jar;

    public Config(String fileName, String filePath, String suffix) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.suffix = suffix;
        this.is_jar = false;
    }

    public Config(String fileName, String filePath, String suffix, boolean is_jar) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.suffix = suffix;
        this.is_jar = is_jar;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public boolean is_jar() {
        return is_jar;
    }

}

package project.entry;

public class Config {
    private String fileName;
    private String filePath;
    private String suffix;

    public Config(String fileName, String filePath, String suffix) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.suffix = suffix;
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
}

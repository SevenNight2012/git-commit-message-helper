package com.fulinlin.model;

public class FileChange {
    private String filePath;
    private ChangeType changeType;
    private String diffContent;

    public FileChange() {}

    public FileChange(String filePath, ChangeType changeType, String diffContent) {
        this.filePath = filePath;
        this.changeType = changeType;
        this.diffContent = diffContent;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public String getDiffContent() {
        return diffContent;
    }

    public void setDiffContent(String diffContent) {
        this.diffContent = diffContent;
    }
}
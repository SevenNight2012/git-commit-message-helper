package com.fulinlin.model;

import java.util.List;

public class CodeChangeInfo {
    private List<FileChange> changedFiles;
    private String diffContent;
    private List<String> relatedIssues;

    public List<FileChange> getChangedFiles() {
        return changedFiles;
    }

    public void setChangedFiles(List<FileChange> changedFiles) {
        this.changedFiles = changedFiles;
    }

    public String getDiffContent() {
        return diffContent;
    }

    public void setDiffContent(String diffContent) {
        this.diffContent = diffContent;
    }

    public List<String> getRelatedIssues() {
        return relatedIssues;
    }

    public void setRelatedIssues(List<String> relatedIssues) {
        this.relatedIssues = relatedIssues;
    }
}
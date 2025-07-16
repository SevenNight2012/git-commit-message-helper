package com.fulinlin.model;

import java.util.List;

public class CodeChangeInfo {
    private List<FileChange> changedFiles;
    private String diffContent;
    private ChangeType changeType;
    private String scope;
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

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<String> getRelatedIssues() {
        return relatedIssues;
    }

    public void setRelatedIssues(List<String> relatedIssues) {
        this.relatedIssues = relatedIssues;
    }
}
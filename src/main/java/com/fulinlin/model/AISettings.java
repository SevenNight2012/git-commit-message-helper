package com.fulinlin.model;

public class AISettings {
    private String apiKey;
    private String apiEndpoint = "https://api.deepseek.com/v1/chat/completions";
    private String model = "deepseek-chat";
    private int maxTokens = 500;
    private double temperature = 0.7;
    private boolean enabled = false;
    private boolean autoGenerate = false;
    private String promptTemplate = "conventional_zh"; // 默认使用中文提示语
    private String fileBlacklist = ""; // 文件黑名单，每行一个正则表达式规则
    private String textFileExtensions = ".java,.kt,.xml,.groovy,.md,.txt,.properties"; // 支持的文本文件扩展名，逗号分隔
    private int diffContextSize = 3; // diff上下文尺寸，默认3

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAutoGenerate() {
        return autoGenerate;
    }

    public void setAutoGenerate(boolean autoGenerate) {
        this.autoGenerate = autoGenerate;
    }

    public String getPromptTemplate() {
        return promptTemplate;
    }

    public void setPromptTemplate(String promptTemplate) {
        this.promptTemplate = promptTemplate;
    }

    public String getFileBlacklist() {
        return fileBlacklist;
    }

    public void setFileBlacklist(String fileBlacklist) {
        this.fileBlacklist = fileBlacklist;
    }

    public String getTextFileExtensions() {
        return textFileExtensions;
    }

    public void setTextFileExtensions(String textFileExtensions) {
        this.textFileExtensions = textFileExtensions;
    }

    public int getDiffContextSize() {
        return diffContextSize;
    }

    public void setDiffContextSize(int diffContextSize) {
        this.diffContextSize = diffContextSize;
    }
}
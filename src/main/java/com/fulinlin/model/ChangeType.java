package com.fulinlin.model;

public enum ChangeType {
    FEATURE("feat", "新功能"),
    FIX("fix", "Bug修复"),
    DOCS("docs", "文档更新"),
    STYLE("style", "代码格式"),
    REFACTOR("refactor", "重构"),
    TEST("test", "测试相关"),
    CHORE("chore", "构建/工具"),
    OTHER("other", "其他");

    private final String type;
    private final String description;

    ChangeType(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
package com.fulinlin.utils;

import com.fulinlin.model.AISettings;
import com.fulinlin.model.CommitTemplate;
import com.intellij.openapi.project.Project;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * AI生成主流程服务
 */
public class AIGeneratorService {
    private final CodeChangeAnalyzer analyzer;
    private final PromptBuilder promptBuilder;
    private final DeepSeekAPIClient apiClient;

    public AIGeneratorService(AISettings settings) {
        this.analyzer = new CodeChangeAnalyzer();
        this.promptBuilder = new PromptBuilder();
        this.apiClient = new DeepSeekAPIClient(settings);
    }

    /**
     * 串联分析、提示词、API调用、响应解析，异步生成CommitTemplate
     *
     * @param project     当前项目
     * @param templateKey 模板key（如conventional_en, conventional_zh）
     * @param locale      语言
     * @return CompletableFuture<CommitTemplate>
     */
    public CompletableFuture<CommitTemplate> generateCommitMessage(Project project,
            String templateKey, Locale locale) {
        return CompletableFuture.supplyAsync(() -> analyzer.analyzeChanges(project))
                .thenCompose(changeInfo -> {
                    String prompt = promptBuilder.buildPrompt(changeInfo, templateKey, locale);
                    return apiClient.generateMessage(prompt)
                            .thenApply(this::parseToCommitTemplate);
                })
                .exceptionally(e -> {
                    IDENotificationUtil.notifyError(project, "AI生成失败", e.getMessage());
                    return new CommitTemplate();
                });
    }

    /**
     * 解析AI响应为CommitTemplate对象
     * 这里假设AI输出为结构化文本（如Type/Scope/Subject/Body/Changes/Closes），可用正则或简单分割解析
     */
    private CommitTemplate parseToCommitTemplate(String aiResponse) {
        CommitTemplate template = new CommitTemplate();
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return template;
        }
        // 简单解析：按行分割，查找关键字
        String[] lines = aiResponse.split("\n");
        for (String line : lines) {
            String l = line.trim();
            if (l.toLowerCase().startsWith("type:")) {
                template.setType(l.substring(5).trim());
            } else if (l.toLowerCase().startsWith("scope:")) {
                template.setScope(l.substring(6).trim());
            } else if (l.toLowerCase().startsWith("subject:")) {
                template.setSubject(l.substring(8).trim());
            } else if (l.toLowerCase().startsWith("body:")) {
                template.setBody(l.substring(5).trim());
            } else if (l.toLowerCase().startsWith("breaking changes:")) {
                template.setChanges(l.substring(16).trim());
            } else if (l.toLowerCase().startsWith("closes:")) {
                template.setCloses(l.substring(7).trim());
            }
        }
        return template;
    }
}
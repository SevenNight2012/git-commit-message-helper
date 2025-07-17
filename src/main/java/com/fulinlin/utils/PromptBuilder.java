package com.fulinlin.utils;

import com.fulinlin.model.CodeChangeInfo;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * AI提示词生成器，支持多语言和多模板
 */
public class PromptBuilder {
    // 多语言多模板支持
    private static final Map<String, String> TEMPLATE_MAP = new HashMap<>();
    static {
        // 英文-Conventional Commits
        TEMPLATE_MAP.put("conventional_en", "You are a professional Git commit message assistant. Based on the following code changes, generate a commit message that conforms to the Conventional Commits specification.\n\nCode changes:\n- Changed files: {0}\n- Change type: {1}\n- Diff content:\n{2}\n\nPlease generate a commit message with the following parts:\nType: (feat/fix/docs/style/refactor/test/chore)\nScope: (optional)\nSubject: short description (max 50 chars)\nBody: detailed description (optional)\nBreaking Changes: (optional)\nCloses: (optional)\n\nRequirements:\n- Use English\n- Follow Conventional Commits\n- Be accurate, concise, clear\n- If breaking changes, mark clearly\n- Output format should match the template");
        // 中文-Conventional Commits
        TEMPLATE_MAP.put("conventional_zh", "你是一个专业的Git commit message生成助手。请根据以下代码变更信息，生成一个符合Conventional Commits规范的commit message。\n\n代码变更信息：\n- 变更文件：{0}\n- 变更类型：{1}\n- 代码差异：\n{2}\n\n请生成包含以下部分的commit message：\nType: 提交类型（feat/fix/docs/style/refactor/test/chore）\nScope: 影响范围（可选）\nSubject: 简短描述（50字符以内）\nBody: 详细描述（可选）\nBreaking Changes: 破坏性变更（可选）\nCloses: 关闭的Issue（可选）\n\n要求：\n- 使用英文\n- 遵循Conventional Commits规范\n- 描述准确、简洁、清晰\n- 如果有破坏性变更，请明确标注\n- 输出格式应与现有模板格式保持一致，最重要的一点请不要有任何多余的文字，严格按照格式输出commit message即可");
    }

    /**
     * 构建AI
     * @param info 代码变更信息
     * @param templateKey 模板key（如conventional_en, conventional_zh）
     * @param locale 语言
     * @return prompt字符串
     */
    public String buildPrompt(CodeChangeInfo info, String templateKey, Locale locale) {
        String template = TEMPLATE_MAP.getOrDefault(templateKey, TEMPLATE_MAP.get("conventional_en"));
        String fileList = info.getChangedFiles() != null ? info.getChangedFiles().stream()
                .map(f -> f.getFilePath())
                .reduce((a, b) -> a + ", " + b).orElse("") : "";
        String changeType = info.getChangeType() != null ? info.getChangeType().getDescription() : "";
        String diffContent = info.getDiffContent() != null ? info.getDiffContent() : "";
        return MessageFormat.format(template, fileList, changeType, diffContent);
    }

    /**
     * 默认英文Conventional Commits模板
     */
    public String buildPrompt(CodeChangeInfo info) {
        return buildPrompt(info, "conventional_en", Locale.ENGLISH);
    }
}
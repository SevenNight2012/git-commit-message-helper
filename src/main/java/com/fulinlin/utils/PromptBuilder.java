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
        TEMPLATE_MAP.put("conventional_en", "You are a professional Git commit message assistant. Based on the following code changes, generate a commit message that conforms to the Conventional Commits specification.\n\nCode changes:\n- Changed files: {0}\n- Diff content:\n{1}\n\nPlease analyze the code changes carefully and determine:\n1. Type: Choose the most appropriate type from (feat/fix/docs/style/refactor/test/chore) based on the actual changes\n2. Scope: Identify the affected module/component based on file paths and code changes\n3. Subject: Write a concise description (max 50 chars)\n4. Body: Provide detailed description if needed\n5. Breaking Changes: Mark if there are breaking changes\n6. Closes: Reference related issues if any\n\nRequirements:\n- Use English\n- Follow Conventional Commits specification\n- Analyze code changes to determine type and scope accurately\n- Be concise, clear, and accurate\n- Output ONLY the commit message in the exact format below, NO extra text:\n\ntype(scope): subject\n\nbody\n\nBREAKING CHANGE: description\nCloses: #issue");

        // 中文-Conventional Commits
        TEMPLATE_MAP.put("conventional_zh", "你是一个专业的Git commit message生成助手。请根据以下代码变更信息，生成一个符合Conventional Commits规范的commit message。\n\n代码变更信息：\n- 变更文件：{0}\n- 代码差异：\n{1}\n\n请仔细分析代码变更并确定：\n1. Type: 根据实际变更内容选择最合适的类型（feat/fix/docs/style/refactor/test/chore）\n2. Scope: 根据文件路径和代码变更识别受影响的模块/组件\n3. Subject: 编写简洁的描述（50字符以内）\n4. Body: 如需要提供详细描述\n5. Breaking Changes: 如有破坏性变更请标注\n6. Closes: 如有相关Issue请引用\n\n要求：\n- 使用中文\n- 遵循Conventional Commits规范\n- 通过分析代码变更准确确定类型和范围\n- 描述准确、简洁、清晰\n- 严格按照以下格式输出commit message，不要有任何多余的文字：\n\ntype(scope): subject\n\nbody\n\nBREAKING CHANGE: description\nCloses: #issue");
    }

    /**
     * 构建AI提示词
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
        String diffContent = info.getDiffContent() != null ? info.getDiffContent() : "";
        return MessageFormat.format(template, fileList, diffContent);
    }

    /**
     * 默认中文Conventional Commits模板
     */
    public String buildPrompt(CodeChangeInfo info) {
        return buildPrompt(info, "conventional_zh", Locale.CHINESE);
    }
}
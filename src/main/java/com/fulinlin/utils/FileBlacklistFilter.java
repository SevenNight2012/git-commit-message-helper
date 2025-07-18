package com.fulinlin.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 文件黑名单过滤器
 * 根据正则表达式规则过滤敏感文件
 * 支持通配符模式自动转换为正则表达式
 */
public class FileBlacklistFilter {

    private List<Pattern> blacklistPatterns;

    public FileBlacklistFilter() {
        this.blacklistPatterns = new ArrayList<>();
    }

    /**
     * 从字符串配置中初始化黑名单模式
     * @param blacklistConfig 黑名单配置字符串，每行一个正则表达式或通配符模式
     */
    public void initializeFromConfig(String blacklistConfig) {
        blacklistPatterns.clear();

        if (blacklistConfig == null || blacklistConfig.trim().isEmpty()) {
            return;
        }

        String[] lines = blacklistConfig.split("\n");
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (!trimmedLine.isEmpty()) {
                try {
                    // 自动检测并转换通配符模式
                    String regexPattern = convertWildcardToRegex(trimmedLine);
                    Pattern pattern = Pattern.compile(regexPattern);
                    blacklistPatterns.add(pattern);
                } catch (PatternSyntaxException e) {
                    // 记录无效的正则表达式，但不抛出异常
                    System.err.println("Invalid regex pattern in file blacklist: " + trimmedLine + " - " + e.getMessage());
                }
            }
        }
    }

    /**
     * 将通配符模式转换为正则表达式
     * @param wildcardPattern 通配符模式（如 *.md, *.txt, config/*.json）
     * @return 对应的正则表达式
     */
    public String convertWildcardToRegex(String wildcardPattern) {
        // 如果已经是正则表达式模式（包含特殊字符），直接返回
        if (isRegexPattern(wildcardPattern)) {
            return wildcardPattern;
        }

        StringBuilder regex = new StringBuilder();
        boolean inBracket = false;

        for (int i = 0; i < wildcardPattern.length(); i++) {
            char c = wildcardPattern.charAt(i);

            switch (c) {
                case '*':
                    // * 转换为 .*
                    regex.append(".*");
                    break;
                case '?':
                    // ? 转换为 .
                    regex.append(".");
                    break;
                case '.':
                    // . 转换为 \.
                    regex.append("\\.");
                    break;
                case '[':
                    // [ 转换为 \[
                    regex.append("\\[");
                    inBracket = true;
                    break;
                case ']':
                    // ] 转换为 \]
                    regex.append("\\]");
                    inBracket = false;
                    break;
                case '(':
                    // ( 转换为 \(
                    regex.append("\\(");
                    break;
                case ')':
                    // ) 转换为 \)
                    regex.append("\\)");
                    break;
                case '+':
                    // + 转换为 \+
                    regex.append("\\+");
                    break;
                case '|':
                    // | 转换为 \|
                    regex.append("\\|");
                    break;
                case '^':
                    // ^ 转换为 \^
                    regex.append("\\^");
                    break;
                case '$':
                    // $ 转换为 \$
                    regex.append("\\$");
                    break;
                case '{':
                    // { 转换为 \{
                    regex.append("\\{");
                    break;
                case '}':
                    // } 转换为 \}
                    regex.append("\\}");
                    break;
                case '\\':
                    // \ 转换为 \\
                    regex.append("\\\\");
                    break;
                default:
                    // 其他字符直接添加
                    regex.append(c);
                    break;
            }
        }

                // 如果模式以 * 结尾，添加 $ 确保匹配文件结尾
        String result = regex.toString();
        if (result.endsWith(".*")) {
            result = result + "$";
        } else if (wildcardPattern.contains("*") && !result.endsWith("$")) {
            // 如果包含通配符但没有以 $ 结尾，添加 $ 确保匹配文件结尾
            result = result + "$";
        }

        return result;
    }

            /**
     * 判断是否为正则表达式模式
     * @param pattern 模式字符串
     * @return true 如果是正则表达式模式，false 如果是通配符模式
     */
    private boolean isRegexPattern(String pattern) {
        // 检查是否包含正则表达式的特殊字符（除了 * 和 ?）
        String regexSpecialChars = "()[]{}+|^$\\";
        for (char c : regexSpecialChars.toCharArray()) {
            if (pattern.indexOf(c) != -1) {
                return true;
            }
        }

        // 检查是否包含正则表达式的转义序列
        if (pattern.contains("\\.") || pattern.contains("\\*") || pattern.contains("\\?") ||
            pattern.contains("\\+") || pattern.contains("\\|") || pattern.contains("\\^") ||
            pattern.contains("\\$") || pattern.contains("\\(") || pattern.contains("\\)") ||
            pattern.contains("\\[") || pattern.contains("\\]") || pattern.contains("\\{") ||
            pattern.contains("\\}")) {
            return true;
        }

        // 检查是否以 $ 结尾（正则表达式特征）
        if (pattern.endsWith("$")) {
            return true;
        }

        // 检查是否包含 .* 模式（正则表达式特征）
        if (pattern.contains(".*")) {
            return true;
        }

        return false;
    }

    /**
     * 检查文件是否在黑名单中
     * @param filePath 文件路径
     * @return true 如果文件应该被过滤（在黑名单中），false 否则
     */
    public boolean isFileBlacklisted(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }

        // 统一路径分隔符
        String normalizedPath = filePath.replace('\\', '/');

        for (Pattern pattern : blacklistPatterns) {
            if (pattern.matcher(normalizedPath).matches()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 过滤文件列表，移除黑名单中的文件
     * @param filePaths 文件路径列表
     * @return 过滤后的文件路径列表
     */
    public List<String> filterFiles(List<String> filePaths) {
        List<String> filteredFiles = new ArrayList<>();

        for (String filePath : filePaths) {
            if (!isFileBlacklisted(filePath)) {
                filteredFiles.add(filePath);
            }
        }

        return filteredFiles;
    }

    /**
     * 获取黑名单模式列表（用于调试）
     * @return 黑名单模式列表
     */
    public List<String> getBlacklistPatterns() {
        List<String> patterns = new ArrayList<>();
        for (Pattern pattern : blacklistPatterns) {
            patterns.add(pattern.pattern());
        }
        return patterns;
    }

    /**
     * 检查黑名单配置是否有效
     * @param blacklistConfig 黑名单配置字符串
     * @return 验证结果，包含是否有效和错误信息
     */
    public static ValidationResult validateBlacklistConfig(String blacklistConfig) {
        if (blacklistConfig == null || blacklistConfig.trim().isEmpty()) {
            return new ValidationResult(true, null);
        }

        String[] lines = blacklistConfig.split("\n");
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                try {
                    // 创建临时实例来测试转换
                    FileBlacklistFilter tempFilter = new FileBlacklistFilter();
                    String regexPattern = tempFilter.convertWildcardToRegex(line);
                    Pattern.compile(regexPattern);
                } catch (PatternSyntaxException e) {
                    errors.add("Line " + (i + 1) + ": " + line + " - " + e.getMessage());
                }
            }
        }

        return new ValidationResult(errors.isEmpty(), errors.isEmpty() ? null : String.join("\n", errors));
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final String errorMessage;

        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return isValid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
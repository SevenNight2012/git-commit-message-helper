package com.fulinlin.ui.commit;

import com.fulinlin.localization.PluginBundle;
import com.fulinlin.model.AISettings;
import com.fulinlin.storage.GitCommitMessageHelperSettings;
import com.fulinlin.utils.AIGeneratorService;
import com.fulinlin.utils.CodeChangeAnalyzer;
import com.fulinlin.utils.IDENotificationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;

import org.apache.commons.lang3.StringUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * AI生成面板，用于AI生成commit message
 */
public class AIGeneratePanel {
    private JPanel mainPanel;
    private JButton aiGenerateButton;
    private JButton retryButton;
    private JLabel aiStatusLabel;
    private JTextArea aiContentTextArea;
    private JLabel aiInstructionsLabel;
    private JProgressBar progressBar;
    private AIGeneratorService aiGeneratorService;
    private final AISettings aiSettings;
    private final Project project;
    private final CheckinProjectPanel gitPanel;

    // 新增关闭issue相关组件
    private JTextField closedIssuesTextField;
    private JLabel closedIssuesLabel;

    // 重试相关
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private final AtomicInteger retryCount = new AtomicInteger(0);
    private CompletableFuture<String> currentGenerationTask;

    public AIGeneratePanel(Project project, AISettings aiSettings, CheckinProjectPanel gitPanel) {
        this.project = project;
        this.aiSettings = aiSettings;
        this.gitPanel = gitPanel;
        initComponents();
        initAI();
    }

    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // AI Control Panel
        JPanel controlPanel = new JPanel(new BorderLayout());

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aiGenerateButton = new JButton("🤖 AI Generate");
        aiGenerateButton.setToolTipText("Generate commit message with AI");

        retryButton = new JButton("🔄 Retry");
        retryButton.setToolTipText("Retry AI generation");
        retryButton.setEnabled(false);

        buttonPanel.add(aiGenerateButton);
        buttonPanel.add(retryButton);
        buttonPanel.add(Box.createHorizontalGlue()); // 添加弹性空间

        // 状态和进度面板
        JPanel statusPanel = new JPanel(new BorderLayout());
        aiStatusLabel = new JLabel("Ready");
        aiStatusLabel.setForeground(Color.GRAY);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        statusPanel.add(aiStatusLabel, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.CENTER);

        controlPanel.add(buttonPanel, BorderLayout.NORTH);
        controlPanel.add(statusPanel, BorderLayout.CENTER);

        // 关闭issue输入面板
        JPanel closedIssuesPanel = new JPanel(new BorderLayout());
        closedIssuesLabel = new JLabel(PluginBundle.get("commit.panel.closes.field"));
        closedIssuesLabel.setToolTipText("Enter issue numbers to close (e.g., #123, #456)");
        closedIssuesTextField = new JTextField();
        closedIssuesTextField.setToolTipText("Enter issue numbers to close (e.g., #123, #456)");

        closedIssuesPanel.add(closedIssuesLabel, BorderLayout.WEST);
        closedIssuesPanel.add(closedIssuesTextField, BorderLayout.CENTER);
        closedIssuesPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        // AI Content Area
        aiContentTextArea = new JTextArea();
        aiContentTextArea.setEditable(true);
        aiContentTextArea.setLineWrap(true);
        aiContentTextArea.setWrapStyleWord(true);
        aiContentTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        aiContentTextArea.setRows(10);

        JScrollPane scrollPane = new JScrollPane(aiContentTextArea);
        scrollPane.setPreferredSize(new Dimension(600, 300));

        // Instructions
        aiInstructionsLabel = new JLabel(
            "Click 'AI Generate' to automatically generate a commit message based on your code changes. " +
                    "You can edit the generated message before committing. Optionally enter issue numbers to close."
        );
        aiInstructionsLabel.setForeground(Color.GRAY);

        // Layout
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(closedIssuesPanel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(aiInstructionsLabel, BorderLayout.SOUTH);
    }

    private void initAI() {
        if (isAIAvailable()) {
            // 创建CodeChangeAnalyzer时传入文件黑名单配置
            CodeChangeAnalyzer analyzer = new CodeChangeAnalyzer(gitPanel, aiSettings);
            aiGeneratorService = new AIGeneratorService(aiSettings, analyzer);
            aiGenerateButton.addActionListener(e -> generateWithAI());
            retryButton.addActionListener(e -> retryGeneration());
            aiStatusLabel.setText("Ready");
            aiStatusLabel.setForeground(Color.GRAY);
        } else {
            // Disable AI if not available
            aiGenerateButton.setEnabled(false);
            retryButton.setEnabled(false);
            aiStatusLabel.setText(getAIUnavailableReason());
            aiStatusLabel.setForeground(Color.GRAY);
        }
    }

    /**
     * 检查AI功能是否可用
     */
    private boolean isAIAvailable() {
        if (aiSettings == null) {
            return false;
        }

        if (!aiSettings.isEnabled()) {
            return false;
        }

        if (aiSettings.getApiKey() == null || aiSettings.getApiKey().trim().isEmpty()) {
            return false;
        }

        if (aiSettings.getApiEndpoint() == null || aiSettings.getApiEndpoint().trim().isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * 获取AI不可用的原因
     */
    private String getAIUnavailableReason() {
        if (aiSettings == null) {
            return "AI settings not configured";
        }

        if (!aiSettings.isEnabled()) {
            return "AI is disabled";
        }

        if (aiSettings.getApiKey() == null || aiSettings.getApiKey().trim().isEmpty()) {
            return "API Key not configured";
        }

        if (aiSettings.getApiEndpoint() == null || aiSettings.getApiEndpoint().trim().isEmpty()) {
            return "API endpoint not configured";
        }

        return "AI not available";
    }

    private void generateWithAI() {
        if (currentGenerationTask != null && !currentGenerationTask.isDone()) {
            // 如果当前有正在进行的任务，取消它
            currentGenerationTask.cancel(true);
        }

        retryCount.set(0);
        startGeneration();
    }

    private void retryGeneration() {
        if (retryCount.get() >= MAX_RETRY_ATTEMPTS) {
            showMaxRetriesReached();
            return;
        }

        retryCount.incrementAndGet();
        startGeneration();
    }

    private void startGeneration() {
        setGeneratingState(true);

        Locale locale = Locale.ENGLISH; // 可根据设置或系统自动切换
        // 使用设置中的提示语模板
        String templateKey = aiSettings.getPromptTemplate();

        currentGenerationTask = aiGeneratorService.generateCommitMessage(project, templateKey, locale)
            .thenApply(content -> {
                SwingUtilities.invokeLater(() -> {
                    updateAIContent(content);
                    setGeneratingState(false);
                    retryCount.set(0); // 重置重试计数
                });
                return content;
            })
            .exceptionally(error -> {
                SwingUtilities.invokeLater(() -> {
                    handleGenerationError(error);
                });
                return "";
            });
    }

    private void setGeneratingState(boolean generating) {
        aiGenerateButton.setEnabled(!generating);
        retryButton.setEnabled(false);
        progressBar.setVisible(generating);

        if (generating) {
            aiStatusLabel.setText("Generating...");
            aiStatusLabel.setForeground(Color.BLUE);
        } else {
            progressBar.setVisible(false);
        }
    }

    private void handleGenerationError(Throwable error) {
        setGeneratingState(false);

        String errorMessage = getErrorMessage(error);
        String userFriendlyMessage = getUserFriendlyErrorMessage(error);

        // 更新UI状态
        aiStatusLabel.setText(errorMessage);
        aiStatusLabel.setForeground(Color.RED);

        // 显示重试按钮（如果还有重试次数）
        if (retryCount.get() < MAX_RETRY_ATTEMPTS) {
            retryButton.setEnabled(true);
            retryButton.setText("🔄 Retry (" + (MAX_RETRY_ATTEMPTS - retryCount.get()) + " left)");
        } else {
            retryButton.setEnabled(false);
            retryButton.setText("🔄 Retry");
        }

        // 发送通知
        IDENotificationUtil.notifyError(project, "AI Generation Failed", userFriendlyMessage);

        // 记录详细错误信息
        System.err.println("AI Generation Error: " + error.getMessage());
        error.printStackTrace();
    }

    private String getErrorMessage(Throwable error) {
        if (error instanceof java.net.ConnectException ||
            error.getMessage().contains("connect") ||
            error.getMessage().contains("Connection refused")) {
            return "Network connection failed";
        } else if (error instanceof java.net.SocketTimeoutException ||
                   error.getMessage().contains("timeout")) {
            return "Request timeout";
        } else if (error.getMessage().contains("401") ||
                   error.getMessage().contains("Unauthorized")) {
            return "Invalid API Key";
        } else if (error.getMessage().contains("429") ||
                   error.getMessage().contains("Too Many Requests")) {
            return "Rate limit exceeded";
        } else if (error.getMessage().contains("500") ||
                   error.getMessage().contains("Internal Server Error")) {
            return "Server error";
        } else if (error.getMessage().contains("403") ||
                   error.getMessage().contains("Forbidden")) {
            return "Access forbidden";
        } else {
            return "Generation failed";
        }
    }

    private String getUserFriendlyErrorMessage(Throwable error) {
        String message = error.getMessage();

        if (message.contains("connect") || message.contains("Connection refused")) {
            return "无法连接到AI服务器。请检查网络连接或稍后重试。";
        } else if (message.contains("timeout")) {
            return "请求超时。服务器响应时间过长，请稍后重试。";
        } else if (message.contains("401") || message.contains("Unauthorized")) {
            return "API密钥无效。请在设置中检查并更新您的API密钥。";
        } else if (message.contains("429") || message.contains("Too Many Requests")) {
            return "请求频率过高。请稍等片刻后重试。";
        } else if (message.contains("500") || message.contains("Internal Server Error")) {
            return "服务器内部错误。请稍后重试或联系技术支持。";
        } else if (message.contains("403") || message.contains("Forbidden")) {
            return "访问被拒绝。请检查您的API密钥权限。";
        } else {
            return "AI生成失败：" + message;
        }
    }

    private void showMaxRetriesReached() {
        aiStatusLabel.setText("Max retries reached");
        aiStatusLabel.setForeground(Color.RED);
        retryButton.setEnabled(false);
        retryButton.setText("🔄 Retry");

        IDENotificationUtil.notifyWarning(project, "Max Retries Reached",
            "已达到最大重试次数。请检查网络连接或稍后重试。");
    }

    private void updateAIContent(String content) {
        // Update the AI content text area directly with the generated content
        if (null != content) {
            content = content.replaceAll("```","").trim();
        }
        aiContentTextArea.setText(content);

        // Update status
        aiStatusLabel.setText("Generated successfully");
        aiStatusLabel.setForeground(Color.GREEN);

        // // 显示成功通知
        // IDENotificationUtil.notifySuccess(project, "AI Generation Success",
        //     "Commit message generated successfully. You can now edit it before committing.");
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public String getAIContent() {
        String aiContent = aiContentTextArea.getText().trim();
        String closedIssues = closedIssuesTextField.getText().trim();

        if (StringUtils.isEmpty(aiContent)) {
            return "";
        }

        if (StringUtils.isNotEmpty(closedIssues)) {
            // 格式化issue
            String formattedIssues = formatIssues(closedIssues);

            // 如果AI内容已经包含Closes部分，则替换它
            if (aiContent.contains("Closes:") || aiContent.contains("closes:")) {
                // 移除现有的Closes部分
                String[] lines = aiContent.split("\n");
                StringBuilder newContent = new StringBuilder();
                boolean skipCloses = false;

                for (String line : lines) {
                    if (line.trim().toLowerCase().startsWith("closes:")) {
                        skipCloses = true;
                        continue;
                    }
                    if (skipCloses && line.trim().isEmpty()) {
                        skipCloses = false;
                        continue;
                    }
                    if (!skipCloses) {
                        newContent.append(line).append("\n");
                    }
                }

                aiContent = newContent.toString().trim();
            }

            // 添加新的Closes部分
            return aiContent + "\n\nCloses: " + formattedIssues;
        }

        return aiContent;
    }

    public void setAIContent(String content) {
        aiContentTextArea.setText(content);
    }

    /**
     * 获取关闭的issue
     */
    public String getClosedIssues() {
        return formatIssues(closedIssuesTextField.getText().trim());
    }

    /**
     * 设置关闭的issue
     */
    public void setClosedIssues(String closedIssues) {
        closedIssuesTextField.setText(closedIssues);
    }

    /**
     * 获取原始issue输入（未格式化）
     */
    public String getRawClosedIssues() {
        return closedIssuesTextField.getText().trim();
    }

    /**
     * 验证issue格式是否正确
     */
    public boolean isValidIssueFormat(String issues) {
        if (StringUtils.isEmpty(issues)) {
            return true; // 空值是有效的
        }

        // 简单的格式验证：检查是否包含#号
        String[] issueArray = issues.split(",");
        for (String issue : issueArray) {
            String trimmed = issue.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                return false;
            }
        }
        return true;
    }

    /**
     * 格式化issue字符串，确保格式正确
     */
    public String formatIssues(String issues) {
        if (StringUtils.isEmpty(issues)) {
            return "";
        }

        String[] issueArray = issues.split(",");
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < issueArray.length; i++) {
            String issue = issueArray[i].trim();
            if (!issue.isEmpty()) {
                if (!issue.startsWith("#")) {
                    issue = "#" + issue;
                }
                if (i > 0) {
                    formatted.append(", ");
                }
                formatted.append(issue);
            }
        }

        return formatted.toString();
    }

    public boolean hasContent() {
        return !aiContentTextArea.getText().trim().isEmpty() || !getRawClosedIssues().isEmpty();
    }

    /**
     * 触发AI生成（供外部调用）
     */
    public void triggerAIGeneration() {
        if (aiGenerateButton.isEnabled()) {
            aiGenerateButton.doClick();
        }
    }

    /**
     * 显示指导信息
     */
    public void showGuidanceMessage(String message) {
        aiStatusLabel.setText(message);
        aiStatusLabel.setForeground(Color.BLUE);

        // 3秒后恢复原始状态
        Timer timer = new Timer(3000, e -> {
            if (isAIAvailable()) {
                aiStatusLabel.setText("Ready");
                aiStatusLabel.setForeground(Color.GRAY);
            } else {
                aiStatusLabel.setText(getAIUnavailableReason());
                aiStatusLabel.setForeground(Color.GRAY);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * 检查是否有正在进行的生成任务
     */
    public boolean isGenerating() {
        return currentGenerationTask != null && !currentGenerationTask.isDone();
    }

    /**
     * 取消当前生成任务
     */
    public void cancelGeneration() {
        if (currentGenerationTask != null && !currentGenerationTask.isDone()) {
            currentGenerationTask.cancel(true);
            setGeneratingState(false);
            aiStatusLabel.setText("Generation cancelled");
            aiStatusLabel.setForeground(Color.GRAY);
        }
    }
}
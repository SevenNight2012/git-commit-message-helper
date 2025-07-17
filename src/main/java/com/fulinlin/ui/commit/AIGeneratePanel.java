package com.fulinlin.ui.commit;

import com.fulinlin.model.AISettings;
import com.fulinlin.model.CommitTemplate;
import com.fulinlin.utils.AIGeneratorService;
import com.fulinlin.utils.IDENotificationUtil;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

/**
 * AI生成面板，用于AI生成commit message
 */
public class AIGeneratePanel {
    private JPanel mainPanel;
    private JButton aiGenerateButton;
    private JLabel aiStatusLabel;
    private JTextArea aiContentTextArea;
    private JLabel aiInstructionsLabel;
    private AIGeneratorService aiGeneratorService;
    private final AISettings aiSettings;
    private final Project project;

    public AIGeneratePanel(Project project, AISettings aiSettings) {
        this.project = project;
        this.aiSettings = aiSettings;
        initComponents();
        initAI();
    }

    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // AI Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        aiGenerateButton = new JButton("🤖 AI Generate");
        aiGenerateButton.setToolTipText("Generate commit message with AI");
        aiStatusLabel = new JLabel("Ready");
        aiStatusLabel.setForeground(Color.GRAY);

        controlPanel.add(aiGenerateButton);
        controlPanel.add(aiStatusLabel);
        controlPanel.add(Box.createHorizontalGlue()); // 添加弹性空间

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
            "You can edit the generated message before committing."
        );
        aiInstructionsLabel.setForeground(Color.GRAY);

        // Layout
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(aiInstructionsLabel, BorderLayout.SOUTH);
    }

    private void initAI() {
        if (isAIAvailable()) {
            aiGeneratorService = new AIGeneratorService(aiSettings);
            aiGenerateButton.addActionListener(e -> generateWithAI());
            aiStatusLabel.setText("Ready");
            aiStatusLabel.setForeground(Color.GRAY);
        } else {
            // Disable AI if not available
            aiGenerateButton.setEnabled(false);
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
        aiGenerateButton.setEnabled(false);
        aiStatusLabel.setText("Generating...");
        aiStatusLabel.setForeground(Color.BLUE);

        Locale locale = Locale.ENGLISH; // 可根据设置或系统自动切换
        String templateKey = "conventional_en";

        aiGeneratorService.generateCommitMessage(project, templateKey, locale)
            .thenAccept(template -> {
                SwingUtilities.invokeLater(() -> {
                    updateAIContent(template);
                    aiGenerateButton.setEnabled(true);
                });
            })
            .exceptionally(error -> {
                SwingUtilities.invokeLater(() -> {
                    aiStatusLabel.setText("Generation failed");
                    aiStatusLabel.setForeground(Color.RED);
                    IDENotificationUtil.notifyError(project, "AI Generation Error", error.getMessage());
                    aiGenerateButton.setEnabled(true);
                });
                return null;
            });
    }

    private void updateAIContent(CommitTemplate template) {
        // Build the commit message string from template
        StringBuilder commitMessage = new StringBuilder();

        // Build the header: type(scope): subject
        if (template.getType() != null) {
            commitMessage.append(template.getType());
        }
        if (template.getScope() != null && !template.getScope().trim().isEmpty()) {
            commitMessage.append("(").append(template.getScope()).append(")");
        }
        if (template.getSubject() != null) {
            commitMessage.append(": ").append(template.getSubject());
        }

        // Add body if present
        if (template.getBody() != null && !template.getBody().trim().isEmpty()) {
            commitMessage.append("\n\n").append(template.getBody());
        }

        // Add breaking changes if present
        if (template.getChanges() != null && !template.getChanges().trim().isEmpty()) {
            commitMessage.append("\n\nBREAKING CHANGE: ").append(template.getChanges());
        }

        // Add closes if present
        if (template.getCloses() != null && !template.getCloses().trim().isEmpty()) {
            commitMessage.append("\n\nCloses: ").append(template.getCloses());
        }

        // Update the AI content text area
        aiContentTextArea.setText(commitMessage.toString());

        // Update status
        aiStatusLabel.setText("Generated successfully");
        aiStatusLabel.setForeground(Color.GREEN);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public String getAIContent() {
        return aiContentTextArea.getText().trim();
    }

    public void setAIContent(String content) {
        aiContentTextArea.setText(content);
    }

    public boolean hasContent() {
        return !aiContentTextArea.getText().trim().isEmpty();
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
}
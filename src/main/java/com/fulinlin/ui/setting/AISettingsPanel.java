package com.fulinlin.ui.setting;

import com.fulinlin.model.AISettings;
import com.fulinlin.utils.DeepSeekAPIClient;
import com.fulinlin.utils.FileBlacklistFilter;
import com.fulinlin.utils.IDENotificationUtil;
import com.fulinlin.utils.NetworkRequestLogger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

public class AISettingsPanel {
    private JPanel mainPanel;
    private JTextField apiKeyField;
    private JTextField endpointField;
    private JComboBox<String> modelComboBox;
    private JSpinner maxTokensSpinner;
    private JSpinner temperatureSpinner;
    private JCheckBox enabledCheckBox;
    private JCheckBox autoGenerateCheckBox;
    private JComboBox<String> promptTemplateComboBox;
    private JTextArea fileBlacklistTextArea;
    private JButton testConnectionButton;
    private Project project;
    private JTextField textFileExtensionsField;
    private JSpinner diffContextSizeSpinner;
    private JTextArea logDirectoryField;

    public AISettingsPanel() {
        this(null);
    }

    public AISettingsPanel(Project project) {
        this.project = project;
        initComponents();
        setupEventHandlers();
        initializeLogDirectory();
    }

    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // AI配置面板（合并API和生成设置）
        JPanel aiConfigPanel = createAIConfigPanel();

        // 文件过滤面板
        JPanel filterPanel = createFilterPanel();

        // 组装主面板
        mainPanel.add(aiConfigPanel, BorderLayout.NORTH);
        mainPanel.add(filterPanel, BorderLayout.CENTER);
    }

    private JPanel createAIConfigPanel() {
        JPanel aiConfigPanel = new JPanel(new BorderLayout(10, 10));
        aiConfigPanel.setBorder(BorderFactory.createTitledBorder("AI Configuration"));

        // 创建左侧的API配置面板
        JPanel apiPanel = createAPIPanel();

        // 创建右侧的生成设置面板
        JPanel generationPanel = createGenerationPanel();

        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();

        // 创建左右分栏布局，使用GridBagLayout精确控制宽度比例
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 10);

        // API面板占据45%宽度
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.40;
        contentPanel.add(apiPanel, gbc);

        // Generation面板占据55%宽度
        gbc.gridx = 1;
        gbc.weightx = 0.60;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(generationPanel, gbc);

        // 组装AI配置面板
        aiConfigPanel.add(contentPanel, BorderLayout.CENTER);
        aiConfigPanel.add(buttonPanel, BorderLayout.SOUTH);

        return aiConfigPanel;
    }

    private JPanel createAPIPanel() {
        JPanel apiPanel = new JPanel(new GridBagLayout());
        apiPanel.setBorder(BorderFactory.createTitledBorder("API Settings"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // API Key
        gbc.gridx = 0;
        gbc.gridy = 0;
        apiPanel.add(new JLabel("API Key:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        apiKeyField = new JPasswordField(25);
        apiPanel.add(apiKeyField, gbc);

        // Endpoint
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        apiPanel.add(new JLabel("Endpoint:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        endpointField = new JTextField("https://api.deepseek.com/v1/chat/completions", 25);
        apiPanel.add(endpointField, gbc);

        // Model
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        apiPanel.add(new JLabel("Model:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        modelComboBox = new ComboBox<>(new String[]{"deepseek-chat"});
        apiPanel.add(modelComboBox, gbc);

        return apiPanel;
    }

    private JPanel createGenerationPanel() {
        JPanel generationPanel = new JPanel(new GridBagLayout());
        generationPanel.setBorder(BorderFactory.createTitledBorder("Generation Settings"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Max Tokens
        gbc.gridx = 0;
        gbc.gridy = 0;
        generationPanel.add(new JLabel("Max Tokens:"), gbc);

        gbc.gridx = 1;
        maxTokensSpinner = new JSpinner(new SpinnerNumberModel(500, 100, 2000, 50));
        generationPanel.add(maxTokensSpinner, gbc);

        // Temperature
        gbc.gridx = 0;
        gbc.gridy = 1;
        generationPanel.add(new JLabel("Temperature:"), gbc);

        gbc.gridx = 1;
        temperatureSpinner = new JSpinner(new SpinnerNumberModel(0.7, 0.0, 2.0, 0.1));
        generationPanel.add(temperatureSpinner, gbc);

        // Prompt Template
        gbc.gridx = 0;
        gbc.gridy = 2;
        generationPanel.add(new JLabel("Prompt Template:"), gbc);

        gbc.gridx = 1;
        promptTemplateComboBox = new ComboBox<>(new String[]{
                "conventional_zh - 中文提示语",
                "conventional_en - 英文提示语"
        });
        generationPanel.add(promptTemplateComboBox, gbc);

        // Enable AI
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        enabledCheckBox = new JCheckBox("Enable AI Generation");
        generationPanel.add(enabledCheckBox, gbc);

        // Auto Generate
        gbc.gridx = 0;
        gbc.gridy = 4;
        autoGenerateCheckBox = new JCheckBox("Auto-generate on commit dialog open");
        generationPanel.add(autoGenerateCheckBox, gbc);

        // 支持的文本文件扩展名
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        generationPanel.add(new JLabel("Text File Extensions:"), gbc);

        gbc.gridx = 1;
        textFileExtensionsField = new JTextField(".java,.kt,.xml,.groovy,.md,.txt,.properties", 25);
        generationPanel.add(textFileExtensionsField, gbc);

        // diff上下文尺寸
        gbc.gridx = 0;
        gbc.gridy = 6;
        generationPanel.add(new JLabel("Diff Context Size:"), gbc);

        gbc.gridx = 1;
        diffContextSizeSpinner = new JSpinner(new SpinnerNumberModel(3, 0, 20, 1));
        generationPanel.add(diffContextSizeSpinner, gbc);

        // 网络请求日志目录
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        generationPanel.add(new JLabel("Log Directory:"), gbc);

        gbc.gridx = 1;
        logDirectoryField = new JTextArea(3, 40);
        logDirectoryField.setEditable(false);
        logDirectoryField.setLineWrap(true);
        logDirectoryField.setWrapStyleWord(true);
        logDirectoryField.setToolTipText("Network request log directory path (read-only)");
        JScrollPane logDirScrollPane = new JScrollPane(logDirectoryField);
        logDirScrollPane.setBorder(logDirectoryField.getBorder());
        generationPanel.add(logDirScrollPane, gbc);

        return generationPanel;
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new BorderLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("File Filter Settings"));

        // 说明标签
        JLabel descriptionLabel = new JLabel(
            "<html>File Blacklist (one pattern per line):<br>" +
            "Files matching these patterns will be excluded from AI analysis.<br>" +
            "Supports both regex patterns and wildcard patterns.<br>" +
            "Regex examples: .*\\.key$, .*\\.pem$, .*config.*, .*\\.env$<br>" +
            "Wildcard examples: *.md, *.txt, config/*.json, src/**/*.java</html>"
        );
        descriptionLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 文件黑名单文本区域
        fileBlacklistTextArea = new JTextArea();
        fileBlacklistTextArea.setRows(6);
        fileBlacklistTextArea.setLineWrap(true);
        fileBlacklistTextArea.setWrapStyleWord(true);
        fileBlacklistTextArea.setToolTipText(
            "Enter regex patterns to exclude files from AI analysis.\n" +
            "One pattern per line. Examples:\n" +
            ".*\\.key$\n" +
            ".*\\.pem$\n" +
            ".*config.*\n" +
            ".*\\.env$"
        );

        JScrollPane scrollPane = new JScrollPane(fileBlacklistTextArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(400, 120));

        // 验证按钮面板
        JPanel validationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton validateButton = new JButton("Validate Patterns");
        validateButton.setToolTipText("Validate regex patterns in the blacklist");
        validateButton.addActionListener(e -> validateBlacklistPatterns());
        validationPanel.add(validateButton);

        filterPanel.add(descriptionLabel, BorderLayout.NORTH);
        filterPanel.add(scrollPane, BorderLayout.CENTER);
        filterPanel.add(validationPanel, BorderLayout.SOUTH);

        return filterPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        testConnectionButton = new JButton("Test Connection");
        buttonPanel.add(testConnectionButton);
        return buttonPanel;
    }

    private void setupEventHandlers() {
        testConnectionButton.addActionListener(e -> testConnection());
        enabledCheckBox.addActionListener(e -> updateUIState());
    }

    private void updateUIState() {
        boolean enabled = enabledCheckBox.isSelected();
        apiKeyField.setEnabled(enabled);
        endpointField.setEnabled(enabled);
        modelComboBox.setEnabled(enabled);
        maxTokensSpinner.setEnabled(enabled);
        temperatureSpinner.setEnabled(enabled);
        promptTemplateComboBox.setEnabled(enabled);
        autoGenerateCheckBox.setEnabled(enabled);
        fileBlacklistTextArea.setEnabled(enabled);
        testConnectionButton.setEnabled(enabled);
        // 日志目录字段始终保持只读状态，不受AI启用状态影响
    }

    private void testConnection() {
        // 尝试获取当前活动的项目
        final Project currentProject = getCurrentProject();

        if (currentProject == null) {
            // 如果没有项目，显示警告但继续测试
            int result = Messages.showYesNoDialog(
                    "No active project found. Test connection without project context?",
                    "Test Connection Warning",
                    Messages.getQuestionIcon()
            );
            if (result != Messages.YES) {
                return;
            }
        }

        // 禁用按钮，显示加载状态
        testConnectionButton.setEnabled(false);
        testConnectionButton.setText("Testing...");

        AISettings testSettings = getSettings();
        DeepSeekAPIClient client = new DeepSeekAPIClient(testSettings);

        // 异步测试连接
        client.testConnection().thenAccept(result -> {
            SwingUtilities.invokeLater(() -> {
                testConnectionButton.setEnabled(true);
                testConnectionButton.setText("Test Connection");

                if (result.isSuccess()) {
                    if (currentProject != null && currentProject.isDisposed() == false) {
                        IDENotificationUtil.notifyInfo(currentProject, "AI Connection Test", result.getMessage());
                    } else {
                        Messages.showInfoMessage(result.getMessage(), "AI Connection Test");
                    }
                } else {
                    String errorMessage = result.getMessage();
                    if (result.getDetails() != null && !result.getDetails().isEmpty()) {
                        errorMessage += "\n\n详细信息: " + result.getDetails();
                    }

                    if (currentProject != null && currentProject.isDisposed() == false) {
                        IDENotificationUtil.notifyError(currentProject, "AI Connection Test", errorMessage);
                    } else {
                        Messages.showErrorDialog(errorMessage, "AI Connection Test");
                    }
                }
            });
        }).exceptionally(error -> {
            SwingUtilities.invokeLater(() -> {
                testConnectionButton.setEnabled(true);
                testConnectionButton.setText("Test Connection");
                if (currentProject != null && currentProject.isDisposed() == false) {
                    IDENotificationUtil.notifyError(currentProject, "AI Connection Test", "Connection failed: " + error.getMessage());
                } else {
                    Messages.showErrorDialog("Connection failed: " + error.getMessage(), "AI Connection Test");
                }
            });
            return null;
        });
    }

    private Project getCurrentProject() {
        try {
            ProjectManager projectManager = ProjectManager.getInstance();
            Project[] openProjects = projectManager.getOpenProjects();
            if (openProjects.length > 0) {
                return openProjects[0];
            }
        } catch (Exception e) {
            // 忽略获取项目时的异常
        }
        return null;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setSettings(AISettings settings) {
        apiKeyField.setText(settings.getApiKey() != null ? settings.getApiKey() : "");
        endpointField.setText(settings.getApiEndpoint());
        modelComboBox.setSelectedItem(settings.getModel());
        maxTokensSpinner.setValue(settings.getMaxTokens());
        temperatureSpinner.setValue(settings.getTemperature());
        enabledCheckBox.setSelected(settings.isEnabled());
        autoGenerateCheckBox.setSelected(settings.isAutoGenerate());

        // 设置提示语模板选择
        String promptTemplate = settings.getPromptTemplate();
        if ("conventional_zh".equals(promptTemplate)) {
            promptTemplateComboBox.setSelectedIndex(0);
        } else if ("conventional_en".equals(promptTemplate)) {
            promptTemplateComboBox.setSelectedIndex(1);
        } else {
            // 默认选择中文
            promptTemplateComboBox.setSelectedIndex(0);
        }

        // 设置文件黑名单
        String fileBlacklist = settings.getFileBlacklist();
        if (fileBlacklist != null && !fileBlacklist.isEmpty()) {
            fileBlacklistTextArea.setText(fileBlacklist);
        } else {
            fileBlacklistTextArea.setText("");
        }

        // 设置支持的文本文件扩展名
        String textFileExtensions = settings.getTextFileExtensions();
        if (textFileExtensions != null && !textFileExtensions.isEmpty()) {
            textFileExtensionsField.setText(textFileExtensions);
        } else {
            textFileExtensionsField.setText(".java,.kt,.xml,.groovy,.md,.txt,.properties");
        }
        // 设置diff上下文尺寸
        diffContextSizeSpinner.setValue(settings.getDiffContextSize());

        updateUIState();
    }

    public AISettings getSettings() {
        AISettings settings = new AISettings();
        settings.setApiKey(apiKeyField.getText());
        settings.setApiEndpoint(endpointField.getText());
        settings.setModel((String) modelComboBox.getSelectedItem());
        settings.setMaxTokens((Integer) maxTokensSpinner.getValue());
        settings.setTemperature((Double) temperatureSpinner.getValue());
        settings.setEnabled(enabledCheckBox.isSelected());
        settings.setAutoGenerate(autoGenerateCheckBox.isSelected());

        // 获取提示语模板选择
        int selectedIndex = promptTemplateComboBox.getSelectedIndex();
        if (selectedIndex == 0) {
            settings.setPromptTemplate("conventional_zh");
        } else if (selectedIndex == 1) {
            settings.setPromptTemplate("conventional_en");
        } else {
            settings.setPromptTemplate("conventional_zh"); // 默认中文
        }

        // 获取文件黑名单
        settings.setFileBlacklist(fileBlacklistTextArea.getText());

        // 获取支持的文本文件扩展名
        settings.setTextFileExtensions(textFileExtensionsField.getText());
        // 获取diff上下文尺寸
        settings.setDiffContextSize((Integer) diffContextSizeSpinner.getValue());

        return settings;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * 初始化日志目录路径显示
     */
    private void initializeLogDirectory() {
        try {
            String logDirectoryPath = getLogDirectoryPath();
            logDirectoryField.setText(logDirectoryPath);
        } catch (Exception e) {
            logDirectoryField.setText("Failed to get log directory path");
        }
    }

    /**
     * 获取日志目录路径
     */
    private String getLogDirectoryPath() {
        return NetworkRequestLogger.getLogDirectory().toString();
    }

    /**
     * 验证黑名单正则表达式模式
     */
    private void validateBlacklistPatterns() {
        String blacklistConfig = fileBlacklistTextArea.getText();
        FileBlacklistFilter.ValidationResult result = FileBlacklistFilter.validateBlacklistConfig(blacklistConfig);

        if (result.isValid()) {
            Messages.showInfoMessage(
                "All regex patterns are valid!",
                "Blacklist Validation"
            );
        } else {
            Messages.showErrorDialog(
                "Invalid regex patterns found:\n\n" + result.getErrorMessage(),
                "Blacklist Validation Error"
            );
        }
    }
}
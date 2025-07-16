# AI Commit Message UI设计规范

## 1. 设计原则

### 1.1 设计理念
- **最小化改动**: 保持现有UI结构不变，只在必要位置添加AI功能
- **一致性**: 与IntelliJ IDEA原生界面风格保持一致
- **渐进式集成**: AI功能作为可选功能，根据设置动态显示
- **向后兼容**: 确保现有用户不受影响

### 1.2 设计目标
- 无缝集成到现有的commit dialog中
- 提供清晰的AI生成状态反馈
- 支持用户自定义和编辑AI生成内容
- 保持与现有功能的兼容性

## 2. 界面布局设计

### 2.1 现有界面分析

#### 2.1.1 当前CommitPanel布局
```
┌─────────────────────────────────────────────────────────────┐
│ Type of change: [feat ▼]                                    │
│ Scope of this change: [core]                                │
│ Short description: [AI generated subject...]                │
│                                                             │
│ Long description:                                            │
│ [AI generated detailed description...]                      │
│                                                             │
│ Breaking changes:                                            │
│ [AI generated breaking changes...]                          │
│                                                             │
│ Closed issues: [#123]                                       │
│ Skip CI: [✓] [approve]                                      │
└─────────────────────────────────────────────────────────────┘
```

#### 2.1.2 现有组件结构
- **typePanel**: 包含类型选择组件（ComboBox或RadioButton）
- **changeScope**: 范围输入框
- **shortDescription**: 简短描述输入框
- **longDescription**: 详细描述编辑器
- **breakingChanges**: 破坏性变更编辑器
- **closedIssues**: 关闭Issue输入框
- **skipCiComboBox**: 跳过CI选择框

### 2.2 AI功能集成方案

#### 2.2.1 推荐方案：在typePanel旁边添加AI按钮
```
┌─────────────────────────────────────────────────────────────┐
│ Type of change: [feat ▼] [🤖 AI Generate]                  │
│ Scope of this change: [core]                                │
│ Short description: [AI generated subject...]                │
│                                                             │
│ Long description:                                            │
│ [AI generated detailed description...]                      │
│                                                             │
│ Breaking changes:                                            │
│ [AI generated breaking changes...]                          │
│                                                             │
│ Closed issues: [#123]                                       │
│ Skip CI: [✓] [approve]                                      │
├─────────────────────────────────────────────────────────────┤
│ Status: ✓ AI generated successfully                         │
└─────────────────────────────────────────────────────────────┘
```

#### 2.2.2 备选方案
- **方案B**: 在shortDescription字段右侧添加按钮
- **方案C**: 在面板顶部添加工具栏
- **方案D**: 在现有按钮组中添加AI按钮

### 2.3 设置界面集成

#### 2.3.1 扩展现有设置界面
在现有的CentralSettingConfigurable中添加AI设置标签页：

```
┌─────────────────────────────────────────────────────────────┐
│                    GitCommitMessageHelper Settings          │
├─────────────────────────────────────────────────────────────┤
│ [General] [AI Settings]                                     │
├─────────────────────────────────────────────────────────────┤
│ AI Settings                                                 │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ API Configuration                                       │ │
│ │ API Key: [••••••••••••••••••••••••••••••••••••••••••••] │ │
│ │ Endpoint: [https://api.deepseek.com/v1/chat/completions]│ │
│ │ Model: [deepseek-chat ▼]                                │ │
│ └─────────────────────────────────────────────────────────┘ │
│                                                             │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │ Generation Settings                                     │ │
│ │ Max Tokens: [500] Temperature: [0.7]                    │ │
│ │ [✓] Enable AI Generation                                │ │
│ │ [ ] Auto-generate on commit dialog open                 │ │
│ └─────────────────────────────────────────────────────────┘ │
│                                                             │
│ [Test Connection] [Save] [Cancel]                          │
└─────────────────────────────────────────────────────────────┘
```

## 3. 组件设计规范

### 3.1 AI按钮设计

#### 3.1.1 按钮样式
```java
// AI按钮样式定义
public class AIButton extends JButton {
    public AIButton() {
        setText("🤖 AI Generate");
        setToolTipText("Generate commit message with AI");
        setIcon(Icons.AI_GENERATE);
        setPreferredSize(new Dimension(120, 30));

        // 使用IntelliJ的按钮样式
        setUI(new DarculaButtonUI());
    }
}
```

#### 3.1.2 按钮状态
```java
public enum AIButtonState {
    ENABLED("🤖 AI Generate", Color.BLACK, true),
    DISABLED("🤖 AI Generate", Color.GRAY, false),
    LOADING("⏳ Generating...", Color.BLUE, false),
    SUCCESS("✅ Generated", Color.GREEN, true),
    ERROR("❌ Failed", Color.RED, true);

    private final String text;
    private final Color color;
    private final boolean enabled;
}
```

#### 3.1.3 按钮位置
```java
// 在CommitPanel.form中添加AI按钮
// 位置：typePanel右侧，与现有组件对齐
private void addAIButtonToLayout() {
    // 创建水平布局容器
    JPanel typeContainer = new JPanel(new BorderLayout());
    typeContainer.add(typePanel, BorderLayout.CENTER);
    typeContainer.add(aiGenerateButton, BorderLayout.EAST);

    // 替换原有的typePanel
    mainPanel.remove(typePanel);
    mainPanel.add(typeContainer, typePanelConstraints);
}
```

### 3.2 状态指示器设计

#### 3.2.1 状态类型
```java
public enum AIStatusType {
    READY("Ready", Icons.READY, Color.GRAY),
    GENERATING("Generating...", Icons.LOADING, Color.BLUE),
    SUCCESS("Generated successfully", Icons.SUCCESS, Color.GREEN),
    ERROR("Generation failed", Icons.ERROR, Color.RED),
    WARNING("Warning", Icons.WARNING, Color.ORANGE);

    private final String text;
    private final Icon icon;
    private final Color color;
}
```

#### 3.2.2 状态显示组件
```java
public class AIStatusIndicator extends JPanel {
    private JLabel statusLabel;
    private JLabel iconLabel;
    private JProgressBar progressBar;

    public AIStatusIndicator() {
        setLayout(new BorderLayout(5, 0));
        setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        // 初始化组件
        iconLabel = new JLabel();
        statusLabel = new JLabel("Ready");
        progressBar = new JProgressBar();
        progressBar.setVisible(false);

        // 添加到面板
        add(iconLabel, BorderLayout.WEST);
        add(statusLabel, BorderLayout.CENTER);
        add(progressBar, BorderLayout.EAST);

        // 初始状态
        updateStatus(AIStatusType.READY);
    }

    public void updateStatus(AIStatusType status) {
        SwingUtilities.invokeLater(() -> {
            iconLabel.setIcon(status.getIcon());
            statusLabel.setText(status.getText());
            statusLabel.setForeground(status.getColor());

            if (status == AIStatusType.GENERATING) {
                progressBar.setVisible(true);
                progressBar.setIndeterminate(true);
            } else {
                progressBar.setVisible(false);
            }
        });
    }
}
```

### 3.3 设置面板设计

#### 3.3.1 API配置面板
```java
public class APIConfigPanel extends JPanel {
    private JTextField apiKeyField;
    private JTextField endpointField;
    private JComboBox<String> modelComboBox;
    private JButton testConnectionButton;

    public APIConfigPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("API Configuration"));

        // 添加配置字段
        addConfigFields();

        // 设置事件处理
        setupEventHandlers();
    }

    private void addConfigFields() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // API Key
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("API Key:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        apiKeyField = new JPasswordField(40);
        add(apiKeyField, gbc);

        // Endpoint
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        add(new JLabel("Endpoint:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        endpointField = new JTextField("https://api.deepseek.com/v1/chat/completions", 40);
        add(endpointField, gbc);

        // Model
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        add(new JLabel("Model:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        modelComboBox = new JComboBox<>(new String[]{"deepseek-chat", "deepseek-reasoner"});
        add(modelComboBox, gbc);

        // Test Connection Button
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        testConnectionButton = new JButton("Test Connection");
        add(testConnectionButton, gbc);
    }
}
```

#### 3.3.2 生成配置面板
```java
public class GenerationConfigPanel extends JPanel {
    private JSpinner maxTokensSpinner;
    private JSpinner temperatureSpinner;
    private JCheckBox enabledCheckBox;
    private JCheckBox autoGenerateCheckBox;

    public GenerationConfigPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Generation Settings"));

        // 添加配置字段
        addConfigFields();

        // 设置事件处理
        setupEventHandlers();
    }

    private void addConfigFields() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Max Tokens
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Max Tokens:"), gbc);

        gbc.gridx = 1;
        maxTokensSpinner = new JSpinner(new SpinnerNumberModel(500, 100, 2000, 50));
        add(maxTokensSpinner, gbc);

        // Temperature
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Temperature:"), gbc);

        gbc.gridx = 1;
        temperatureSpinner = new JSpinner(new SpinnerNumberModel(0.7, 0.0, 2.0, 0.1));
        add(temperatureSpinner, gbc);

        // Enable AI
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        enabledCheckBox = new JCheckBox("Enable AI Generation");
        add(enabledCheckBox, gbc);

        // Auto Generate
        gbc.gridx = 0; gbc.gridy = 3;
        autoGenerateCheckBox = new JCheckBox("Auto-generate on commit dialog open");
        add(autoGenerateCheckBox, gbc);
    }
}
```

## 4. 交互设计

### 4.1 用户操作流程

#### 4.1.1 首次使用流程
1. 用户打开commit dialog（现有流程不变）
2. 发现AI按钮（如果已启用）或提示配置AI功能
3. 点击设置按钮进入AI配置
4. 配置API密钥和参数
5. 测试连接
6. 启用AI功能
7. 返回commit dialog使用AI生成

#### 4.1.2 日常使用流程
1. 用户打开commit dialog
2. 点击"AI Generate"按钮
3. 显示生成进度指示器
4. AI分析代码变更并生成commit message
5. 自动填充到现有表单字段
6. 用户确认或手动编辑
7. 正常提交流程（现有流程不变）

### 4.2 错误处理

#### 4.2.1 错误类型和显示
```java
public class AIErrorHandler {
    public static void handleError(Throwable error, JComponent parent) {
        String title = "AI Generation Error";
        String message;

        if (error instanceof APIException) {
            message = "API Error: " + error.getMessage();
        } else if (error instanceof NetworkException) {
            message = "Network Error: Please check your internet connection.";
        } else if (error instanceof AuthenticationException) {
            message = "Authentication Error: Please check your API key.";
        } else {
            message = "Unexpected Error: " + error.getMessage();
        }

        JOptionPane.showMessageDialog(
            parent,
            message,
            title,
            JOptionPane.ERROR_MESSAGE
        );
    }
}
```

#### 4.2.2 降级处理
```java
public class AIDegradationHandler {
    public static void handleDegradation(CommitPanel panel) {
        // 隐藏AI按钮
        panel.getAIGenerateButton().setVisible(false);

        // 显示降级提示
        panel.getAIStatusIndicator().updateStatus(
            AIStatusType.WARNING,
            "AI feature temporarily unavailable"
        );

        // 保持现有功能可用
        // 用户可以继续手动填写commit message
    }
}
```

### 4.3 快捷键支持

#### 4.3.1 快捷键定义
- **Ctrl+Shift+A**: 打开AI设置
- **Ctrl+Shift+G**: 生成commit message
- **Ctrl+Shift+R**: 重新生成
- **Ctrl+Shift+H**: 查看AI帮助

#### 4.3.2 快捷键实现
```java
public class AICommitAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            // 打开commit dialog并触发AI生成
            openCommitDialogWithAI(project);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        // 只有在AI功能启用时才启用快捷键
        e.getPresentation().setEnabled(isAIEnabled());
    }
}
```

## 5. 响应式设计

### 5.1 窗口大小适配
```java
public class ResponsiveLayout {
    public static void adaptToWindowSize(JComponent component, Dimension size) {
        // 根据窗口大小调整组件布局
        if (size.width < 600) {
            // 小窗口：垂直布局
            applyVerticalLayout(component);
        } else {
            // 大窗口：水平布局
            applyHorizontalLayout(component);
        }
    }
}
```

### 5.2 高DPI支持
```java
public class HighDPISupport {
    public static void setupHighDPI() {
        // 设置高DPI缩放
        System.setProperty("sun.java2d.uiScale", "1.0");

        // 图标缩放
        UIManager.put("Icon.scale", 1.5);
    }
}
```

## 6. 主题适配

### 6.1 深色主题支持
```java
public class ThemeAdapter {
    public static void applyTheme(JComponent component) {
        if (isDarkTheme()) {
            component.setBackground(UIUtil.getPanelBackground());
            component.setForeground(UIUtil.getLabelForeground());

            // 适配深色主题的按钮样式
            if (component instanceof JButton) {
                component.setUI(new DarculaButtonUI());
            }
        }
    }

    private static boolean isDarkTheme() {
        return UIUtil.isUnderDarcula() || UIUtil.isUnderIntelliJLaF();
    }
}
```

### 6.2 颜色方案
```java
public class ColorScheme {
    // 浅色主题
    public static final Color LIGHT_BACKGROUND = new Color(255, 255, 255);
    public static final Color LIGHT_FOREGROUND = new Color(51, 51, 51);

    // 深色主题
    public static final Color DARK_BACKGROUND = new Color(43, 43, 43);
    public static final Color DARK_FOREGROUND = new Color(187, 187, 187);

    // 状态颜色
    public static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    public static final Color ERROR_COLOR = new Color(244, 67, 54);
    public static final Color WARNING_COLOR = new Color(255, 193, 7);
    public static final Color INFO_COLOR = new Color(33, 150, 243);
}
```

## 7. 可访问性设计

### 7.1 屏幕阅读器支持
```java
public class AccessibilitySupport {
    public static void setupAccessibility(JComponent component) {
        // 设置描述性文本
        component.getAccessibleContext().setAccessibleDescription(
            "AI commit message generation component"
        );

        // 设置键盘导航
        component.setFocusTraversalKeysEnabled(true);
    }
}
```

### 7.2 高对比度支持
```java
public class HighContrastSupport {
    public static void applyHighContrast(JComponent component) {
        if (isHighContrastMode()) {
            // 使用高对比度颜色
            component.setBackground(Color.WHITE);
            component.setForeground(Color.BLACK);
            component.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        }
    }
}
```

## 8. 国际化支持

### 8.1 多语言支持
```properties
# 英文资源文件 (ai_en.properties)
ai.generate.button=AI Generate
ai.settings.title=AI Settings
ai.status.ready=Ready to generate
ai.status.generating=Generating commit message...
ai.status.success=Generated successfully
ai.status.error=Generation failed

# 中文资源文件 (ai_zh.properties)
ai.generate.button=AI生成
ai.settings.title=AI设置
ai.status.ready=准备生成
ai.status.generating=正在生成提交信息...
ai.status.success=生成成功
ai.status.error=生成失败
```

### 8.2 本地化适配
```java
public class LocalizationAdapter {
    public static void adaptToLocale(Locale locale) {
        // 根据语言调整文本长度
        if (locale.getLanguage().equals("zh")) {
            // 中文：增加按钮宽度
            adjustButtonWidthForChinese();
        } else if (locale.getLanguage().equals("ja")) {
            // 日文：调整字体大小
            adjustFontSizeForJapanese();
        }
    }
}
```

## 9. 性能优化

### 9.1 UI性能
```java
public class UIPerformanceOptimizer {
    public static void optimizeUI(JComponent component) {
        // 使用异步更新UI
        SwingUtilities.invokeLater(() -> {
            component.revalidate();
            component.repaint();
        });

        // 避免阻塞主线程
        if (SwingUtilities.isEventDispatchThread()) {
            // 在后台线程执行耗时操作
            CompletableFuture.runAsync(() -> {
                // 耗时操作
            });
        }
    }
}
```

### 9.2 内存管理
```java
public class MemoryManager {
    public static void cleanupResources() {
        // 清理临时数据
        clearTemporaryData();

        // 释放不需要的资源
        releaseUnusedResources();

        // 强制垃圾回收（谨慎使用）
        System.gc();
    }
}
```

## 10. 测试规范

### 10.1 UI测试
```java
@Test
public void testAIGenerateButtonClick() {
    // 模拟按钮点击
    aiGenerateButton.doClick();

    // 验证状态变化
    assertEquals(AIButtonState.LOADING, aiGenerateButton.getState());
    assertTrue(aiStatusIndicator.isVisible());
    assertTrue(progressBar.isVisible());
}

@Test
public void testAISettingsPanel() {
    // 测试设置面板
    AISettingsPanel panel = new AISettingsPanel(settings);

    // 验证组件存在
    assertNotNull(panel.getApiKeyField());
    assertNotNull(panel.getEndpointField());
    assertNotNull(panel.getModelComboBox());

    // 测试设置保存
    panel.getApiKeyField().setText("test-key");
    panel.saveSettings();
    assertEquals("test-key", settings.getAISettings().getApiKey());
}
```

### 10.2 主题测试
```java
@Test
public void testThemeAdaptation() {
    // 切换主题
    ThemeManager.getInstance().setTheme(Theme.DARK);

    // 验证颜色适配
    assertEquals(DARK_BACKGROUND, panel.getBackground());
    assertEquals(DARK_FOREGROUND, panel.getForeground());

    // 验证按钮样式
    assertTrue(panel.getAIGenerateButton().getUI() instanceof DarculaButtonUI);
}
```

---

**文档版本**: v2.0
**创建日期**: 2024年12月
**更新日期**: 2024年12月
**UI设计师**: 设计团队
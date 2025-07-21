# AI面板关闭Issue功能

## 功能概述

在AI生成提交信息面板中新增了关闭Issue的文本输入框功能，用户可以在AI生成提交信息时手动指定要关闭的Issue编号，系统会自动将Issue信息添加到生成的提交信息中。

## 功能特性

### 1. 关闭Issue输入框
- **位置**: AI生成面板顶部，在AI生成按钮下方
- **标签**: 使用本地化文本 "Closed Issues:" (中文: "关闭问题:")
- **输入格式**: 支持多个Issue编号，用逗号分隔 (例如: #123, #456)
- **提示信息**: 鼠标悬停时显示使用说明

### 2. 智能Issue处理
- **自动检测**: 如果AI生成的内容中已包含Closes部分，会自动替换为用户输入的内容
- **格式保持**: 保持Conventional Commits格式的一致性
- **模板支持**: 支持使用Velocity模板进行格式化

### 3. 集成功能
- **CommitMessage集成**: 在生成最终的CommitMessage时包含Issue信息
- **CommitTemplate集成**: 在获取CommitTemplate时包含Issue信息
- **本地化支持**: 支持多语言界面

## 技术实现

### 修改的文件

1. **AIGeneratePanel.java**
   - 新增 `closedIssuesTextField` 和 `closedIssuesLabel` 字段
   - 修改构造函数，添加 `GitCommitMessageHelperSettings` 参数
   - 在 `initComponents()` 方法中添加Issue输入面板
   - 修改 `getAIContent()` 方法，支持Issue拼接
   - 新增 `getClosedIssues()` 和 `setClosedIssues()` 方法

2. **CommitDialog.java**
   - 修改AIGeneratePanel构造函数调用，传递settings参数
   - 修改 `getCommitMessage()` 方法，包含Issue信息
   - 修改 `getCommitMessageTemplate()` 方法，包含Issue信息

3. **CommitDialogTest.java**
   - 新增 `testAIClosedIssuesIntegration()` 测试方法

### 核心逻辑

#### Issue拼接逻辑
```java
public String getAIContent() {
    String aiContent = aiContentTextArea.getText().trim();
    String closedIssues = closedIssuesTextField.getText().trim();

    if (StringUtils.isEmpty(aiContent)) {
        return "";
    }

    if (StringUtils.isNotEmpty(closedIssues)) {
        // 如果AI内容已经包含Closes部分，则替换它
        if (aiContent.contains("Closes:") || aiContent.contains("closes:")) {
            // 移除现有的Closes部分
            // ... 处理逻辑
        }

        // 添加新的Closes部分
        return aiContent + "\n\nCloses: " + closedIssues;
    }

    return aiContent;
}
```

#### UI布局
```java
// 关闭issue输入面板
JPanel closedIssuesPanel = new JPanel(new BorderLayout());
closedIssuesLabel = new JLabel(PluginBundle.get("commit.panel.closes.field"));
closedIssuesTextField = new JTextField();

closedIssuesPanel.add(closedIssuesLabel, BorderLayout.WEST);
closedIssuesPanel.add(closedIssuesTextField, BorderLayout.CENTER);
```

## 使用方法

1. 打开Git提交对话框
2. 切换到"AI Generate"选项卡
3. 在"Closes Issues"输入框中输入要关闭的Issue编号
   - 单个Issue: `#123`
   - 多个Issue: `#123, #456`
4. 点击"AI Generate"按钮生成提交信息
5. 生成的提交信息会自动包含Issue关闭信息

## 示例

### 输入
- AI生成内容: `feat: add new user authentication feature`
- 关闭Issue: `#123, #456`

### 输出
```
feat: add new user authentication feature

Closes: #123, #456
```

## 兼容性

- 向后兼容：不影响现有的AI生成功能
- 模板兼容：支持现有的Velocity模板系统
- 本地化兼容：支持多语言界面
- 格式兼容：符合Conventional Commits规范

## 注意事项

1. Issue编号格式建议使用 `#` 前缀
2. 多个Issue之间用逗号分隔
3. 如果AI生成的内容中已包含Closes信息，会被用户输入的内容替换
4. 该功能仅在AI生成面板中可用，不影响手动构建面板
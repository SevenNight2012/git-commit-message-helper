# AI提示语模板选择功能

## 功能概述

在AI设置面板中新增了提示语模板选择功能，用户可以在中文和英文两种提示语之间自由选择，用于生成Git commit message。

## 功能特性

### 1. 提示语模板选项
- **中文提示语 (conventional_zh)**: 默认选项，使用中文生成commit message
- **英文提示语 (conventional_en)**: 使用英文生成commit message

### 2. 默认设置
- 新用户默认使用中文提示语
- 设置会被自动保存和恢复

### 3. 使用方法
1. 打开IDE设置
2. 找到"Git Commit Message Helper"设置
3. 在AI设置面板中选择"Prompt Template"
4. 选择中文或英文提示语
5. 点击"Apply"保存设置

## 技术实现

### 修改的文件

1. **AISettings.java**
   - 新增 `promptTemplate` 字段
   - 默认值设为 `"conventional_zh"`

2. **AISettingsPanel.java**
   - 新增提示语模板选择下拉框
   - 在UI中显示中文和英文选项
   - 处理设置的保存和加载

3. **AIGeneratorService.java**
   - 修改 `generateCommitMessage` 方法
   - 使用设置中的提示语模板

4. **AIGeneratePanel.java**
   - 修改 `startGeneration` 方法
   - 传递正确的提示语模板

5. **PromptBuilder.java**
   - 更新默认方法使用中文提示语

### 提示语模板内容

#### 中文提示语 (conventional_zh)
```
你是一个专业的Git commit message生成助手。请根据以下代码变更信息，生成一个符合Conventional Commits规范的commit message。

代码变更信息：
- 变更文件：{0}
- 变更类型：{1}
- 代码差异：
{2}

请生成包含以下部分的commit message：
Type: 提交类型（feat/fix/docs/style/refactor/test/chore）
Scope: 影响范围（可选）
Subject: 简短描述（50字符以内）
Body: 详细描述（可选）
Breaking Changes: 破坏性变更（可选）
Closes: 关闭的Issue（可选）

要求：
- 使用中文
- 遵循Conventional Commits规范
- 描述准确、简洁、清晰
- 如果有破坏性变更，请明确标注
- 输出格式应与现有模板格式保持一致，最重要的一点请不要有任何多余的文字，严格按照格式输出commit message即可
```

#### 英文提示语 (conventional_en)
```
You are a professional Git commit message assistant. Based on the following code changes, generate a commit message that conforms to the Conventional Commits specification.

Code changes:
- Changed files: {0}
- Change type: {1}
- Diff content:
{2}

Please generate a commit message with the following parts:
Type: (feat/fix/docs/style/refactor/test/chore)
Scope: (optional)
Subject: short description (max 50 chars)
Body: detailed description (optional)
Breaking Changes: (optional)
Closes: (optional)

Requirements:
- Use English
- Follow Conventional Commits
- Be accurate, concise, clear
- If breaking changes, mark clearly
- Output format should match the template.The most important thing is that please do not have any extra text, and just output the commit message in strict accordance with the format
```

## 测试

新增了以下测试用例：
- `AISettingsTest.java`: 测试提示语模板的设置和获取
- `PromptBuilderTest.java`: 测试不同提示语模板的生成

## 兼容性

- 向后兼容：现有用户的设置会自动使用默认的中文提示语
- 设置持久化：用户的选择会被自动保存到配置文件中
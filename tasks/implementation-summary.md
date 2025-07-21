# AI提示语模板选择功能实现总结

## 实现概述

成功在AI设置面板中增加了提示语选项功能，用户可以在中文和英文两种提示语之间自由选择，用于生成Git commit message。

## 实现的功能

### 1. 新增提示语模板选择
- 在AI设置面板中添加了"Prompt Template"下拉选择框
- 提供两个选项：
  - `conventional_zh - 中文提示语` (默认)
  - `conventional_en - 英文提示语`

### 2. 默认设置
- 新用户默认使用中文提示语 (`conventional_zh`)
- 设置会被自动保存到配置文件中
- 向后兼容：现有用户会自动使用默认的中文提示语

### 3. 动态提示语选择
- 在AI生成commit message时，会根据用户选择的提示语模板进行网络请求
- 支持运行时切换提示语模板

## 修改的文件

### 1. `src/main/java/com/fulinlin/model/AISettings.java`
- 新增 `promptTemplate` 字段，默认值为 `"conventional_zh"`
- 新增 `getPromptTemplate()` 和 `setPromptTemplate()` 方法

### 2. `src/main/java/com/fulinlin/ui/setting/AISettingsPanel.java`
- 新增 `promptTemplateComboBox` 下拉选择框
- 在UI中添加"Prompt Template"标签和选择控件
- 修改 `setSettings()` 和 `getSettings()` 方法处理提示语模板的保存和加载
- 在 `updateUIState()` 中包含提示语模板控件的启用/禁用状态

### 3. `src/main/java/com/fulinlin/utils/AIGeneratorService.java`
- 修改 `generateCommitMessage()` 方法
- 优先使用设置中的提示语模板，如果没有指定则使用传入的templateKey

### 4. `src/main/java/com/fulinlin/ui/commit/AIGeneratePanel.java`
- 修改 `startGeneration()` 方法
- 使用 `aiSettings.getPromptTemplate()` 获取用户选择的提示语模板

### 5. `src/main/java/com/fulinlin/utils/PromptBuilder.java`
- 更新默认 `buildPrompt()` 方法使用中文提示语
- 保持现有的中英文提示语模板内容不变

## 新增的测试文件

### 1. `src/test/java/com/fulinlin/model/AISettingsTest.java`
- 测试默认提示语模板是否为中文
- 测试提示语模板的设置和获取功能
- 测试提示语模板与其他设置的兼容性

### 2. `src/test/java/com/fulinlin/utils/PromptBuilderTest.java`
- 测试中文提示语模板的生成
- 测试英文提示语模板的生成
- 测试默认提示语是否为中文

## 技术特点

### 1. 向后兼容
- 现有用户的设置会自动使用默认的中文提示语
- 不会影响现有的功能

### 2. 设置持久化
- 用户的选择会被自动保存到 `GitCommitMessageHelperSettings` 中
- 重启IDE后设置会被自动恢复

### 3. 动态切换
- 用户可以在设置中随时切换提示语模板
- 切换后立即生效，无需重启IDE

### 4. 错误处理
- 如果提示语模板设置无效，会使用默认的中文提示语
- 保持了原有的错误处理机制

## 使用方法

1. 打开IDE设置 (File → Settings)
2. 找到 "Git Commit Message Helper" 设置
3. 在 "AI Settings" 面板中找到 "Prompt Template" 选项
4. 选择 "conventional_zh - 中文提示语" 或 "conventional_en - 英文提示语"
5. 点击 "Apply" 保存设置
6. 在commit对话框中，AI生成功能会使用选择的提示语模板

## 测试结果

- ✅ `AISettingsTest` 测试通过
- ✅ `PromptBuilderTest` 测试通过
- ✅ 编译成功，无语法错误
- ✅ 向后兼容性验证通过

## 总结

成功实现了AI提示语模板选择功能，用户现在可以根据自己的偏好选择中文或英文提示语来生成Git commit message。该功能具有良好的用户体验、向后兼容性和错误处理机制。
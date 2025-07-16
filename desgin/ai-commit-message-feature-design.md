# AI自动生成Commit Message功能设计文档

## 1. 功能概述

### 1.1 功能目标
在现有的Git Commit Message Helper插件基础上，新增AI自动生成commit message功能，利用DeepSeek的OpenAPI服务，分析本地代码变更内容，自动生成符合规范的commit message，提升开发效率。

### 1.2 核心价值
- **提升效率**：减少手动编写commit message的时间
- **提高质量**：AI生成的commit message更加规范和准确
- **保持一致性**：确保团队commit message风格统一
- **智能分析**：基于代码变更内容进行智能分析

### 1.3 设计原则
- **最小化改动**：保持现有UI和功能不变，只添加AI功能
- **向后兼容**：确保现有用户不受影响
- **渐进式集成**：AI功能作为可选功能，用户可选择启用或禁用
- **保持一致性**：AI生成的格式与现有模板格式保持一致

## 2. 功能架构

### 2.1 整体架构
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   IntelliJ IDE  │    │   Plugin Core   │    │   DeepSeek API  │
│                 │    │                 │    │                 │
│ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │
│ │ Git Changes │ │───▶│ │ AI Generator│ │───▶│ │ AI Service  │ │
│ │ Analysis    │ │    │ │ Service     │ │    │ │             │ │
│ └─────────────┘ │    │ └─────────────┘ │    │ └─────────────┘ │
│                 │    │                 │    │                 │
│ ┌─────────────┐ │    │ ┌─────────────┐ │    │                 │
│ │ Commit UI   │ │◀───│ │ Template    │ │    │                 │
│ │ (Enhanced)  │ │    │ │ Engine      │ │    │                 │
│ └─────────────┘ │    │ └─────────────┘ │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 2.2 核心组件
1. **AI生成服务** (AIGeneratorService)
2. **代码变更分析器** (CodeChangeAnalyzer)
3. **AI设置管理器** (AISettingsManager)
4. **API客户端** (DeepSeekAPIClient)
5. **UI增强组件** (AI功能按钮和状态指示器)

## 3. 详细功能设计

### 3.1 AI设置配置

#### 3.1.1 设置项
- **API Key**: DeepSeek API密钥
- **API Endpoint**: API服务地址（默认：https://api.deepseek.com/v1/chat/completions）
- **Model**: 使用的AI模型（默认：deepseek-chat）
- **Max Tokens**: 最大生成token数（默认：500）
- **Temperature**: 生成温度（默认：0.7）
- **Enable AI**: 是否启用AI功能（默认：false）
- **Auto Generate**: 是否自动生成（默认：false）

#### 3.1.2 设置界面集成
- **方案A（推荐）**: 在现有的CentralSettingConfigurable中添加AI设置标签页
- **方案B**: 创建独立的AISettingsConfigurable，作为子配置项
- 保持与现有设置界面风格一致
- 使用现有的配置管理机制

### 3.2 代码变更分析

#### 3.2.1 分析内容
- **文件变更列表**: 新增、修改、删除的文件
- **代码差异**: 具体的代码变更内容（diff）
- **变更类型**: 功能新增、bug修复、重构、文档更新等
- **影响范围**: 变更影响的模块或组件

#### 3.2.2 分析策略
- 利用IntelliJ的Git集成API获取变更信息
- 解析Git diff输出
- 识别变更的文件类型（Java、XML、配置文件等）
- 分析变更的语义（方法新增、类修改、配置变更等）
- 提取关键信息（类名、方法名、变量名等）

### 3.3 AI提示词设计

#### 3.3.1 基础提示词模板
```
你是一个专业的Git commit message生成助手。请根据以下代码变更信息，生成一个符合Conventional Commits规范的commit message。

代码变更信息：
- 变更文件：{file_list}
- 变更类型：{change_type}
- 代码差异：{diff_content}

请生成包含以下部分的commit message：
1. Type: 提交类型（feat/fix/docs/style/refactor/test/chore）
2. Scope: 影响范围（可选）
3. Subject: 简短描述（50字符以内）
4. Body: 详细描述（可选）
5. Breaking Changes: 破坏性变更（可选）
6. Closes: 关闭的Issue（可选）

要求：
- 使用英文
- 遵循Conventional Commits规范
- 描述准确、简洁、清晰
- 如果有破坏性变更，请明确标注
- 输出格式应与现有模板格式保持一致
```

#### 3.3.2 上下文增强
- 包含项目结构信息
- 包含相关文件的历史变更
- 包含当前分支信息
- 包含相关的Issue或PR信息

### 3.4 UI界面设计

#### 3.4.1 最小化改动方案
**在现有CommitPanel基础上添加AI功能按钮**：

```
现有布局保持不变，在合适位置添加AI按钮：

┌─────────────────────────────────────────────────────────────┐
│ Type: [feat ▼] Scope: [core] [🤖 AI Generate]              │
│ Subject: [AI generated subject...]                          │
│                                                             │
│ Body:                                                        │
│ [AI generated detailed description...]                      │
│                                                             │
│ Breaking Changes: [ ]                                       │
│ Closes: [#123]                                              │
│ Skip CI: [✓] [approve]                                      │
├─────────────────────────────────────────────────────────────┤
│ Status: ✓ AI generated successfully                         │
└─────────────────────────────────────────────────────────────┘
```

#### 3.4.2 具体实现方案
1. **在CommitPanel.form中添加AI按钮**：
   - 在typePanel旁边添加"AI Generate"按钮
   - 添加状态指示器显示生成状态
   - 保持现有布局结构不变

2. **按钮位置选择**：
   - 方案A：在typePanel右侧添加按钮
   - 方案B：在shortDescription字段右侧添加按钮
   - 方案C：在面板顶部添加工具栏

3. **状态显示**：
   - 在现有字段下方添加状态栏
   - 显示生成进度和结果
   - 支持错误信息显示

### 3.5 工作流程

#### 3.5.1 用户操作流程
1. 用户打开commit dialog（现有流程不变）
2. 点击"AI Generate"按钮
3. 显示生成进度指示器
4. AI分析代码变更并生成commit message
5. 自动填充到现有表单字段
6. 用户确认或手动编辑
7. 正常提交流程（现有流程不变）

#### 3.5.2 技术实现流程
1. 用户点击AI按钮
2. 获取当前项目的Git变更信息
3. 构建AI提示词
4. 调用DeepSeek API
5. 解析AI响应
6. 填充到现有CommitTemplate对象
7. 更新UI显示

## 4. 技术实现方案

### 4.1 核心类设计

#### 4.1.1 AIGeneratorService
```java
public class AIGeneratorService {
    private final DeepSeekAPIClient apiClient;
    private final CodeChangeAnalyzer analyzer;
    private final PromptBuilder promptBuilder;

    public CompletableFuture<CommitTemplate> generateCommitMessage(Project project) {
        return CompletableFuture.supplyAsync(() -> {
            // 分析代码变更
            CodeChangeInfo changeInfo = analyzer.analyzeChanges(project);

            // 构建提示词
            String prompt = promptBuilder.buildPrompt(changeInfo);

            // 调用AI API
            String response = apiClient.generateMessage(prompt).get();

            // 解析响应并转换为CommitTemplate
            return parseToCommitTemplate(response);
        });
    }
}
```

#### 4.1.2 现有类增强
```java
// 在CommitPanel中添加AI功能
public class CommitPanel {
    // 现有字段...
    private JButton aiGenerateButton;
    private JLabel aiStatusLabel;
    private AIGeneratorService aiGeneratorService;

    // 在构造函数中初始化AI功能
    public CommitPanel(Project project, GitCommitMessageHelperSettings settings, CommitTemplate commitMessageTemplate) {
        // 现有初始化代码...

        // 初始化AI功能
        if (settings.getAISettings().isEnabled()) {
            initAIFeatures();
        }
    }

    private void initAIFeatures() {
        // 初始化AI按钮和事件处理
        aiGenerateButton.addActionListener(e -> generateWithAI());
    }

    private void generateWithAI() {
        // AI生成逻辑
        aiGeneratorService.generateCommitMessage(project)
            .thenAccept(this::updateCommitTemplate)
            .exceptionally(this::handleAIError);
    }
}
```

### 4.2 数据模型扩展

#### 4.2.1 扩展现有设置模型
```java
// 在GitCommitMessageHelperSettings中添加AI设置
public class GitCommitMessageHelperSettings {
    // 现有字段...
    private AISettings aiSettings;

    // 现有方法...
    public AISettings getAISettings() {
        return aiSettings;
    }

    public void setAISettings(AISettings aiSettings) {
        this.aiSettings = aiSettings;
    }
}

// 新增AI设置模型
public class AISettings {
    private String apiKey;
    private String apiEndpoint;
    private String model;
    private int maxTokens;
    private double temperature;
    private boolean enabled;
    private boolean autoGenerate;

    // getters and setters
}
```

#### 4.2.2 新增数据模型
```java
public class CodeChangeInfo {
    private List<FileChange> changedFiles;
    private String diffContent;
    private ChangeType changeType;
    private String scope;
    private List<String> relatedIssues;
}

public class FileChange {
    private String filePath;
    private ChangeType changeType;
    private String diffContent;
}
```

### 4.3 配置存储

#### 4.3.1 扩展现有配置
- 使用现有的GitCommitMessageHelperSettings存储机制
- 在现有配置文件中添加AI设置节点
- 保持配置格式的一致性

#### 4.3.2 配置结构
```xml
<application>
    <component name="GitCommitMessageHelperSettings">
        <!-- 现有配置 -->
        <option name="centralSettings">
            <!-- 现有中央设置 -->
        </option>
        <option name="dateSettings">
            <!-- 现有数据设置 -->
        </option>
        <!-- 新增AI设置 -->
        <option name="aiSettings">
            <AISettings>
                <option name="apiKey" value="encrypted_key" />
                <option name="apiEndpoint" value="https://api.deepseek.com/v1/chat/completions" />
                <option name="model" value="deepseek-chat" />
                <option name="maxTokens" value="500" />
                <option name="temperature" value="0.7" />
                <option name="enabled" value="false" />
                <option name="autoGenerate" value="false" />
            </AISettings>
        </option>
    </component>
</application>
```

## 5. 用户体验设计

### 5.1 交互流程
1. **首次使用**:
   - AI功能默认禁用
   - 用户需要手动启用并配置API设置
   - 提供配置向导

2. **日常使用**:
   - 一键生成commit message
   - 实时状态反馈
   - 支持手动编辑和优化

3. **错误处理**:
   - 友好的错误提示
   - 降级到手动模式
   - 保持现有功能可用

### 5.2 界面设计原则
- **保持一致性**: 与现有界面风格保持一致
- **最小化改动**: 只在必要位置添加AI功能
- **渐进式显示**: AI功能根据设置动态显示/隐藏
- **响应式反馈**: 提供清晰的状态指示

### 5.3 错误处理
- **API连接失败**: 显示错误信息，不影响现有功能
- **生成失败**: 提供重试选项，保持表单状态
- **网络超时**: 自动重试机制，用户可取消
- **配置错误**: 引导用户检查设置

## 6. 安全考虑

### 6.1 数据安全
- **API密钥加密存储**: 使用IntelliJ的加密机制
- **本地代码分析**: 代码变更信息在本地处理
- **选择性上传**: 用户可选择是否上传代码内容
- **数据清理**: 支持清除历史记录

### 6.2 隐私保护
- **用户可控**: 用户可选择启用/禁用AI功能
- **透明处理**: 明确说明数据处理方式
- **符合规范**: 遵循GDPR等隐私保护要求
- **最小化收集**: 只收集必要的变更信息

## 7. 测试策略

### 7.1 单元测试
- **API客户端测试**: 模拟DeepSeek API响应
- **代码分析器测试**: 测试各种代码变更场景
- **提示词生成测试**: 验证提示词构建逻辑
- **设置管理测试**: 测试配置存储和读取

### 7.2 集成测试
- **端到端测试**: 测试完整的AI生成流程
- **UI交互测试**: 测试按钮点击和状态更新
- **错误处理测试**: 测试各种异常情况
- **性能测试**: 测试响应时间和资源使用

### 7.3 兼容性测试
- **现有功能测试**: 确保AI功能不影响现有功能
- **配置兼容性**: 测试新旧配置格式兼容性
- **UI兼容性**: 测试在不同主题下的显示效果

## 8. 发布计划

### 8.1 开发阶段
- **Phase 1**: 基础架构和API集成（2周）
- **Phase 2**: UI集成和功能实现（2周）
- **Phase 3**: 测试和优化（1周）
- **Phase 4**: 文档和发布准备（1周）

### 8.2 发布策略
- **渐进式发布**: 先发布Beta版本供内部测试
- **功能开关**: AI功能默认禁用，用户可选择启用
- **向后兼容**: 确保现有用户不受影响
- **用户反馈**: 收集用户反馈并持续优化

## 9. 风险评估

### 9.1 技术风险
- **API服务稳定性**: 实现降级方案，保持现有功能可用
- **网络连接问题**: 实现重试机制，提供离线模式
- **代码分析准确性**: 持续优化算法，提供手动编辑选项

### 9.2 业务风险
- **API成本控制**: 实现使用限制，提供成本监控
- **用户接受度**: 提供开关选项，保持原有功能
- **竞争对手功能**: 持续创新，提供差异化功能

### 9.3 缓解措施
- **功能开关**: AI功能可完全禁用
- **降级方案**: 失败时自动回退到手动模式
- **成本控制**: 实现使用限制和监控
- **用户教育**: 提供详细的使用说明和最佳实践

## 10. 成功指标

### 10.1 技术指标
- **功能完成度**: 100%
- **测试覆盖率**: >80%
- **性能指标**: 响应时间<5秒
- **错误率**: <1%
- **兼容性**: 100%向后兼容

### 10.2 业务指标
- **用户使用率**: >40%（AI功能启用率）
- **用户满意度**: >4.0/5.0
- **功能稳定性**: 不影响现有功能使用
- **用户反馈**: 积极正面

## 11. 后续规划

### 11.1 功能扩展
- **多AI模型支持**: 支持更多AI服务提供商
- **多语言支持**: 支持中文等语言的commit message
- **团队协作**: 支持团队级别的AI配置
- **智能分析**: 更智能的代码变更分析

### 11.2 平台扩展
- **VS Code支持**: 开发VS Code版本
- **其他IDE**: 支持Eclipse、WebStorm等
- **命令行工具**: 提供独立的命令行版本
- **Web版本**: 开发Web界面版本

---

**文档版本**: v2.0
**创建日期**: 2024年12月
**更新日期**: 2024年12月
**负责人**: 开发团队
**审核人**: 产品经理
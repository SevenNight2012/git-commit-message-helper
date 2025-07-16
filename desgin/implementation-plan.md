# AI Commit Message 实现计划（v2.0）

## 1. 项目概述

### 1.1 项目目标
在现有的Git Commit Message Helper插件基础上，**以最小化改动**的方式集成DeepSeek AI服务，实现智能commit message生成功能，提升开发效率和代码提交质量。

### 1.2 项目原则
- **最小化改动**：对现有UI和功能影响最小，AI功能以按钮和设置页形式渐进式集成
- **向后兼容**：AI功能默认关闭，现有用户体验不变
- **渐进式集成**：AI相关UI和逻辑仅在用户启用后生效
- **配置可选**：AI相关配置与现有设置并存，互不影响

## 2. 开发阶段规划

### 2.1 Phase 1: 架构与配置扩展（第1-2周）
- [ ] 扩展`GitCommitMessageHelperSettings`，增加`AISettings`字段及持久化
- [ ] 设计`AISettings`数据模型（apiKey、endpoint、model、maxTokens、temperature、enabled、autoGenerate等）
- [ ] 在`CentralSettingConfigurable`中集成AI设置Tab页（推荐）
- [ ] 保持原有配置和功能不变

### 2.2 Phase 2: API与分析集成（第3-4周）
- [ ] 实现`DeepSeekAPIClient`，兼容OpenAI风格API
- [ ] 实现`CodeChangeAnalyzer`，利用IntelliJ Git API获取diff和变更文件
- [ ] 实现`PromptBuilder`，生成符合规范的AI提示词，输出格式与现有模板一致
- [ ] 实现`AIGeneratorService`，串联分析、提示词、API调用、响应解析
- [ ] 单元测试与接口测试

### 2.3 Phase 3: UI集成与最小增强（第5-6周）
- [ ] 在`CommitPanel.form`和`CommitPanel.java`中添加AI按钮（推荐放在typePanel右侧）
- [ ] 添加AI状态指示器（如label或进度条，显示生成中/成功/失败）
- [ ] AI按钮仅在AI功能启用时显示
- [ ] 点击AI按钮后，自动填充现有表单字段（type、scope、subject、body等）
- [ ] 保持原有手动编辑和提交流程不变
- [ ] UI兼容深色/浅色主题

### 2.4 Phase 4: 测试、优化与发布（第7-8周）
- [ ] 端到端集成测试，确保AI功能不影响现有功能
- [ ] 性能与错误处理优化（异步调用、超时、降级、错误提示）
- [ ] 兼容性测试（不同IDE主题、不同配置、不同网络环境）
- [ ] 内部Beta发布，收集反馈
- [ ] 文档完善与正式发布

## 3. 技术实现要点

### 3.1 数据模型与配置
```java
// AI设置模型
public class AISettings {
    private String apiKey;
    private String apiEndpoint = "https://api.deepseek.com/v1/chat/completions";
    private String model = "deepseek-chat";
    private int maxTokens = 500;
    private double temperature = 0.7;
    private boolean enabled = false;
    private boolean autoGenerate = false;
    // getters/setters
}

// 在GitCommitMessageHelperSettings中集成
private AISettings aiSettings;
public AISettings getAISettings() { ... }
public void setAISettings(AISettings aiSettings) { ... }
```

### 3.2 主要类与接口
- `DeepSeekAPIClient`：负责与DeepSeek API通信，支持测试连接
- `CodeChangeAnalyzer`：分析当前Git变更，生成diff和文件列表
- `PromptBuilder`：根据变更生成AI提示词，输出格式与模板一致
- `AIGeneratorService`：负责AI生成主流程，异步调用，支持错误处理
- `AISettingsPanel`：AI设置UI，集成到设置页Tab
- `CommitPanel`增强：添加AI按钮和状态指示器，最小化UI改动

### 3.3 UI集成方案
- **AI按钮**：推荐放在typePanel右侧，使用"🤖 AI Generate"文本和图标
- **状态指示器**：按钮旁边显示生成状态（Ready/Generating/Success/Error）
- **表单自动填充**：AI生成后自动填充type、scope、subject、body等字段，用户可手动编辑
- **设置Tab**：在设置页增加AI Settings Tab，支持API Key、模型、参数配置和连接测试
- **主题适配**：兼容深色/浅色主题，按钮和状态指示器自适应

### 3.4 兼容性与安全
- **默认禁用**：AI功能默认关闭，用户需手动启用
- **配置兼容**：新旧配置互不影响，升级无缝
- **API Key加密**：本地加密存储API Key
- **降级处理**：API异常时不影响手动提交
- **隐私保护**：仅上传必要diff信息，用户可选

## 4. 测试与质量保证
- 单元测试：API客户端、分析器、提示词、设置管理等
- 集成测试：端到端AI生成流程、UI交互、错误处理
- 兼容性测试：不同主题、不同配置、不同网络
- 性能测试：响应时间、内存占用、并发
- 回归测试：确保现有功能不受影响

## 5. 发布计划
- **Beta发布**：内部测试，收集反馈
- **正式发布**：插件市场，文档同步
- **持续优化**：根据用户反馈迭代

## 6. 风险与缓解
- **API服务不稳定**：降级为手动模式，错误提示
- **网络问题**：重试与超时机制
- **用户不接受AI**：AI功能可选，默认关闭
- **API成本**：调用频率限制，用户自控

## 7. 成功指标
- 功能完成度100%，测试覆盖率>80%
- 响应时间<5秒，错误率<1%
- 用户满意度>4.0/5.0，AI功能启用率>40%
- 现有功能零破坏，用户反馈积极

---

**文档版本**: v2.0
**创建日期**: 2024年12月
**更新日期**: 2024年12月
**项目经理**: 开发团队
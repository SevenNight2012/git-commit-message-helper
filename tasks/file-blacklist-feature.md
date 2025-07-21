# 文件黑名单功能说明

## 概述

文件黑名单功能允许用户配置正则表达式规则来过滤敏感文件，防止这些文件的变更被上传到AI服务器进行分析。这对于保护包含敏感信息（如API密钥、配置文件、证书等）的文件非常有用。

## 功能特性

- **正则表达式支持**：每行一个正则表达式规则
- **实时验证**：提供模式验证功能，确保正则表达式语法正确
- **跨平台兼容**：自动处理Windows和Unix路径分隔符
- **容错处理**：忽略无效的正则表达式，继续使用有效的规则
- **用户友好**：提供示例和工具提示帮助用户理解用法

## 配置方法

### 1. 打开AI设置

1. 在IntelliJ IDEA中，进入 `File` → `Settings` (Windows/Linux) 或 `IntelliJ IDEA` → `Preferences` (macOS)
2. 导航到 `Tools` → `Git Commit Message Helper` → `AI Settings`
3. 找到 "File Filter Settings" 部分

### 2. 配置黑名单规则

在 "File Blacklist" 文本区域中输入正则表达式规则，每行一个：

```
# 示例规则
.*\.key$           # 过滤所有.key文件
.*\.pem$           # 过滤所有.pem文件
.*config.*         # 过滤包含config的文件和目录
.*\.env$           # 过滤.env文件
.*\.properties$    # 过滤.properties文件
.*secrets.*        # 过滤包含secrets的文件和目录
```

### 3. 验证规则

点击 "Validate Patterns" 按钮来验证所有正则表达式规则的有效性。

## 常用正则表达式示例

| 规则 | 说明 | 匹配示例 |
|------|------|----------|
| `.*\.key$` | 过滤所有.key文件 | `config/private.key`, `keys/server.key` |
| `.*\.pem$` | 过滤所有.pem文件 | `certs/certificate.pem`, `ssl/private.pem` |
| `.*config.*` | 过滤包含config的文件和目录 | `config/settings.json`, `app/config/database.yml` |
| `.*\.env$` | 过滤.env文件 | `.env`, `production.env` |
| `.*\.properties$` | 过滤.properties文件 | `application.properties`, `config.properties` |
| `.*secrets.*` | 过滤包含secrets的文件和目录 | `secrets/api.key`, `app/secrets/config.yml` |
| `.*password.*` | 过滤包含"password"的文件 | `password.txt`, `user-password.conf` |
| `.*\.(key\|pem\|p12)$` | 过滤多种证书文件 | `private.key`, `cert.pem`, `keystore.p12` |

## 工作原理

1. **初始化**：当AI功能启用时，系统会读取配置的黑名单规则并编译为正则表达式
2. **文件过滤**：在分析代码变更时，每个文件的路径都会与黑名单规则进行匹配
3. **跳过处理**：如果文件路径匹配任何黑名单规则，该文件将被跳过，不会包含在AI分析中
4. **路径标准化**：系统会自动将Windows路径分隔符(`\`)转换为Unix分隔符(`/`)以确保跨平台兼容性

## 安全考虑

- **本地处理**：文件过滤在本地进行，不会将黑名单规则发送到外部服务器
- **隐私保护**：被过滤的文件内容不会上传到AI服务器
- **配置持久化**：黑名单配置保存在本地，不会泄露到外部

## 故障排除

### 常见问题

1. **规则不生效**
   - 检查正则表达式语法是否正确
   - 使用 "Validate Patterns" 按钮验证规则
   - 确保文件路径格式正确

2. **误过滤文件**
   - 检查正则表达式是否过于宽泛
   - 使用更具体的路径模式
   - 测试规则匹配效果

3. **性能问题**
   - 避免使用过于复杂的正则表达式
   - 减少规则数量，合并相似规则
   - 使用更精确的匹配模式

### 调试技巧

1. **测试规则**：在文本编辑器中测试正则表达式
2. **逐步验证**：逐个添加规则并测试效果
3. **查看日志**：检查IDE日志中的错误信息

## 最佳实践

1. **精确匹配**：使用尽可能精确的正则表达式，避免过度匹配
2. **分类组织**：按文件类型或目录组织规则，便于维护
3. **定期审查**：定期检查和更新黑名单规则
4. **团队共享**：与团队成员共享有效的黑名单配置
5. **文档记录**：为复杂的正则表达式添加注释说明

## 技术实现

文件黑名单功能由以下组件实现：

- `FileBlacklistFilter`：核心过滤逻辑
- `CodeChangeAnalyzer`：集成过滤功能到代码分析流程
- `AISettingsPanel`：用户界面配置
- `AISettings`：数据模型存储

所有组件都经过单元测试验证，确保功能的正确性和稳定性。
# 网络请求日志记录功能说明

## 概述

网络请求日志记录功能是Git Commit Message Helper插件的一个重要组成部分，用于记录网络请求异常信息，帮助开发者诊断和调试API调用问题。

## 功能特性

### 1. 完整的异常信息记录
- **请求参数**：URL、HTTP方法、请求头、请求体
- **响应数据**：状态码、响应头、响应体
- **异常信息**：异常类型、异常消息、完整堆栈跟踪
- **上下文信息**：发生异常的方法名、时间戳

### 2. 智能文件管理
- **按天分文件**：每天创建一个新的日志文件
- **自动清理**：最多保存7天的日志文件，自动删除过期文件
- **UTF-8编码**：确保中文等字符正确显示

### 3. 异步记录
- **非阻塞**：日志记录在后台线程进行，不影响主流程
- **容错处理**：日志记录失败不会影响正常的API调用

## 日志文件位置

### Windows系统
```
%USERPROFILE%\GitCommitMessageHelper\network-request-logs\
```

### macOS/Linux系统
```
~/.GitCommitMessageHelper/network-request-logs\
```

### 日志文件命名格式
```
network-request-YYYY-MM-DD.log
```

例如：`network-request-2024-01-15.log`

## 日志内容格式

每个异常记录包含以下信息：

```
=== Network Request Exception Log ===
Timestamp: 2024-01-15 14:30:25.123
Method: generateMessage
Exception: IOException
Exception Message: Connection timeout

--- Request Information ---
URL: https://api.deepseek.com/v1/chat/completions
Method: POST
Headers: Authorization: Bearer sk-xxx, Content-Type: application/json
Request Body: {"model":"deepseek-chat","messages":[{"role":"user","content":"test"}],"max_tokens":500,"temperature":0.7,"stream":false}

--- Response Information ---
Status Code: 500
Response Headers: Content-Type: application/json, Server: nginx
Response Body: {"error":{"message":"Internal server error","type":"server_error"}}

--- Exception Stack Trace ---
java.io.IOException: Connection timeout
    at com.fulinlin.utils.DeepSeekAPIClient.generateMessage(DeepSeekAPIClient.java:67)
    at com.fulinlin.utils.AIGeneratorService.generateCommitMessage(AIGeneratorService.java:45)
    ...

=== End Log Entry ===
```

## 使用场景

### 1. API连接问题诊断
当遇到网络连接问题时，日志会记录：
- 具体的连接错误信息
- 请求的目标URL和参数
- 网络超时或连接被拒绝的详细信息

### 2. API响应错误分析
当API返回错误状态码时，日志会记录：
- HTTP状态码和错误响应
- 完整的请求参数
- 服务器返回的错误信息

### 3. 配置问题排查
当API配置有误时，日志会记录：
- 使用的API端点和密钥信息（部分隐藏）
- 请求头配置
- 参数格式问题

## 日志文件管理

### 自动清理机制
- 系统会自动保留最近7天的日志文件
- 超过7天的日志文件会被自动删除
- 清理操作在插件启动时进行

### 手动清理
如果需要手动清理日志文件：
1. 找到日志目录位置
2. 删除不需要的日志文件
3. 重启插件后会自动创建新的日志文件

## 隐私和安全

### 敏感信息处理
- API密钥在日志中会被部分隐藏
- 请求体中的敏感数据会被完整记录（用于调试）
- 建议定期清理日志文件

### 日志文件权限
- 日志文件存储在用户目录下，只有当前用户可访问
- 不会上传到任何外部服务器
- 仅在本地存储，保护用户隐私

## 故障排除

### 常见问题

1. **日志文件无法创建**
   - 检查用户目录权限
   - 确认磁盘空间充足
   - 重启IDE后重试

2. **日志内容为空**
   - 确认网络请求确实发生了异常
   - 检查日志记录器是否正常初始化
   - 查看IDE日志中的错误信息

3. **日志文件过大**
   - 检查是否有大量异常发生
   - 考虑调整日志级别
   - 手动清理旧日志文件

### 调试技巧

1. **查看实时日志**
   - 在发生异常时立即查看当天的日志文件
   - 使用文本编辑器打开日志文件
   - 搜索特定的异常类型或错误信息

2. **分析异常模式**
   - 统计特定类型的异常频率
   - 识别网络问题的规律
   - 优化API调用策略

3. **性能监控**
   - 监控日志文件大小
   - 观察异常发生的时间分布
   - 评估网络稳定性

## 技术实现

### 核心组件
- `NetworkRequestLogger`：日志记录器主类
- `RequestInfo`：请求信息数据类
- `ResponseInfo`：响应信息数据类

### 集成方式
- 在`DeepSeekAPIClient`中自动集成
- 所有网络请求异常都会被记录
- 无需手动调用日志记录方法

### 性能考虑
- 异步记录，不影响主线程
- 单线程执行器，避免并发问题
- 内存使用最小化

## 更新日志

### v2.0.0-ai
- 新增网络请求日志记录功能
- 支持按天分文件存储
- 自动清理过期日志文件
- 完整的异常信息记录
# 网络请求日志目录显示功能

## 功能描述

在AI设置面板中添加了网络请求日志目录路径的显示功能，用户可以清楚地看到网络请求日志文件存储的位置。

## 实现细节

### 1. UI组件添加

在 `AISettingsPanel.java` 的生成设置面板中添加了以下组件：

- **标签**: "Log Directory:"
- **文本字段**: 只读的文本字段，显示日志目录的完整路径
- **工具提示**: "Network request log directory path (read-only)"

### 2. 路径计算逻辑

使用与 `NetworkRequestLogger` 相同的逻辑来计算日志目录路径：

```java
private String getLogDirectoryPath() {
    // 使用与NetworkRequestLogger相同的逻辑获取日志目录
    String cachePath = System.getProperty("idea.system.path");
    if (cachePath == null) {
        cachePath = System.getProperty("user.home");
    }

    java.nio.file.Path basePath = java.nio.file.Paths.get(cachePath);
    java.nio.file.Path logDir;

    if (SystemInfo.isWindows) {
        logDir = basePath.resolve("GitCommitMessageHelper").resolve("network-request-logs");
    } else {
        logDir = basePath.resolve(".GitCommitMessageHelper").resolve("network-request-logs");
    }

    return logDir.toString();
}
```

### 3. 初始化时机

在 `AISettingsPanel` 构造函数中调用 `initializeLogDirectory()` 方法来初始化日志目录路径显示。

### 4. 错误处理

如果无法获取日志目录路径，会在文本字段中显示 "Failed to get log directory path"。

## 用户体验

- **只读显示**: 用户可以看到但不能修改日志目录路径
- **跨平台支持**: 自动根据操作系统（Windows/Unix）显示正确的路径
- **实时计算**: 路径在面板创建时动态计算，确保准确性
- **清晰标识**: 通过标签和工具提示明确说明这是网络请求日志目录

## 测试验证

添加了 `testLogDirectoryDisplay()` 测试方法来验证功能实现。

## 相关文件

- `src/main/java/com/fulinlin/ui/setting/AISettingsPanel.java` - 主要实现
- `src/test/java/com/fulinlin/ui/setting/AISettingsPanelTest.java` - 测试代码
- `src/main/java/com/fulinlin/utils/NetworkRequestLogger.java` - 日志记录器（路径计算参考）
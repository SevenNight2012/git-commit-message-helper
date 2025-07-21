# 文件黑名单通配符功能

## 概述

FileBlacklistFilter 现在支持自动将通配符模式转换为正则表达式模式，使得用户可以更方便地配置文件过滤规则。

## 功能特性

### 1. 通配符模式支持

支持以下通配符模式：
- `*` - 匹配任意数量的字符
- `?` - 匹配单个字符
- 路径分隔符 `/` - 支持目录结构匹配

### 2. 自动转换

系统会自动检测输入的模式类型：
- 如果包含正则表达式特殊字符（如 `()[]{}+|^$\`），则按正则表达式处理
- 如果包含 `.*` 模式，则按正则表达式处理
- 否则按通配符模式处理并自动转换为正则表达式

### 3. 转换规则

| 通配符模式 | 转换后的正则表达式 | 说明 |
|-----------|------------------|------|
| `*.md` | `.*\.md$` | 匹配所有 .md 文件 |
| `*.txt` | `.*\.txt$` | 匹配所有 .txt 文件 |
| `config/*.json` | `config/.*\.json$` | 匹配 config 目录下的所有 .json 文件 |
| `src/*/*.java` | `src/.*/.*\.java$` | 匹配 src 目录下任意子目录中的 .java 文件 |
| `test?.txt` | `test.\.txt$` | 匹配 test 后跟一个字符的 .txt 文件 |

### 4. 正则表达式保持不变

如果输入已经是正则表达式模式，系统会保持原样：
- `.*\.key$` → `.*\.key$`
- `.*config.*` → `.*config.*`

## 使用示例

### 在 AI 设置面板中

在文件黑名单输入框中，可以输入：

```
*.md
*.txt
config/*.json
src/**/*.java
.*\.key$
.*password.*
```

### 程序化使用

```java
FileBlacklistFilter filter = new FileBlacklistFilter();

// 初始化配置（支持混合模式）
filter.initializeFromConfig("*.md\n*.txt\n.*\\.key$");

// 检查文件是否被过滤
boolean isBlocked = filter.isFileBlacklisted("README.md"); // true
boolean isAllowed = filter.isFileBlacklisted("src/main.java"); // false

// 验证配置
FileBlacklistFilter.ValidationResult result =
    FileBlacklistFilter.validateBlacklistConfig("*.md\n*.txt");
if (result.isValid()) {
    System.out.println("配置有效");
} else {
    System.out.println("配置错误: " + result.getErrorMessage());
}
```

## 测试覆盖

功能包含完整的测试覆盖：
- 基本通配符转换测试
- 问号通配符测试
- 混合模式测试
- 正则表达式保持不变测试
- 配置验证测试
- 文件过滤功能测试

## 向后兼容性

此功能完全向后兼容：
- 现有的正则表达式配置继续有效
- 新的通配符模式提供了更友好的配置方式
- 验证功能确保配置的正确性
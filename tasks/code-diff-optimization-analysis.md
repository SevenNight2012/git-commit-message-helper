# CodeChangeAnalyzer Diff内容优化技术方案

## 背景

当前`CodeChangeAnalyzer.java`在分析变更内容时，直接获取了前后版本的全部内容，并拼接为diff内容。这种方式存在以下问题：
- **数据量大**：对于大文件，全部内容会极大增加数据传输量。
- **请求失败风险**：数据量过大时，可能导致请求超时或失败。
- **Token浪费**：对于AI服务（如DeepSeek），传递全部内容会浪费大量token，增加成本。
- **不友好展示**：全部内容不利于直观查看实际变更。

## 目标

优化diff内容生成逻辑，使其输出内容类似于`git diff`命令，仅包含实际变更的部分（增删改行），而非全部文件内容。

## 技术方案分析

### 1. 方案对比

| 方案 | 优点 | 缺点 |
|------|------|------|
| 直接获取全部内容 | 实现简单 | 数据量大，浪费token，不直观 |
| 采用diff算法，仅输出变更内容 | 数据量小，直观，节省token | 实现复杂度略高 |

### 2. 可选diff算法

- **JDK自带Diff算法**：JDK本身没有直接的文本diff工具。
- **第三方库**：
  - [google-diff-match-patch](https://github.com/google/diff-match-patch)：轻量级，支持行/字符级diff，Java实现。
  - [java-diff-utils](https://github.com/java-diff-utils/java-diff-utils)：功能丰富，支持unified diff格式，广泛用于Java项目。
- **手写简单diff**：可实现最基础的行级diff，但不建议，维护性差。

### 3. 推荐方案

**优先推荐：集成`java-diff-utils`库**
- 支持生成unified diff格式，与git diff输出类似。
- API简单，社区活跃。
- 可灵活定制diff输出（如上下文行数等）。

### 4. 实现思路

1. **引入依赖**
   - 在`build.gradle`中添加`java-diff-utils`依赖。
2. **内容分割**
   - 将before/after内容按行分割为List<String>。
3. **生成diff**
   - 使用`DiffUtils.diff()`生成Patch对象。
   - 使用`UnifiedDiffUtils`生成unified diff文本。
4. **替换原有diff内容生成逻辑**
   - 仅输出unified diff内容。

### 5. 兼容性与风险

- **兼容性**：`java-diff-utils`为纯Java实现，兼容性好。
- **性能**：对大文件有一定性能消耗，但远小于传递全部内容。
- **依赖管理**：需确保依赖可被IDE插件项目正确打包。

### 6. 参考代码片段

```java
import difflib.DiffUtils;
import difflib.Patch;
import difflib.UnifiedDiffUtils;

List<String> original = Arrays.asList(beforeContent.split("\\n"));
List<String> revised = Arrays.asList(afterContent.split("\\n"));
Patch<String> patch = DiffUtils.diff(original, revised);
List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
    fileName, fileName, original, patch, 3);
String diffText = String.join("\n", unifiedDiff);
```

---

## 7. 针对多种文件类型的适用性与处理建议

### 7.1 文本类文件

包括：Java、Kotlin、XML、Groovy、Markdown、properties、txt等。

- **处理方式**：直接采用diff算法（如`java-diff-utils`），仅输出实际变更的内容。
- **优点**：极大减少数据量，提升网络请求稳定性，节省AI服务token消耗。
- **适用性**：完全适用。

### 7.2 二进制文件

包括：PNG、JPEG、WEBP等图片，音频、视频等二进制文件。

- **处理方式**：
  - diff算法无法对二进制内容做内容级变更输出。
  - 推荐仅输出变更元信息（如文件名、变更类型：新增/删除/修改），**不传递文件内容本身**。
  - 如确需传递内容，建议仅在必要场景下传递，并考虑压缩或base64编码，但这会极大增加消耗。
- **优点**：避免大文件内容传输，保证网络稳定性。
- **适用性**：仅适合传递元信息，不适合传递内容diff。

### 7.3 文件类型自动识别建议

- 可通过文件扩展名判断文件类型。
- 对于文本类文件，走diff内容生成逻辑。
- 对于二进制文件，仅记录变更类型和文件名。

---

## 8. 网络消耗与稳定性分析

- **文本类文件**：采用unified diff后，数据量极小，网络请求稳定性大幅提升，AI服务token消耗极低。
- **二进制文件**：只传递变更元信息，不会有大流量和token消耗，网络稳定性有保障。
- **整体**：只要不把大文件内容（尤其是图片、视频等二进制内容）直接传递给AI服务，网络和token消耗都能得到有效控制。

---

## 9. 结论与建议

- 目前的技术方案**完全适用于所有文本类文件**，能够极大减少请求消耗，保证网络稳定性。
- 对于图片等二进制文件，**建议只传递变更元信息**，不要传递内容本身，这样同样可以保证消耗和稳定性。
- 只要在实现时对不同类型文件做适当区分，**你的目标可以很好地实现**。
- 如需支持更多文本类型，可扩展支持的文件后缀列表。
- 如需特殊处理二进制文件（如图片对比），需引入专门的二进制diff算法或仅做元信息传递。

---

*如需具体代码实现建议或对某类文件的特殊处理方案，请随时告知。*
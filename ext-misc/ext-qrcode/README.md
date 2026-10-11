# ext-qrcode

> 二维码工具模块 - 提供二维码生成与识别功能

## 简介

`ext-qrcode` 是 ext-library 的二维码工具模块，基于 Google ZXing 库，提供简洁的链式 API 进行二维码生成和识别。

## 快速开始

### Maven

```xml
<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-qrcode</artifactId>
</dependency>
```

### Gradle

```groovy
implementation("ext.library:ext-qrcode")
```

## 依赖说明

| 依赖 | 说明 |
|------|------|
| ext-tool | 基础工具类 |
| zxing-javase | Google ZXing 二维码库 |

## 功能特性

- 服务端二维码生成
- 服务端二维码识别
- 支持添加 Logo
- 自定义尺寸和颜色
- 多种输出格式 (PNG, JPG, Base64, 字节数组, 流)
- 自动删除白边

## 核心类说明

| 类名 | 说明 |
|------|------|
| `QrCode` | 二维码工具类，提供链式 API |

## 使用示例

### 生成二维码

```java
// 基本用法
QrCode.from("https://example.com")
    .toFile("/path/to/output.png");

// 完整配置
QrCode.from("牛年大吉")
    .size(512)                          // 尺寸，默认 512
    .backGroundColor(Color.WHITE)       // 背景色，默认白色
    .foreGroundColor(Color.BLACK)       // 前景色，默认黑色
    .encode(Charsets.UTF_8)             // 编码，默认 UTF-8
    .imageFormat("png")                 // 图片格式，默认 png
    .deleteMargin(true)                 // 删除白边，默认 true
    .logo("/path/to/logo.png")          // 添加 Logo
    .toFile("/path/to/output.png");
```

### 多种输出方式

```java
// 输出到文件
QrCode.from("content").toFile("/path/to/output.png");

// 输出为 BufferedImage
BufferedImage image = QrCode.from("content").toImage();

// 输出为字节数组
byte[] bytes = QrCode.from("content").toBytes();

// 输出为 Base64 Data URI（可直接用于 HTML img 标签）
String base64 = QrCode.from("content").toBase64();
// 结果: data:image/png;base64,iVBORw0KGgoAAAANS...

// 输出到流
OutputStream outputStream = ...;
QrCode.from("content").toStream(outputStream);
```

### 添加 Logo

```java
// 本地文件
QrCode.from("content")
    .logo("/path/to/logo.png")
    .toFile("/path/to/output.png");

// URL 远程图片
QrCode.from("content")
    .logo("https://example.com/logo.png")
    .toFile("/path/to/output.png");

// InputStream
QrCode.from("content")
    .logo(inputStream)
    .toFile("/path/to/output.png");
```

### 自定义样式

```java
// 自定义颜色
QrCode.from("content")
    .size(300)
    .foreGroundColor(new Color(0, 100, 200))  // 蓝色前景
    .backGroundColor(Color.WHITE)
    .toFile("/path/to/output.png");

// 保留白边
QrCode.from("content")
    .deleteMargin(false)
    .toFile("/path/to/output.png");

// 不同图片格式
QrCode.from("content")
    .imageFormat("jpg")   // jpg 格式
    .toFile("/path/to/output.jpg");
```

### 识别二维码

```java
// 从文件读取
String text = QrCode.read("/path/to/qrcode.png");

// 从 URL 读取
String text = QrCode.read("https://example.com/qrcode.png");

// 从 InputStream 读取
String text = QrCode.read(inputStream);

// 从 BufferedImage 读取
String text = QrCode.read(bufferedImage);

// 获取原始字节数据（用于二进制内容）
byte[] rawBytes = QrCode.readRawBytes(bufferedImage);
```

## API 速查

### 创建方法

| 方法 | 描述 |
|------|------|
| `QrCode.from(content)` | 创建二维码构建器 |

### 配置方法

| 方法 | 描述 |
|------|------|
| `size(int)` | 设置二维码尺寸（必须大于 0） |
| `backGroundColor(Color)` | 设置背景颜色 |
| `foreGroundColor(Color)` | 设置前景颜色 |
| `encode(Charset)` | 设置编码格式 |
| `imageFormat(String)` | 设置图片格式（png/jpg/gif 等） |
| `deleteMargin(boolean)` | 是否删除白边 |
| `logo(String/InputStream)` | 添加 Logo |

### 输出方法

| 方法 | 描述 |
|------|------|
| `toFile(String)` | 输出到文件 |
| `toImage()` | 返回 BufferedImage |
| `toBytes()` | 返回字节数组 |
| `toBase64()` | 返回 Base64 Data URI（MIME 类型自动匹配图片格式） |
| `toStream(OutputStream)` | 输出到流 |

### 识别方法

| 方法 | 描述 |
|------|------|
| `QrCode.read(String)` | 从文件/URL 识别 |
| `QrCode.read(InputStream)` | 从流识别 |
| `QrCode.read(BufferedImage)` | 从图片识别 |
| `QrCode.readRawBytes(String)` | 从文件/URL 读取原始字节 |
| `QrCode.readRawBytes(InputStream)` | 从流读取原始字节 |
| `QrCode.readRawBytes(BufferedImage)` | 从图片读取原始字节 |

## 配置默认值

| 配置项 | 默认值 |
|-------|-------|
| 尺寸 | 512 |
| 背景色 | 白色 (Color.WHITE) |
| 前景色 | 黑色 (Color.BLACK) |
| 编码 | UTF-8 |
| 图片格式 | png |
| 删除白边 | true |

## 注意事项

1. **内容不能为空**：`QrCode.from(content)` 的 content 参数不能为 null 或空字符串。

2. **尺寸限制**：`size(int)` 参数必须大于 0。

3. **Base64 MIME 类型**：`toBase64()` 会根据 `imageFormat` 自动设置正确的 MIME 类型（image/png、image/jpeg 等）。

4. **Logo 可选**：Logo 是可选的，不设置 Logo 时二维码正常生成。

## 许可证

[Apache License 2.0](../../LICENSE)

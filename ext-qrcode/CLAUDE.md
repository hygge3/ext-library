[根目录](../CLAUDE.md) > **ext-qrcode**

# ext-qrcode 模块文档

## 模块职责

ext-qrcode 提供二维码生成功能，基于 ZXing 库封装。

## 入口与启动

该模块是工具类集合，无需自动配置。

## 核心组件

### 1. 工具类
- **QrCode**: 二维码生成工具类

## 关键依赖

- **ext-tool**: 基础工具类
- **javase**: ZXing JavaSE 支持

## 使用示例

### 生成二维码
```java
// 生成默认二维码
BufferedImage image = QrCode.generate("https://example.com");

// 生成带 Logo 的二维码
BufferedImage image = QrCode.generate("https://example.com", logoPath);

// 生成自定义尺寸的二维码
BufferedImage image = QrCode.generate("https://example.com", 400, 400);

// 生成自定义颜色的二维码
BufferedImage image = QrCode.generate("https://example.com", Color.BLACK, Color.WHITE);
```

## 常见问题 (FAQ)

### Q: 支持哪些格式？
支持常见的图片格式：PNG、JPG 等。

## 相关文件清单

### 主要源码文件
- `src/main/java/ext/library/qrcode/QrCode.java`

## 变更记录 (Changelog)

### 2025-12-24
- 创建：模块文档

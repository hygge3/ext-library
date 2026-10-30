---
id: ext-qrcode
type: module-design
title: ext-qrcode — 二维码生成与识别
status: draft
parent: ext-library-architecture
depends-on: [ext-tool]
---

# ext-qrcode — 二维码生成与识别

**职责**：基于 Google ZXing 的二维码工具：`QrCode.from(content)` 链式 API（`size`、颜色、Logo、输出 PNG / JPG / `byte[]` / Base64），以及 `QrCode.read(inputStream)` 识别。

**边界**：只依赖 `ext-tool` 与 `javase` 版 ZXing；不提供条码以外的格式、不做图片处理管线。

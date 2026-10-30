---
id: ext-useragent
type: module-design
title: ext-useragent — User-Agent 解析
status: draft
parent: ext-library-architecture
depends-on: [ext-tool]
---

# ext-useragent — User-Agent 解析

**职责**：把 UA 字符串解析成结构化结果：浏览器与版本、操作系统与版本、设备平台（移动 / 桌面 / iOS / Android）、渲染引擎与版本（`UserAgentParser.parse`，结果含 `isMobile()`）。

**边界**：纯解析，只依赖 `ext-tool`；无自动配置、无 Spring 依赖，可当普通工具库使用。空串输入返回 `null`——调用方必须判空（历史 NPE 修复结论）。

**消费者**：[ext-security](../../ext-security/SPEC.md) 用它记录登录设备，因此解析结果的变化会影响多端登录限制的判定。

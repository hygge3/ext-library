---
id: ext-json
type: module-design
title: ext-json — JSON 处理与数据口径
status: draft
parent: ext-library-architecture
depends-on: [ext-tool]
---

# ext-json — JSON 处理与数据口径

**职责**：三件事——

- Jackson `ObjectMapper` 增强配置、JSON 序列化/反序列化工具、JsonPath 查询。
- **字段翻译** `ext.library.trans`：`@Translate` 注解 + `TranslatorRegistry` + Jackson `TranslationHandler`，在序列化期把编码翻成展示值。
- **数据脱敏** `ext.library.desensitize`：`@Sensitive` + `SensitiveStrategy` + `DesensitizationUtil`。

**边界**：依赖 `ext-tool`、`starter-json`、`json-path`；不做 HTTP 处理、不落库。

**包结构**：翻译与脱敏是同一层的两个兄弟子包，都直接挂在 `ext.library` 下——`ext.library.trans.*` 与 `ext.library.desensitize.*`，不嵌进 JSON 相关包（它们是「数据口径」，JSON 只是生效时机），也不合并成一个大包（两者使用场景独立）。

**决策**：脱敏策略是全库唯一口径。`ext-logback` 的日志脱敏直接复用 `DesensitizeStrategy`，所以同一个手机号在日志里和接口返回里被遮蔽成同一形态。新增规则改这里，不要在调用方另写遮蔽逻辑。

---
id: ext-logback
type: module-design
title: ext-logback — 日志脱敏
status: draft
parent: ext-library-architecture
depends-on: [ext-json]
---

# ext-logback — 日志脱敏

**职责**：把脱敏口径搬到日志输出上：`%maskedMsg` 转换器 `LogMaskConverter`、单条规则 `LogMaskRule`（正则 + 遮蔽函数，可只遮蔽指定捕获组）、内置规则集 `LogMaskRules`（手机号、身份证、邮箱、银行卡、IPv4、键值对凭据）、静态注册表 `LogMaskRegistry`、开关每条规则的 `LogMaskProperties`（前缀 `ext.logback.mask`）。

**遮蔽口径**：遮蔽函数直接复用 [ext-json](../../ext-common/ext-json/SPEC.md) 的 `DesensitizeStrategy`，所以日志里的手机号和接口返回里的手机号是同一形态——不要在这里另写一套遮蔽逻辑。

**关键约束**：converter 由 logback XML 实例化，拿不到 Spring 容器；规则必须在启动时由 `LogMaskAutoConfiguration` 写入 `LogMaskRegistry`。规则表为空时 converter 直接放行，不报错。

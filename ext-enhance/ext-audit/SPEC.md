---
id: ext-audit
type: module-design
title: ext-audit — 操作审计
status: draft
parent: ext-library-architecture
depends-on: [ext-core, ext-json, ext-servlet, ext-security]
---

# ext-audit — 操作审计

**职责**：`@OperationLog`（`module` / `type` / `desc` 支持 SpEL，`recordArgs` / `recordResult` 控制入参与结果是否落痕）+ `AuditAspect`：**成功与失败都留痕**。`AuditRecord` 携带操作人、模块、类型、描述、入参、结果、耗时、时间；`AuditHandler` 为 SPI，业务实现它把记录落库或投递 MQ；`AuditOperatorResolver` 解析操作人。

**不变量**：`AuditDispatcher` 分发时，单个 `AuditHandler` 抛异常不得影响业务方法与其它处理器——审计永远不能把业务打挂。未注册任何 `AuditHandler` 时按 `ext.audit.log-to-console`（默认 `true`）降级打日志。

**边界**：对 [ext-security](../../ext-security/SPEC.md) 只做 `compileOnly` 依赖 + 反射调用 `SecurityUtil` 取当前登录人，取不到时回退客户端 IP；因此不引安全模块也能启动。依赖 `ext-json` 做序列化。客户端 IP 一律用 [ext-servlet](../../ext-common/ext-servlet/SPEC.md) 的 `ServletUtil.getIpAddr()`（走 `X-Forwarded-For` 链），**不要用裸 `getRemoteAddr()`**——那记下来的是反代后的内网地址，与限流、安全用的 IP 口径不一致。

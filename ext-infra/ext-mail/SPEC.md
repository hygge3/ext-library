---
id: ext-mail
type: module-design
title: ext-mail — 邮件发送封装
status: draft
parent: ext-library-architecture
depends-on: [ext-tool]
---

# ext-mail — 邮件发送封装

**职责**：对 Spring Mail 的收敛封装——`MailSender` / `MailSenderImpl` 发送、`MailDetails` 描述邮件、`MailSendResult` 返回结果、`MailSendEvent` 发布发送事件（便于监听失败/重试）。`MailProperties` 承载 `ext.mail.*`：`retry-count`（失败重试次数，默认 `3`，即首次之外最多再试 3 次）、`retry-interval`（重试间隔，默认 `1s`），重试复用 `ext-tool` 的 `RetryUtils.doWithRetry`；最终的成败结果都通过 `MailSendEvent` 发出。

**边界**：只依赖 `ext-tool` 与 `starter-mail`；账号与服务器配置完全沿用 Spring 前缀 `spring.mail`，除重试参数外不新增配置项，也不管理发送队列或模板。

---
id: ext-core
type: module-design
title: ext-core — 核心功能
status: draft
parent: ext-library-architecture
depends-on: [ext-tool]
---

# ext-core — 核心功能

**职责**：全库共用的核心机制：SpEL 表达式求值（`ExtExpressionEvaluator`）、基于 Spring `BeanUtils` 的属性拷贝（`BeanUtil`）、AOP / AspectJ 支持、线程池与异步执行（`ThreadPoolConfiguration`、`AsyncConfiguration`，前缀 `ext.thread-pool`）、`SpringUtil` 静态容器访问、发布订阅 SPI `PubSub`（`publish` / `subscribe`，由 [ext-redis](../../ext-infra/ext-redis/SPEC.md) 与 [ext-postgres](../../ext-infra/ext-postgres/SPEC.md) 各实现一份）。

**边界**：只依赖 `ext-tool`。不依赖 `ext-json`，不引入任何 web / servlet 组件——它被非 Web 应用使用，拖入 web 栈会改变消费方的启动形态。

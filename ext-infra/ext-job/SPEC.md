---
id: ext-job
type: module-design
title: ext-job — 基于 PG 队列的任务调度
status: draft
parent: ext-library-architecture
depends-on: [ext-postgres]
---

# ext-job — 基于 PG 队列的任务调度

**职责**：基于 [ext-postgres](../ext-postgres/SPEC.md) 的 `PostgresQueue` 提供声明式任务投递与执行，不引入独立调度框架：`JobHandler<T>`（声明负载类型、队列名与处理逻辑，`handle` 抛异常即视为本次尝试失败）、`JobDispatcher`（投递、延迟投递、重试次数上限、队列统计）、`JobExecutor`（`@Scheduled` 轮询出队）、运维能力 `countPending` / `stats` / `cleanup` / `resetStuck` / `queues`。

**决策**：队列直接用 PostgreSQL 的 `FOR UPDATE SKIP LOCKED`，因此不需要额外的消息中间件——这也是本库"用 PG 替代 Redis"路线的一部分。

**边界与装配**：未注册任何 `JobHandler` 时只装配投递器与执行器，不启动轮询；配置前缀 `ext.job`（`enabled`、`batch-size`、`poll-interval-millis`、`stuck-timeout`、`completed-retention`、`recover-on-startup`、`cleanup-on-startup`）。

**建表**：队列表由 [ext-postgres](../ext-postgres/SPEC.md) 的 `ext.postgres.queue.auto-init-schema` 决定是否自动创建（默认 `true`，表名 `ext.postgres.queue.table-name`，默认 `pg_jobs`）。关掉它就真的不建表——`ext-job` 自己不做任何 DDL，也不会在启动时偷偷把表补上；这时消费方必须先用迁移工具建好同名表（列结构见 [ext-postgres](../ext-postgres/SPEC.md)），否则 `ext-job` 不可用。

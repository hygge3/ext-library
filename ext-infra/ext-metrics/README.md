# ext-metrics

运行时指标采集、即时告警与 Web 大屏。纯自建，不依赖 Micrometer / Actuator / Prometheus 客户端。

## 使用方案：引入依赖即接入，业务代码不改

```gradle
implementation 'ext.library:ext-infra-ext-metrics'
```

唯一动作就是加这一行依赖。零配置、零注解、零初始化代码，启动后：

- 日志里出现 `载入模块：运行时指标与告警` 与 `指标大屏：/metrics`；
- 访问 `http://<host>:<port>/metrics` 看大屏，`/metrics/snapshot` 拿 JSON；
- 错误率或 P99 超阈值时，日志里出现 `[指标]` 开头的 WARN。

自动配置具体做了这些事：

1. 注册 `MetricsFilter`（顺序 `HIGHEST_PRECEDENCE + 30`，在 trace 与 accesslog 之后），统计所有 HTTP 请求的 QPS、耗时与 P99、错误率、错误码分布、端点明细；
2. 提供 `MetricsRecorder` Bean —— 限流拒绝、幂等命中、业务错误码、慢 SQL 由各模块自己往这里上报；
3. 包住容器里的 `JdbcTemplate`（`JdbcClient` 由它构建），采集超过阈值的慢 SQL；
4. 启动一个守护线程，按间隔做告警评估与池水位采样。

没引入 ext-metrics 时，其它模块通过 `ObjectProvider<MetricsRecorder>` 取到 null 就跳过，不加载类也不报错。

**能零改动的前提**：HTTP 请求走 `MetricsFilter`（Servlet 环境），SQL 走容器里的 `JdbcTemplate`/`JdbcClient`，限流/幂等/业务异常走本库的 `@RateLimit`、`@Idempotent`、`@RepeatSubmit` 与 `GlobalExceptionHandler`。自己 `new JdbcTemplate(...)`、直接用 MyBatis、或绕过本库异常体系抛错，这四类采不到。

## 采什么

| 指标 | 来源 |
|------|------|
| QPS、P99、错误率、端点明细 | `MetricsFilter` 自动采集 |
| 错误码分布 | `GlobalExceptionHandler` 的业务异常分支 |
| 限流拒绝数 | `RateLimitAspect`，按 `@RateLimit(prefix)` 分组 |
| 幂等命中数 | `IdempotentAspect`，按方法签名分组 |
| 慢 SQL | `JdbcTemplate` 代理，SQL 字面量已脱敏 |
| 线程池 / 连接池水位 | `PoolSampler` 反射读 Bean |

口径说明：计数是**固定窗口**（默认 60s，窗口切换清零），P99 是 20 个延迟桶的近似值（取桶上界），不是精确分位。

## 告警

默认阈值：错误率 > 1%、P99 > 500ms。样本数少于 `alert.min-requests` 不评估，避免低流量接口误报。评估间隔 10s，同对象同指标冷却 5min。

通知默认只打 WARN 日志；要发钉钉/企业微信/邮件，提供自己的 `AlertNotifier` Bean 即可。

## 大屏

```yaml
ext:
  metrics:
    dashboard:
      enabled: true
      path: /metrics
```

页面是单文件 HTML，轮询 `<path>/snapshot` 的 JSON。**默认关闭是因为它没有鉴权**——对外暴露前自己套 Spring Security 或网关规则。

## 配置

```yaml
ext:
  metrics:
    enabled: true
    window: 60s
    max-endpoints: 200            # 端点表上限，满了汰汰最久未访问的那个
    slow-sql-threshold: 500ms
  dashboard:
    enabled: true
    path: /metrics
    token: ''              # 配置后需带 X-Metrics-Token 头或 ?token=；留空只允许"本机且无反向代理"的请求
    max-slow-sql: 20
    exclude-patterns:
      - /actuator
      - /v3/api-docs
    alert:
      enabled: true
      interval: 10s
      min-requests: 20
      error-rate-threshold: 0.01
      p99-threshold: 500ms
      cooldown: 5m
    pool:
      enabled: true
      interval: 15s
    dashboard:
      enabled: false
      path: /metrics
```

## 边界

- **不做分布式聚合**：每个实例各看各的，多实例要在网关或 Prometheus 侧汇总。
- **不做持久化**：重启清零。
- **不做链路追踪**：那是 ext-trace 的事。
- **不覆盖 MyBatis**：`SqlSessionTemplate` 不在 `JdbcTemplate` 代理内。

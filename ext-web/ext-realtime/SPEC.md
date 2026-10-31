---
id: ext-realtime
type: module-design
title: ext-realtime — SSE 与 WebSocket 实时通道
status: draft
parent: ext-library-architecture
depends-on: [ext-security, ext-mvc, ext-core]
---

# ext-realtime — SSE 与 WebSocket 实时通道

**职责**：两套实时通道，语义一致、可按需选一套或并用：

- **SSE**（`ext.library.sse`）：`SseController` 建流、`SseEmitterManager` / `SseConnectionManager` 管连接、`SseTopicListener` 订阅主题；配置前缀 `ext.sse`。
- **WebSocket**（`ext.library.websocket`）：`ExtWebSocketHandler` 收发、`WebSocketConnectionManager` 管会话、`WebSocketTopicListener` 订阅主题、`ExtWebSocketInterceptor` 握手期接入认证；配置前缀 `ext.websocket`。

**边界**：广播用的 `ext-redis` / `ext-postgres` 不在本模块声明依赖，由消费方按需引入（各自的自动配置会提供 `PubSub` Bean）——不引后端时只有**单实例**语义（同 JVM 内广播），多实例部署必须引其中之一。广播实现不自带：注入 [ext-core](../../ext-common/ext-core/SPEC.md) 的 `PubSub`，实际是 [ext-redis](../ext-redis/SPEC.md) 的 `RedisPubSub` 或 [ext-postgres](../ext-postgres/SPEC.md) 的 `PostgresPubSub`，两套通道共用同一份，不各自复制 pub/sub 实现。登录态与 token 校验交给 [ext-security](../../ext-security/SPEC.md)（`api` 依赖），本模块不定义会话模型。

**约束**：两套通道共用同一套连接管理/主题监听形状，新增第三方推送方式应沿用同一形状，不要在某一侧另造抽象。

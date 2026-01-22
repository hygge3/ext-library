# 用 PostgreSQL 替代 Redis

## 功能 #1：使用 UNLOGGED 表实现缓存

**Redis：**

```
await redis.set('session:abc123', JSON.stringify(sessionData), 'EX', 3600);
```



**PostgreSQL：**

```sql
CREATE UNLOGGED TABLE cache (
  key TEXT PRIMARY KEY,
  value JSONB NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_cache_expires ON cache(expires_at);
```



**插入：**

```sql
INSERT INTO cache (key, value, expires_at)
VALUES ($1, $2, NOW() + INTERVAL '1 hour')
ON CONFLICT (key) DO UPDATE
  SET value = EXCLUDED.value,
      expires_at = EXCLUDED.expires_at;
```



**读取：**

```sql
SELECT value FROM cache
WHERE key = $1 AND expires_at > NOW();
```



**清理（定期执行）：**

```sql
DELETE FROM cache WHERE expires_at < NOW();
```



### 什么是 UNLOGGED？

**UNLOGGED 表：**

- 跳过预写日志（WAL）
- 写入速度更快
- 崩溃后数据不会保留（非常适合缓存！）

**性能对比：**

```
Redis SET: 0.05ms
Postgres UNLOGGED INSERT: 0.08ms
```



**对于缓存场景来说，差距可以接受。**

------

## PostgreSQL 功能 #2：使用 LISTEN/NOTIFY 实现发布/订阅

**这是最有趣的部分。**

PostgreSQL 有**原生的发布/订阅**功能，但大多数开发者并不知道。

### Redis 发布/订阅

```javascript
// 发布者
redis.publish('notifications', JSON.stringify({ userId: 123, msg: 'Hello' }));

// 订阅者
redis.subscribe('notifications');
redis.on('message', (channel, message) => {
  console.log(message);
});
```



### PostgreSQL 发布/订阅

```sql
-- 发布者
NOTIFY notifications, '{"userId": 123, "msg": "Hello"}';
```



```javascript
// 订阅者（Node.js 使用 pg 库）
const client = new Client({ connectionString: process.env.DATABASE_URL });
await client.connect();

await client.query('LISTEN notifications');

client.on('notification', (msg) => {
  const payload = JSON.parse(msg.payload);
  console.log(payload);
});
```



**性能对比：**

```
Redis 发布/订阅延迟: 1-2ms
Postgres NOTIFY 延迟: 2-5ms
```



**稍微慢一点，但是：**

- 无需额外基础设施
- 可在事务中使用
- 可与查询组合

### 真实案例：实时日志流

在我的日志管理应用中，我需要**实时日志流**。

**使用 Redis：**

```javascript
// 新日志到达时
await db.query('INSERT INTO logs ...');
await redis.publish('logs:new', JSON.stringify(log));

// 前端监听
redis.subscribe('logs:new');
```



**问题：** 两个操作。如果发布失败怎么办？

**使用 PostgreSQL：**

```sql
CREATE FUNCTION notify_new_log() RETURNS TRIGGER AS $$
BEGIN
  PERFORM pg_notify('logs_new', row_to_json(NEW)::text);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER log_inserted
AFTER INSERT ON logs
FOR EACH ROW EXECUTE FUNCTION notify_new_log();
```



现在是**原子操作**。插入和通知要么一起成功，要么一起失败。

```javascript
// 前端（通过 SSE）
app.get('/logs/stream', async (req, res) => {
  const client = await pool.connect();

  res.writeHead(200, {
    'Content-Type': 'text/event-stream',
    'Cache-Control': 'no-cache',
  });

  await client.query('LISTEN logs_new');

  client.on('notification', (msg) => {
    res.write(`data: ${msg.payload}\n\n`);
  });
});
```



**结果：** 零 Redis 实现实时日志流。

------

## PostgreSQL 功能 #3：使用 SKIP LOCKED 实现任务队列

**Redis（使用 Bull/BullMQ）：**

```javascript
queue.add('send-email', { to, subject, body });

queue.process('send-email', async (job) => {
  await sendEmail(job.data);
});
```



**PostgreSQL：**

```sql
CREATE TABLE jobs (
  id BIGSERIAL PRIMARY KEY,
  queue TEXT NOT NULL,
  payload JSONB NOT NULL,
  attempts INT DEFAULT 0,
  max_attempts INT DEFAULT 3,
  scheduled_at TIMESTAMPTZ DEFAULT NOW(),
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_jobs_queue ON jobs(queue, scheduled_at)
WHERE attempts < max_attempts;
```



**入队：**

```sql
INSERT INTO jobs (queue, payload)
VALUES ('send-email', '{"to": "user@example.com", "subject": "Hi"}');
```



**Worker（出队）：**

```sql
WITH next_job AS (
  SELECT id FROM jobs
  WHERE queue = $1
    AND attempts < max_attempts
    AND scheduled_at <= NOW()
  ORDER BY scheduled_at
  LIMIT 1
  FOR UPDATE SKIP LOCKED
)
UPDATE jobs
SET attempts = attempts + 1
FROM next_job
WHERE jobs.id = next_job.id
RETURNING *;
```



**魔法在于：`FOR UPDATE SKIP LOCKED`**

这使 PostgreSQL 成为一个**无锁队列**：

- 多个 worker 可以并发拉取任务
- 没有任务会被处理两次
- 如果 worker 崩溃，任务会重新变为可用状态

**性能对比：**

```
Redis BRPOP: 0.1ms
Postgres SKIP LOCKED: 0.3ms
```



**对于大多数工作负载来说，差异可以忽略不计。**

------

## PostgreSQL 功能 #4：限流

**Redis（经典限流器）：**

```javascript
const key = `ratelimit:${userId}`;
const count = await redis.incr(key);
if (count === 1) {
  await redis.expire(key, 60); // 60 秒
}

if (count > 100) {
  throw new Error('Rate limit exceeded');
}
```



**PostgreSQL：**

```sql
CREATE TABLE rate_limits (
  user_id INT PRIMARY KEY,
  request_count INT DEFAULT 0,
  window_start TIMESTAMPTZ DEFAULT NOW()
);

-- 检查并递增
WITH current AS (
  SELECT
    request_count,
    CASE
      WHEN window_start < NOW() - INTERVAL '1 minute'
      THEN 1  -- 重置计数器
      ELSE request_count + 1
    END AS new_count
  FROM rate_limits
  WHERE user_id = $1
  FOR UPDATE
)
UPDATE rate_limits
SET
  request_count = (SELECT new_count FROM current),
  window_start = CASE
    WHEN window_start < NOW() - INTERVAL '1 minute'
    THEN NOW()
    ELSE window_start
  END
WHERE user_id = $1
RETURNING request_count;
```



**或者更简单的窗口函数方式：**

```sql
CREATE TABLE api_requests (
  user_id INT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 检查限流
SELECT COUNT(*) FROM api_requests
WHERE user_id = $1
  AND created_at > NOW() - INTERVAL '1 minute';

-- 如果未超限，插入
INSERT INTO api_requests (user_id) VALUES ($1);

-- 定期清理旧请求
DELETE FROM api_requests WHERE created_at < NOW() - INTERVAL '5 minutes';
```



**Postgres 更好的场景：**

- 需要基于复杂逻辑进行限流（不只是计数）
- 希望限流数据与业务逻辑在同一事务中

**Redis 更好的场景：**

- 需要亚毫秒级限流
- 超高吞吐量（每秒数百万请求）

------

## PostgreSQL 功能 #5：使用 JSONB 管理会话

**Redis：**

```javascript
await redis.set(`session:${sessionId}`, JSON.stringify(sessionData), 'EX', 86400);
```



**PostgreSQL：**

```sql
CREATE TABLE sessions (
  id TEXT PRIMARY KEY,
  data JSONB NOT NULL,
  expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_sessions_expires ON sessions(expires_at);

-- 插入/更新
INSERT INTO sessions (id, data, expires_at)
VALUES ($1, $2, NOW() + INTERVAL '24 hours')
ON CONFLICT (id) DO UPDATE
  SET data = EXCLUDED.data,
      expires_at = EXCLUDED.expires_at;

-- 读取
SELECT data FROM sessions
WHERE id = $1 AND expires_at > NOW();
```



**额外好处：JSONB 操作符**

你可以查询会话内部数据：

```sql
-- 查找特定用户的所有会话
SELECT * FROM sessions
WHERE data->>'userId' = '123';

-- 查找具有特定角色的会话
SELECT * FROM sessions
WHERE data->'user'->>'role' = 'admin';
```



**这在 Redis 中做不到！**

------

## 真实基准测试

我在生产数据集上运行了基准测试：

### 测试配置

- **硬件：** AWS RDS db.t3.medium（2 vCPU，4GB RAM）
- **数据集：** 100 万条缓存记录，1 万条会话
- **工具：** pgbench（自定义脚本）

### 结果

| 操作           | Redis  | PostgreSQL | 差异           |
| :------------- | :----- | :--------- | :------------- |
| **缓存 SET**   | 0.05ms | 0.08ms     | 慢 60%         |
| **缓存 GET**   | 0.04ms | 0.06ms     | 慢 50%         |
| **发布/订阅**  | 1.2ms  | 3.1ms      | 慢 158%        |
| **队列入队**   | 0.08ms | 0.15ms     | 慢 87%         |
| **队列出队**   | 0.12ms | 0.31ms     | 慢 158%        |

**PostgreSQL 确实更慢...但是：**

- 所有操作仍在 1ms 以内
- 消除了到 Redis 的网络往返
- 降低了基础设施复杂性

### 组合操作（真正的优势）

**场景：** 插入数据 + 使缓存失效 + 通知订阅者

**使用 Redis：**

```javascript
await db.query('INSERT INTO posts ...');       // 2ms
await redis.del('posts:latest');                // 1ms（网络往返）
await redis.publish('posts:new', data);         // 1ms（网络往返）
// 总计: ~4ms
```



**使用 PostgreSQL：**

```sql
BEGIN;
INSERT INTO posts ...;                          -- 2ms
DELETE FROM cache WHERE key = 'posts:latest';  -- 0.1ms（同一连接）
NOTIFY posts_new, '...';                        -- 0.1ms（同一连接）
COMMIT;
-- 总计: ~2.2ms
```



**组合操作时 PostgreSQL 更快。**

------

## 何时保留 Redis

**以下情况不要替换 Redis：**

### 1. 需要极致性能

```
Redis: 100,000+ ops/sec（单实例）
Postgres: 10,000-50,000 ops/sec
```



如果你每秒需要数百万次缓存读取，保留 Redis。

### 2. 使用 Redis 特有的数据结构

**Redis 有：**

- 有序集合（排行榜）
- HyperLogLog（唯一计数估算）
- 地理空间索引
- 流（高级发布/订阅）

**Postgres 有等效方案但更笨重：**

```sql
-- Postgres 中的排行榜（更慢）
SELECT user_id, score
FROM leaderboard
ORDER BY score DESC
LIMIT 10;

-- vs Redis
ZREVRANGE leaderboard 0 9 WITHSCORES
```



### 3. 架构要求独立的缓存层

如果你的架构强制要求独立的缓存层（例如微服务），保留 Redis。

------

## 迁移策略

**不要一夜之间移除 Redis。** 以下是我的做法：

### 阶段 1：并行运行（第 1 周）

```javascript
// 双写
await redis.set(key, value);
await pg.query('INSERT INTO cache ...');

// 从 Redis 读取（仍为主要）
let data = await redis.get(key);
```



**监控：** 对比命中率、延迟。

### 阶段 2：从 Postgres 读取（第 2 周）

```javascript
// 优先从 Postgres 读取
let data = await pg.query('SELECT value FROM cache WHERE key = $1', [key]);

// 回退到 Redis
if (!data) {
  data = await redis.get(key);
}
```



**监控：** 错误率、性能。

### 阶段 3：仅写入 Postgres（第 3 周）

```javascript
// 仅写入 Postgres
await pg.query('INSERT INTO cache ...');
```



**监控：** 一切正常吗？

### 阶段 4：移除 Redis（第 4 周）

```bash
# 关闭 Redis
# 观察错误
# 没有故障？成功！
```



------

## 代码示例：完整实现

### 缓存模块（PostgreSQL）

```javascript
// cache.js
class PostgresCache {
  constructor(pool) {
    this.pool = pool;
  }

  async get(key) {
    const result = await this.pool.query(
      'SELECT value FROM cache WHERE key = $1 AND expires_at > NOW()',
      [key]
    );
    return result.rows[0]?.value;
  }

  async set(key, value, ttlSeconds = 3600) {
    await this.pool.query(
      `INSERT INTO cache (key, value, expires_at)
       VALUES ($1, $2, NOW() + INTERVAL '${ttlSeconds} seconds')
       ON CONFLICT (key) DO UPDATE
         SET value = EXCLUDED.value,
             expires_at = EXCLUDED.expires_at`,
      [key, value]
    );
  }

  async delete(key) {
    await this.pool.query('DELETE FROM cache WHERE key = $1', [key]);
  }

  async cleanup() {
    await this.pool.query('DELETE FROM cache WHERE expires_at < NOW()');
  }
}

module.exports = PostgresCache;
```



### 发布/订阅模块

```javascript
// pubsub.js
class PostgresPubSub {
  constructor(pool) {
    this.pool = pool;
    this.listeners = new Map();
  }

  async publish(channel, message) {
    const payload = JSON.stringify(message);
    await this.pool.query('SELECT pg_notify($1, $2)', [channel, payload]);
  }

  async subscribe(channel, callback) {
    const client = await this.pool.connect();

    await client.query(`LISTEN ${channel}`);

    client.on('notification', (msg) => {
      if (msg.channel === channel) {
        callback(JSON.parse(msg.payload));
      }
    });

    this.listeners.set(channel, client);
  }

  async unsubscribe(channel) {
    const client = this.listeners.get(channel);
    if (client) {
      await client.query(`UNLISTEN ${channel}`);
      client.release();
      this.listeners.delete(channel);
    }
  }
}

module.exports = PostgresPubSub;
```



### 任务队列模块

```javascript
// queue.js
class PostgresQueue {
  constructor(pool) {
    this.pool = pool;
  }

  async enqueue(queue, payload, scheduledAt = new Date()) {
    await this.pool.query(
      'INSERT INTO jobs (queue, payload, scheduled_at) VALUES ($1, $2, $3)',
      [queue, payload, scheduledAt]
    );
  }

  async dequeue(queue) {
    const result = await this.pool.query(
      `WITH next_job AS (
        SELECT id FROM jobs
        WHERE queue = $1
          AND attempts < max_attempts
          AND scheduled_at <= NOW()
        ORDER BY scheduled_at
        LIMIT 1
        FOR UPDATE SKIP LOCKED
      )
      UPDATE jobs
      SET attempts = attempts + 1
      FROM next_job
      WHERE jobs.id = next_job.id
      RETURNING jobs.*`,
      [queue]
    );

    return result.rows[0];
  }

  async complete(jobId) {
    await this.pool.query('DELETE FROM jobs WHERE id = $1', [jobId]);
  }

  async fail(jobId, error) {
    await this.pool.query(
      `UPDATE jobs
       SET attempts = max_attempts,
           payload = payload || jsonb_build_object('error', $2)
       WHERE id = $1`,
      [jobId, error.message]
    );
  }
}

module.exports = PostgresQueue;
```



------

## 性能调优技巧

### 1. 使用连接池

```javascript
const { Pool } = require('pg');

const pool = new Pool({
  max: 20,  // 最大连接数
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 2000,
});
```



### 2. 添加合适的索引

```sql
CREATE INDEX CONCURRENTLY idx_cache_key ON cache(key) WHERE expires_at > NOW();
CREATE INDEX CONCURRENTLY idx_jobs_pending ON jobs(queue, scheduled_at)
  WHERE attempts < max_attempts;
```



### 3. 调优 PostgreSQL 配置

```
# postgresql.conf
shared_buffers = 2GB           # 内存的 25%
effective_cache_size = 6GB     # 内存的 75%
work_mem = 50MB                # 用于复杂查询
maintenance_work_mem = 512MB   # 用于 VACUUM
```



### 4. 定期维护

```sql
-- 每日执行
VACUUM ANALYZE cache;
VACUUM ANALYZE jobs;

-- 或启用自动清理（推荐）
ALTER TABLE cache SET (autovacuum_vacuum_scale_factor = 0.1);
```

## 结果：3 个月后

**节省的：**

- ✅ $100/月（不再需要 ElastiCache）
- ✅ 备份复杂度降低 50%
- ✅ 少监控一个服务
- ✅ 部署更简单（少一个依赖）

**失去的：**

- ❌ 缓存操作增加约 0.5ms 延迟
- ❌ Redis 的特殊数据结构（但我不需要）

**还会再做吗？** 对于这个场景，会的。

**会普遍推荐吗？** 不会。

------

## 决策矩阵

**以下情况用 Postgres 替换 Redis：**

- ✅ 你使用 Redis 进行简单的缓存/会话管理
- ✅ 缓存命中率 < 95%（大量写入）
- ✅ 你需要事务一致性
- ✅ 你能接受 0.1-1ms 的额外延迟
- ✅ 你是小团队，运维资源有限

**以下情况保留 Redis：**

- ❌ 你需要 100k+ ops/秒
- ❌ 你使用 Redis 数据结构（有序集合等）
- ❌ 你有专门的运维团队
- ❌ 亚毫秒级延迟至关重要
- ❌ 你需要异地复制

------

## 参考资源

**PostgreSQL 功能：**

- [LISTEN/NOTIFY 文档](https://www.postgresql.org/docs/current/sql-notify.html)
- [SKIP LOCKED](https://www.postgresql.org/docs/current/sql-select.html#SQL-FOR-UPDATE-SHARE)
- [UNLOGGED 表](https://www.postgresql.org/docs/current/sql-createtable.html)

**工具：**

- [pgBouncer](https://www.pgbouncer.org/) - 连接池
- [pg_stat_statements](https://www.postgresql.org/docs/current/pgstatstatements.html) - 查询性能分析

**替代方案：**

- [Graphile Worker](https://github.com/graphile/worker) - 基于 Postgres 的任务队列
- [pg-boss](https://github.com/timgit/pg-boss) - 另一个 Postgres 队列

------

## 总结

**我用 PostgreSQL 替换 Redis 实现了：**

1. 缓存 → UNLOGGED 表
2. 发布/订阅 → LISTEN/NOTIFY
3. 任务队列 → SKIP LOCKED
4. 会话 → JSONB 表

**结果：**

- 每月节省 $100
- 降低运维复杂度
- 稍慢（0.1-1ms）但可接受
- 保证事务一致性

**适合的场景：**

- 中小型应用
- 简单缓存需求
- 想减少组件数量

**不适合的场景：**

- 高性能要求（100k+ ops/秒）
- 使用 Redis 特有功能
- 有专门运维团队

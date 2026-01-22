-- =====================================================
-- ext-postgres 模块数据库表结构
-- =====================================================

-- =====================================================
-- 1. 缓存表 (UNLOGGED 表，高性能但不持久化)
-- =====================================================
CREATE UNLOGGED TABLE IF NOT EXISTS pg_cache (
    key TEXT PRIMARY KEY,
    value JSONB NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 过期时间索引，用于清理任务
CREATE INDEX IF NOT EXISTS idx_pg_cache_expires ON pg_cache(expires_at);

-- 通用更新时间触发器函数（供所有需要自动更新 updated_at 的表使用）
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 更新时间触发器
DROP TRIGGER IF EXISTS trg_pg_cache_updated_at ON pg_cache;
CREATE TRIGGER trg_pg_cache_updated_at
    BEFORE UPDATE ON pg_cache
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- 2. 任务队列表
-- =====================================================
CREATE TABLE IF NOT EXISTS pg_jobs (
    id BIGSERIAL PRIMARY KEY,
    queue TEXT NOT NULL,
    payload JSONB NOT NULL,
    status TEXT DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    attempts INT DEFAULT 0,
    max_attempts INT DEFAULT 3,
    error TEXT,
    scheduled_at TIMESTAMPTZ DEFAULT NOW(),
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 队列查询索引 (核心性能优化)
CREATE INDEX IF NOT EXISTS idx_pg_jobs_pending ON pg_jobs(queue, scheduled_at)
    WHERE status = 'PENDING' AND attempts < max_attempts;

-- 状态查询索引
CREATE INDEX IF NOT EXISTS idx_pg_jobs_status ON pg_jobs(queue, status);

-- =====================================================
-- 3. 限流计数表
-- =====================================================
CREATE TABLE IF NOT EXISTS pg_rate_limits (
    key TEXT PRIMARY KEY,
    request_count INT DEFAULT 0,
    window_start TIMESTAMPTZ DEFAULT NOW()
);

-- 窗口过期索引
CREATE INDEX IF NOT EXISTS idx_pg_rate_limits_window ON pg_rate_limits(window_start);

-- =====================================================
-- 4. 会话表 (JSONB 存储会话数据)
-- =====================================================
CREATE TABLE IF NOT EXISTS pg_sessions (
    session_id TEXT PRIMARY KEY,
    user_id TEXT,
    data JSONB NOT NULL DEFAULT '{}',
    expires_at TIMESTAMPTZ NOT NULL,
    last_accessed_at TIMESTAMPTZ DEFAULT NOW(),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 用户 ID 索引（查询用户的所有会话）
CREATE INDEX IF NOT EXISTS idx_pg_sessions_user ON pg_sessions(user_id) WHERE user_id IS NOT NULL;

-- 过期时间索引（清理过期会话）
CREATE INDEX IF NOT EXISTS idx_pg_sessions_expires ON pg_sessions(expires_at);

-- 更新时间触发器
DROP TRIGGER IF EXISTS trg_pg_sessions_updated_at ON pg_sessions;
CREATE TRIGGER trg_pg_sessions_updated_at
    BEFORE UPDATE ON pg_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 最后访问时间索引（清理不活跃会话）
CREATE INDEX IF NOT EXISTS idx_pg_sessions_last_accessed ON pg_sessions(last_accessed_at);

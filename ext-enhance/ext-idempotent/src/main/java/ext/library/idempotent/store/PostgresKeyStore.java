package ext.library.idempotent.store;

import ext.library.postgres.util.PostgresUtil;

import java.time.Duration;

/**
 * 基于 PostgreSQL 的幂等 Key 存储实现
 * <p>
 * 使用 UNLOGGED 表 + CTE 原子操作实现，适用于 PostgreSQL 部署场景。
 * 启动时自动建表，无需手动初始化。
 */
public class PostgresKeyStore implements KeyStore {

    @Override
    public boolean saveIfAbsent(String key, Duration duration) {
        return PostgresUtil.cacheSetNxEx(key, String.valueOf(System.currentTimeMillis()), duration);
    }

    @Override
    public void remove(String key) {
        PostgresUtil.cacheDelete(key);
    }

}

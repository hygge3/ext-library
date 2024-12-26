package ext.library.satoken.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.util.SaFoxUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import ext.library.json.util.JsonUtil;
import ext.library.redis.util.RedisUtil;
import ext.library.tool.constant.Symbol;

/**
 * Sa-Token 持久层接口 (使用框架自带 RedisUtils 实现 协议统一)
 * <p>
 * 采用 caffeine + redis 多级缓存 优化并发查询效率
 */
public class PlusSaTokenDao implements SaTokenDao {

    private static final Cache<String, Object> CAFFEINE = Caffeine.newBuilder()
            // 设置最后一次写入或访问后经过固定时间过期
            .expireAfterWrite(5, TimeUnit.SECONDS)
            // 初始的缓存空间大小
            .initialCapacity(100)
            // 缓存的最大条数
            .maximumSize(1000)
            .build();

    /**
     * 获取 Value，如无返空
     */
    @Override
    public String get(String key) {
        return (String) CAFFEINE.get(key, k -> RedisUtil.get(key));
    }

    /**
     * 写入 Value，并设定存活时间 (单位：秒)
     */
    @Override
    public void set(String key, String value, long timeout) {
        if (timeout == 0 || timeout <= NOT_VALUE_EXPIRE) {
            return;
        }
        // 判断是否为永不过期
        if (timeout == NEVER_EXPIRE) {
            RedisUtil.set(key, value);
        } else {
            RedisUtil.setEx(key, value, timeout, TimeUnit.SECONDS);
        }
        CAFFEINE.invalidate(key);
    }

    /**
     * 修修改指定 key-value 键值对 (过期时间不变)
     */
    @Override
    public void update(String key, String value) {
        if (RedisUtil.exists(key)) {
            RedisUtil.setExAndKeep(key, value);
            CAFFEINE.invalidate(key);
        }
    }

    /**
     * 删除 Value
     */
    @Override
    public void delete(String key) {
        RedisUtil.del(key);
    }

    /**
     * 获取 Value 的剩余存活时间 (单位：秒)
     */
    @Override
    public long getTimeout(String key) {
        long timeout = RedisUtil.ttl(key);
        return timeout < 0 ? timeout : timeout / 1000;
    }

    /**
     * 修改 Value 的剩余存活时间 (单位：秒)
     */
    @Override
    public void updateTimeout(String key, long timeout) {
        RedisUtil.expire(key, timeout);
    }

    /**
     * 获取 Object，如无返空
     */
    @Override
    public Object getObject(String key) {
        return CAFFEINE.get(key, k -> RedisUtil.get(key));
    }

    /**
     * 写入 Object，并设定存活时间 (单位：秒)
     */
    @Override
    public void setObject(String key, Object object, long timeout) {
        if (timeout == 0 || timeout <= NOT_VALUE_EXPIRE) {
            return;
        }
        // 判断是否为永不过期
        if (timeout == NEVER_EXPIRE) {
            RedisUtil.set(key, JsonUtil.toJson(object));
        } else {
            RedisUtil.set(key, JsonUtil.toJson(object), timeout);
        }
        CAFFEINE.invalidate(key);
    }

    /**
     * 更新 Object (过期时间不变)
     */
    @Override
    public void updateObject(String key, Object object) {
        if (RedisUtil.exists(key)) {
            RedisUtil.setExAndKeep(key, JsonUtil.toJson(object));
            CAFFEINE.invalidate(key);
        }
    }

    /**
     * 删除 Object
     */
    @Override
    public void deleteObject(String key) {
        RedisUtil.del(key);
    }

    /**
     * 获取 Object 的剩余存活时间 (单位：秒)
     */
    @Override
    public long getObjectTimeout(String key) {
        long timeout = RedisUtil.ttl(key);
        return timeout < 0 ? timeout : timeout / 1000;
    }

    /**
     * 修改 Object 的剩余存活时间 (单位：秒)
     */
    @Override
    public void updateObjectTimeout(String key, long timeout) {
        RedisUtil.expire(key, timeout);
    }

    /**
     * 搜索数据
     */
    @Override
    @SuppressWarnings(value = "unchecked")
    public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
        String keyStr = prefix + Symbol.ASTERISK + keyword + Symbol.ASTERISK;
        return (List<String>) CAFFEINE.get(keyStr, k -> {
            Collection<String> keys = RedisUtil.keys(keyStr);
            List<String> list = new ArrayList<>(keys);
            return SaFoxUtil.searchList(list, start, size, sortType);
        });
    }

}

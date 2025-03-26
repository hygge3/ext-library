package ext.library.redis.util;

import ext.library.core.util.SpringUtil;
import lombok.experimental.UtilityClass;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * 缓存操作工具类
 */
@UtilityClass
public class CacheUtil {

     static final CacheManager CACHE_MANAGER = SpringUtil.getBean(CacheManager.class);

    /**
     * 获取缓存值
     *
     * @param cacheNames 缓存组名称
     * @param key        缓存 key
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String cacheNames, Object key) {
        Cache.ValueWrapper wrapper = CACHE_MANAGER.getCache(cacheNames).get(key);
        return wrapper != null ? (T) wrapper.get() : null;
    }

    /**
     * 保存缓存值
     *
     * @param cacheNames 缓存组名称
     * @param key        缓存 key
     * @param value      缓存值
     */
    public void put(String cacheNames, Object key, Object value) {
        CACHE_MANAGER.getCache(cacheNames).put(key, value);
    }

    /**
     * 删除缓存值
     *
     * @param cacheNames 缓存组名称
     * @param key        缓存 key
     */
    public void evict(String cacheNames, Object key) {
        CACHE_MANAGER.getCache(cacheNames).evict(key);
    }

    /**
     * 清空缓存值
     *
     * @param cacheNames 缓存组名称
     */
    public void clear(String cacheNames) {
        CACHE_MANAGER.getCache(cacheNames).clear();
    }

}

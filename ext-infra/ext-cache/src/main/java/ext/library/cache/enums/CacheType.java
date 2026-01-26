package ext.library.cache.enums;

/**
 * 缓存操作类型
 *
 * @since 2025.08.29
 */
public enum CacheType {

    /**
     * 存取模式
     * <p>
     * 先查缓存，未命中则执行方法并缓存结果
     */
    FULL,

    /**
     * 强制更新模式
     * <p>
     * 执行方法并强制更新缓存，不查询现有缓存
     */
    PUT,

    /**
     * 删除模式
     * <p>
     * 先删除缓存，再执行方法
     */
    DELETE
}

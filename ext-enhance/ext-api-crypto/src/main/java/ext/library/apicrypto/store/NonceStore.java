package ext.library.apicrypto.store;

import java.time.Duration;

/**
 * 随机数存储 SPI
 * <p>
 * 用于接口签名的防重放：同一随机数在时间窗内只能使用一次。
 * 默认实现是 {@link CacheNonceStore}（全局共享，由 ext-cache 提供）；
 * 自定义实现必须保证多实例间共享，否则重放窗口跨实例不封闭。
 *
 * @since 4.1.0
 */
public interface NonceStore {

    /**
     * 尝试占用一个随机数
     *
     * @param nonce 随机数
     * @param ttl   占用时长
     *
     * @return {@code true} 表示首次占用成功；{@code false} 表示已存在（重放）
     */
    boolean tryAcquire(String nonce, Duration ttl);

}

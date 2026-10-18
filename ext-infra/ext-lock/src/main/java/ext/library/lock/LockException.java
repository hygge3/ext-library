package ext.library.lock;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;

import java.io.Serial;

/**
 * 未获取到分布式锁时抛出的异常
 */
public class LockException extends ExtException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 锁 key */
    private final transient String lockKey;

    /**
     * 构造加锁异常
     *
     * @param message 提示信息
     * @param lockKey 锁 key
     */
    public LockException(String message, String lockKey) {
        super(EmojiSymbol.LOCK, message);
        this.lockKey = lockKey;
    }

    public String getLockKey() {
        return lockKey;
    }

}

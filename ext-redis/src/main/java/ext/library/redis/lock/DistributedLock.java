package ext.library.redis.lock;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import ext.library.redis.lock.function.ExceptionHandler;
import ext.library.tool.core.Threads;
import ext.library.tool.holder.function.CheckedSupplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

/**
 * 分布式锁操作类
 */
public final class DistributedLock<T> implements Action<T>, StateHandler<T> {

    T result;

    String key;

    Long timeout;

    TimeUnit timeUnit;

    int retryCount;

    CheckedSupplier<T> executeAction;

    UnaryOperator<T> successAction;

    Supplier<T> lockFailAction;

    ExceptionHandler exceptionHandler = DistributedLock::throwException;

    @NotNull
    @Contract("->new")
    public static <T> Action<T> instance() {
        return new DistributedLock<>();
    }

    @Override
    @Contract("_,_,_,_->this")
    public StateHandler<T> action(String lockKey, long timeout, TimeUnit timeUnit, CheckedSupplier<T> action) {
        Assert.isTrue(this.executeAction == null, "必须设置执行方法");
        Assert.notNull(action, "执行方法不能为空");
        Assert.hasText(lockKey, "lock key 不能为空");
        this.executeAction = action;
        this.key = lockKey;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        return this;
    }

    @Contract("_->this")
    @Override
    public StateHandler<T> onSuccess(UnaryOperator<T> action) {
        Assert.isTrue(this.successAction == null, "必须设置加锁成功后方法");
        Assert.notNull(action, "加锁成功后方法不能为空");
        this.successAction = action;
        return this;
    }

    @Contract("_->this")
    @Override
    public StateHandler<T> onLockFail(Supplier<T> action) {
        Assert.isTrue(this.lockFailAction == null, "必须设置加锁失败后方法");
        Assert.notNull(action, "加锁失败后方法不能为空");
        this.lockFailAction = action;
        return this;
    }

    @Contract(value = "_->this", mutates = "this")
    @Override
    public StateHandler<T> onException(ExceptionHandler exceptionHandler) {
        Assert.notNull(exceptionHandler, "必须设置异常处理");
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    @Override
    @Contract(value = "_->this", mutates = "this")
    public StateHandler<T> retryCount(int retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    @Override
    public T lock() {
        String requestId = UUID.randomUUID().toString();

        if (Boolean.TRUE.equals(tryLock(requestId))) {
            boolean exResolved = false;
            T value = null;
            try {
                value = this.executeAction.get();
                this.result = value;
            } catch (Throwable e) {
                this.exceptionHandler.handle(e);
                exResolved = true;
            } finally {
                CacheLock.releaseLock(this.key, requestId);
            }
            if (!exResolved && this.successAction != null) {
                this.result = this.successAction.apply(value);
            }
        } else if (this.lockFailAction != null) {
            this.result = this.lockFailAction.get();
        }

        return this.result;

    }

    @NotNull
    private Boolean tryLock(String requestId) {
        Fibonacci fibonacci = new Fibonacci(10);
        int tryCount = 0;
        while (true) {
            tryCount++;

            Boolean lockSuccess = CacheLock.lock(this.key, requestId, this.timeout, this.timeUnit);
            if (Boolean.TRUE.equals(lockSuccess)) {
                return true;
            }

            if (this.retryCount >= 0 && tryCount > this.retryCount) {
                return false;
            }

            Threads.sleep(fibonacci.next());
        }
    }

    @SuppressWarnings("unchecked")
    @Contract(value = "_->fail", pure = true)
    private static <E extends Throwable> void throwException(Throwable t) throws E {
        throw (E) t;
    }

    static class Fibonacci {

        private long current;

        private long prev = 0;

        private boolean first = true;

        @Contract(pure = true)
        Fibonacci() {
            this(1);
        }

        @Contract(pure = true)
        Fibonacci(int initial) {
            this.current = initial;
        }

        public long next() {
            long next = this.current + this.prev;
            if (this.first) {
                this.first = false;
            } else {
                this.prev = this.current;
                this.current = next;
            }
            return next;
        }

    }

}

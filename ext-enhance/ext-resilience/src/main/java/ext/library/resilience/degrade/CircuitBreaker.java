package ext.library.resilience.degrade;

import java.time.Duration;
import java.util.function.LongSupplier;

/**
 * 熔断器（进程内固定窗口统计）
 * <p>
 * 状态机：{@code CLOSED} 正常放行并统计失败率 → 达阈值转 {@code OPEN} → 到期转 {@code HALF_OPEN}
 * 放一个探测请求 → 成功回 {@code CLOSED}、失败回 {@code OPEN}。
 * <p>
 * 所有状态变更都加锁：熔断器的调用频率就是业务调用频率，竞争集中在同一个对象上，
 * 用 CAS 只会把状态机写得更难读。
 */
public class CircuitBreaker {

    /**
     * 熔断状态
     */
    public enum State {
        /** 关闭：正常放行 */
        CLOSED,
        /** 打开：直接走降级 */
        OPEN,
        /** 半开：只放一个探测请求 */
        HALF_OPEN
    }

    private final int minCalls;
    private final int failureRate;
    private final Duration window;
    private final Duration openDuration;
    private final LongSupplier nanoClock;

    private State state = State.CLOSED;
    private long windowStartNanos;
    private int calls;
    private int failures;
    private long openedAtNanos;
    private boolean probing;
    private long probingSinceNanos;

    public CircuitBreaker(int minCalls, int failureRate, Duration window, Duration openDuration) {
        this(minCalls, failureRate, window, openDuration, System::nanoTime);
    }

    /**
     * @param nanoClock 时间源，测试用可控时钟
     */
    public CircuitBreaker(int minCalls, int failureRate, Duration window, Duration openDuration, LongSupplier nanoClock) {
        if (minCalls < 1) {
            throw new IllegalArgumentException("minCalls 必须 >= 1");
        }
        if (failureRate < 1 || failureRate > 100) {
            throw new IllegalArgumentException("failureRate 必须在 1-100 之间");
        }
        if (window.isNegative() || window.isZero()) {
            throw new IllegalArgumentException("window 必须 > 0");
        }
        if (openDuration.isNegative() || openDuration.isZero()) {
            throw new IllegalArgumentException("openDuration 必须 > 0");
        }
        this.minCalls = minCalls;
        this.failureRate = failureRate;
        this.window = window;
        this.openDuration = openDuration;
        this.nanoClock = nanoClock;
        this.windowStartNanos = nanoClock.getAsLong();
    }

    /**
     * 是否放行本次调用
     *
     * @return {@code true} 调用原方法；{@code false} 走降级
     */
    public synchronized boolean allowRequest() {
        long now = nanoClock.getAsLong();
        switch (state) {
            case OPEN -> {
                if (now - openedAtNanos < openDuration.toNanos()) {
                    return false;
                }
                state = State.HALF_OPEN;
                probing = true;
                probingSinceNanos = now;
                return true;
            }
            case HALF_OPEN -> {
                // 探测卡死不能锁死降级：超过一个 openDuration 还没结果就再放一个
                if (probing && now - probingSinceNanos < openDuration.toNanos()) {
                    return false;
                }
                probing = true;
                probingSinceNanos = now;
                return true;
            }
            default -> {
                return true;
            }
        }
    }

    /**
     * 记录一次成功
     */
    public synchronized void onSuccess() {
        if (state == State.HALF_OPEN) {
            reset(nanoClock.getAsLong());
            return;
        }
        record(true);
    }

    /**
     * 记录一次失败
     */
    public synchronized void onFailure() {
        if (state == State.HALF_OPEN) {
            trip(nanoClock.getAsLong());
            return;
        }
        record(false);
    }

    /**
     * 当前状态
     */
    public synchronized State getState() {
        return state;
    }

    /**
     * 统计窗口内的调用次数
     */
    public synchronized int getCalls() {
        return calls;
    }

    private void record(boolean success) {
        // 熔断打开期间在途请求的结果是旧数据，它会把 openedAt 一直往后推，熔断永远回不去
        if (state != State.CLOSED) {
            return;
        }
        long now = nanoClock.getAsLong();
        if (now - windowStartNanos >= window.toNanos()) {
            windowStartNanos = now;
            calls = 0;
            failures = 0;
        }
        calls++;
        if (!success) {
            failures++;
        }
        if (calls >= minCalls && failures * 100L >= (long) calls * failureRate) {
            trip(now);
        }
    }

    private void trip(long now) {
        state = State.OPEN;
        openedAtNanos = now;
        probing = false;
    }

    private void reset(long now) {
        state = State.CLOSED;
        windowStartNanos = now;
        calls = 0;
        failures = 0;
        probing = false;
    }

}

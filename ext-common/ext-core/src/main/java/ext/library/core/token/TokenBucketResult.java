package ext.library.core.token;

/**
 * 令牌桶一次取令牌的结果
 * <p>
 * GCRA 等价实现的实际状态就是「理论到达时间」（tat），三个后端各自原子维护它；
 * 这个值对象把 tat 换算成调用方关心的两个数：还能再取多少、被拒时要等多久。
 *
 * @param allowed          是否取到令牌
 * @param remaining        本次之后桶里还能立刻取出的令牌数（{@code 0..capacity}）
 * @param retryAfterMillis 被拒时至少等待多少毫秒才有 {@code permits} 个令牌；放行时为 0
 * @since 4.1.0
 */
public record TokenBucketResult(boolean allowed, long remaining, long retryAfterMillis) {

    /**
     * 由 GCRA 状态换算结果
     * <p>
     * {@code ahead} 是「理论到达时间 − 当前时间」：放行时传扣减之后的值，被拒时传当前值。
     * 取 {@code permits} 个令牌的条件是 {@code ahead <= interval * (capacity - permits)}。
     *
     * @param allowed       是否放行
     * @param aheadNanos    理论到达时间领先当前时间的纳秒数
     * @param intervalNanos 补一个令牌的间隔
     * @param permits       本次申请数
     * @param capacity      桶容量
     *
     * @return 换算结果
     */
    public static TokenBucketResult of(boolean allowed, long aheadNanos, long intervalNanos,
                                      long permits, long capacity) {
        // 向上取整：宁可少报一个可用令牌，不要让调用方拿到必然被拒的额度
        long consumed = aheadNanos <= 0 ? 0 : (aheadNanos + intervalNanos - 1) / intervalNanos;
        long remaining = Math.clamp(capacity - consumed, 0, capacity);
        if (allowed) {
            return new TokenBucketResult(true, remaining, 0);
        }
        long waitNanos = Math.max(0, aheadNanos - intervalNanos * (capacity - permits));
        return new TokenBucketResult(false, remaining, Math.max(1, (waitNanos + 999_999) / 1_000_000));
    }

}

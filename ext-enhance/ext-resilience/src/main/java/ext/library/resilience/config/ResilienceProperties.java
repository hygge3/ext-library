package ext.library.resilience.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 限流与降级配置
 *
 * @since 4.1.0
 */
@ConfigurationProperties(prefix = "ext.resilience")
public class ResilienceProperties {

    private final RateLimit rateLimit = new RateLimit();

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    /**
     * 限流配置
     */
    public static class RateLimit {

        /**
         * 令牌用量达到该百分比时打告警日志，0 表示关闭
         */
        private int warnThreshold = 80;

        public int getWarnThreshold() {
            return warnThreshold;
        }

        public void setWarnThreshold(int warnThreshold) {
            this.warnThreshold = warnThreshold;
        }

    }

}

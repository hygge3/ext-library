package ext.library.logback.config;

import ext.library.desensitize.DesensitizeStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 日志脱敏配置属性
 * <p>
 * 用 {@link DesensitizeStrategy} 枚举选择要启用的规则，与 JSON 脱敏同一套口径；
 * 配置为空列表等价于不脱敏。
 *
 * @see ext.library.logback.mask.LogMaskConverter
 */
@ConfigurationProperties(LogMaskProperties.PREFIX)
public class LogMaskProperties {

    public static final String PREFIX = "ext.logback.mask";

    /**
     * 启用的脱敏策略，按声明顺序依次执行
     * <p>
     * 默认：银行卡号、身份证号、手机号、邮箱、IPv4、键值对凭据。
     * {@code PASSWORD} 在日志侧按 {@code password=xxx} 键值对形式匹配。
     */
    private List<DesensitizeStrategy> strategies = new ArrayList<>(List.of(
            DesensitizeStrategy.BANK_CARD,
            DesensitizeStrategy.ID_CARD,
            DesensitizeStrategy.PHONE,
            DesensitizeStrategy.EMAIL,
            DesensitizeStrategy.IP,
            DesensitizeStrategy.PASSWORD));

    public List<DesensitizeStrategy> getStrategies() {
        return strategies;
    }

    public void setStrategies(List<DesensitizeStrategy> strategies) {
        this.strategies = strategies;
    }

}

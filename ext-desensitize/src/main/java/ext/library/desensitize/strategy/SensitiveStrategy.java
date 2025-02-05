package ext.library.desensitize.strategy;

import java.util.function.Function;

import ext.library.desensitize.util.DesensitizationUtil;
import lombok.AllArgsConstructor;

/**
 * 脱敏策略，枚举类，针对不同的数据定制特定的策略
 */
@AllArgsConstructor
public enum SensitiveStrategy implements IDesensitizeRule {

    /** 默认不处理 */
    DEFAULT(String::toString),
    /**
     * 身份证脱敏
     */
    ID_CARD(DesensitizationUtil::maskIdCardNo),

    /**
     * 手机号脱敏
     */
    PHONE(DesensitizationUtil::maskPhoneNumber),

    /**
     * 地址脱敏
     */
    ADDRESS(DesensitizationUtil::maskAddress),

    /**
     * 邮箱脱敏
     */
    EMAIL(DesensitizationUtil::maskEmail),

    /**
     * 银行卡
     */
    BANK_CARD(DesensitizationUtil::maskBankCardNo),
    ;

    // 可自行添加其他脱敏策略

    private final Function<String, String> desensitizer;

    /**
     * 脱敏操作
     *
     * @return {@link String}
     */
    @Override
    public Function<String, String> desensitize() {
        return desensitizer;
    }

}

package ext.library.desensitize;

import ext.library.desensitize.util.DesensitizeUtil;

import java.util.function.Function;

/**
 * 脱敏策略枚举
 * <p>
 * 针对不同类型的敏感数据提供预定义的脱敏策略
 */
public enum DesensitizeStrategy implements DesensitizeRule {

    /**
     * 不处理，原样返回
     */
    NONE(Function.identity()),

    /**
     * 身份证号脱敏
     * <p>示例：430123********432X
     */
    ID_CARD(DesensitizeUtil::maskIdCard),

    /**
     * 手机号脱敏
     * <p>示例：138****5678
     */
    PHONE(DesensitizeUtil::maskPhone),

    /**
     * 地址脱敏
     * <p>示例：北京市西城区******
     */
    ADDRESS(DesensitizeUtil::maskAddress),

    /**
     * 邮箱脱敏
     * <p>示例：t****@qq.com
     */
    EMAIL(DesensitizeUtil::maskEmail),

    /**
     * 银行卡号脱敏
     * <p>示例：622260**********1234
     */
    BANK_CARD(DesensitizeUtil::maskBankCard),

    /**
     * 中文姓名脱敏
     * <p>示例：张*梦
     */
    CHINESE_NAME(DesensitizeUtil::maskChineseName),

    /**
     * 密码脱敏
     * <p>示例：******
     */
    PASSWORD(DesensitizeUtil::maskPassword),

    /**
     * IP 地址脱敏
     * <p>示例：192.*.*.*
     */
    IP(DesensitizeUtil::maskIp);

    private final Function<String, String> desensitizer;

    DesensitizeStrategy(Function<String, String> desensitizer) {
        this.desensitizer = desensitizer;
    }

    @Override
    public Function<String, String> desensitize() {
        return desensitizer;
    }
}

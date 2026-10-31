package ext.library.logback.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 日志脱敏配置属性
 * <p>
 * 各开关只控制对应规则是否启用，全部关闭可达到「等同未脱敏」的效果。
 *
 * @see ext.library.logback.mask.LogMaskConverter
 */
@ConfigurationProperties(LogMaskProperties.PREFIX)
public class LogMaskProperties {

    public static final String PREFIX = "ext.logback.mask";

    /** 是否脱敏手机号 */
    private boolean phone = true;
    /** 是否脱敏身份证号 */
    private boolean idCard = true;
    /** 是否脱敏邮箱 */
    private boolean email = true;
    /** 是否脱敏银行卡号 */
    private boolean bankCard = true;
    /** 是否脱敏 IPv4 地址 */
    private boolean ip = true;
    /** 是否脱敏 {@code password=xxx} 这类键值对形式的凭据 */
    private boolean credential = true;

    public boolean isPhone() {
        return phone;
    }

    public void setPhone(boolean phone) {
        this.phone = phone;
    }

    public boolean isIdCard() {
        return idCard;
    }

    public void setIdCard(boolean idCard) {
        this.idCard = idCard;
    }

    public boolean isEmail() {
        return email;
    }

    public void setEmail(boolean email) {
        this.email = email;
    }

    public boolean isBankCard() {
        return bankCard;
    }

    public void setBankCard(boolean bankCard) {
        this.bankCard = bankCard;
    }

    public boolean isIp() {
        return ip;
    }

    public void setIp(boolean ip) {
        this.ip = ip;
    }

    public boolean isCredential() {
        return credential;
    }

    public void setCredential(boolean credential) {
        this.credential = credential;
    }

}

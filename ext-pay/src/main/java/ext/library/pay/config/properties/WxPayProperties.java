package ext.library.pay.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 微信支付属性
 */
@Data
@ConfigurationProperties(prefix = "ext.pay.wx")
public class WxPayProperties {

	/** 是否是沙箱环境 */
	private Boolean sandbox;

	private String appId;

	private String mchId;

	private String mckKey;

	private String returnUrl;

	private String notifyUrl;

}

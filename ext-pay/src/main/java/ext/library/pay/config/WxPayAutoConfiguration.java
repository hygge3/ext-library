package ext.library.pay.config;

import ext.library.pay.WxPay;
import ext.library.pay.config.properties.WxPayProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 微信支付自动配置
 */
@AutoConfiguration
@ConditionalOnClass(WxPay.class)
@EnableConfigurationProperties(WxPayProperties.class)
public class WxPayAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(WxPay.class)
	public WxPay wxPay( WxPayProperties properties) {

		WxPay wxPay = new WxPay(properties.getAppId(), properties.getMchId(), properties.getMckKey(),
				properties.getSandbox());

		wxPay.setReturnUrl(properties.getReturnUrl());
		wxPay.setNotifyUrl(properties.getNotifyUrl());
		return wxPay;
	}

}

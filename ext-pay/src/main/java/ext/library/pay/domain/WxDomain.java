package ext.library.pay.domain;

import java.util.HashMap;
import java.util.Map;

import ext.library.pay.WxPay;
import ext.library.pay.constants.WxPayConstant;
import ext.library.pay.enums.RequestSuffix;
import ext.library.pay.enums.SignType;
import ext.library.pay.response.WxPayResponse;
import ext.library.pay.util.WxPayUtil;
import ext.library.tool.core.Exceptions;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

/**
 * 微信域名
 */
public interface WxDomain {

	/**
	 * 主域名
	 */
	String MAIN1 = "https://api.mch.weixin.qq.com/";

	String MAIN2 = "https://api.weixin.qq.com/";

	/**
	 * 备用域名
	 */
	String BACKUP1 = "https://api2.mch.weixin.qq.com/";

	String BACKUP2 = "https://api2.weixin.qq.com/";

	/**
	 * 发起请求。根据微信建议，实现类最好拥有主备域名自动切换的功能
	 * @param params 参数
	 * @param rs 请求后缀
	 * @return java.util.Map<java.lang.String, java.lang.String>
	 */
	default Map<String, String> request(Map<String, String> params, RequestSuffix rs) {
		String res = "";
		try {
			res = sendRequest(params, rs);
			return WxPayUtil.xmlToMap(res);
		}
		catch (Exception e) {
			// 用于处理返回值异常情况
			LoggerFactory.getLogger(getClass()).error("微信支付请求失败！返回值:\n {}", res);
			throw Exceptions.unchecked(e);
		}
	}

	/**
	 * 发送请求
	 * @param params 参数
	 * @param rs 前缀
	 * @return java.lang.String
	 */
	String sendRequest(Map<String, String> params, RequestSuffix rs);

	/**
	 * 获取沙箱环境密钥
	 * @param wxPay 支付信息
	 * @return 微信支付响应信息
	 */
	default WxPayResponse sandbox(@NotNull WxPay wxPay) {
		HashMap<String, String> map = new HashMap<>();
		map.put("mch_id", wxPay.getMchId());
		map.put("nonce_str", WxPayUtil.generateNonceStr());
		// 设置签名类型
		map.put(WxPayConstant.FIELD_SIGN_TYPE, SignType.MD5.getStr());
		// 签名
		map.put(WxPayConstant.FIELD_SIGN, WxPayUtil.sign(map, wxPay.getMckKey()));

		return WxPayResponse.of(request(map, RequestSuffix.GETSIGNKEY));
	}

}

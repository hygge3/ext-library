package ext.library.pay.constants;

import java.math.BigDecimal;

import org.intellij.lang.annotations.Language;

/**
 * 微信支付常数
 */
public interface WxPayConstant {

    /**
     * 一百
     */
    BigDecimal HUNDRED = new BigDecimal("100");

    /**
     * 签名字段名
     */
    String FIELD_SIGN = "sign";

    /**
     * 签名类型字段名
     */
    String FIELD_SIGN_TYPE = "sign_type";

    /**
     * 回调成功返回值
     */
    @Language("XML")
    String CALLBACK_SUCCESS = """
            <xml>
              <return_code><![CDATA[SUCCESS]]></return_code>
              <return_msg><![CDATA[OK]]></return_msg>
            </xml>""";

    /**
     * 回调验签失败返回值
     */
    @Language("XML")
    String CALLBACK_SIGN_ERROR = """
            <xml>
              <return_code><![CDATA[FAIL]]></return_code>
              <return_msg><![CDATA[签名异常]]></return_msg>
            </xml>""";

}

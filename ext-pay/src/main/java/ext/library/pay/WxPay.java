package ext.library.pay;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;
import ext.library.pay.constants.WxPayConstant;
import ext.library.pay.domain.DefaultWxDomain;
import ext.library.pay.domain.WxDomain;
import ext.library.pay.enums.RequestSuffix;
import ext.library.pay.enums.SignType;
import ext.library.pay.enums.TradeType;
import ext.library.pay.response.WxPayCallback;
import ext.library.pay.response.WxPayOrderQueryResponse;
import ext.library.pay.response.WxPayResponse;
import ext.library.pay.util.WxPayUtil;
import ext.library.tool.$;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * 微信支付
 */
@Data
public class WxPay {

    private final String appId;

    private final String mchId;

    private final String mckKey;

    private String notifyUrl;

    private String returnUrl;

    /**
     * 是否使用沙箱
     */
    private boolean sandbox;

    /**
     * 域名策略，可是使用自定义实现
     */
    private WxDomain domain;

    public WxPay(String appId, String mchId, String mckKey, boolean sandbox) {
        this(appId, mchId, mckKey, sandbox, DefaultWxDomain.of(sandbox));
    }

    public WxPay(String appId, String mchId, String mckKey, boolean sandbox, WxDomain domain) {
        this.sandbox = sandbox;
        this.domain = domain;
        this.mchId = mchId;
        this.appId = appId;

        // 沙箱环境初始化
        if (sandbox) {
            this.mckKey = domain.sandbox(this).getSandboxSignKey();
        } else {
            this.mckKey = mckKey;
        }

    }

    /**
     * jsApi 支付
     *
     * @param sn     订单号
     * @param amount 支付金额，单位 元
     * @param ip     客户端 ip
     * @param body   商品描述
     */
    public WxPayResponse jsApiPay(String sn, BigDecimal amount, String ip, String body) {
        return jsApiPay(sn, amount, ip, body, this.notifyUrl);
    }

    public WxPayResponse jsApiPay(String sn, BigDecimal amount, String ip, String body, String notifyUrl) {
        return WxPayResponse.of(pay(sn, amount, ip, body, notifyUrl, TradeType.JSAPI));
    }

    /**
     * app 支付
     *
     * @param sn     订单号
     * @param amount 支付金额，单位 元
     * @param ip     客户端 ip
     * @param body   商品描述
     */
    public WxPayResponse appPay(String sn, BigDecimal amount, String ip, String body) {
        return appPay(sn, amount, ip, body, this.notifyUrl);
    }

    public WxPayResponse appPay(String sn, BigDecimal amount, String ip, String body, String notifyUrl) {
        return WxPayResponse.of(pay(sn, amount, ip, body, notifyUrl, TradeType.APP));
    }

    /**
     * native 支付
     *
     * @param sn     订单号
     * @param amount 支付金额，单位 元
     * @param body   商品描述
     */
    public WxPayResponse nativePay(String sn, BigDecimal amount, String body) {
        return nativePay(sn, amount, body, this.notifyUrl);
    }

    public WxPayResponse nativePay(String sn, BigDecimal amount, String body, String notifyUrl) {
        return WxPayResponse.of(pay(sn, amount, null, body, notifyUrl, TradeType.NATIVE));
    }

    /**
     * web 支付
     *
     * @param sn     订单号
     * @param amount 支付金额，单位 元
     * @param ip     客户端 ip
     * @param body   商品描述
     */
    public WxPayResponse webPay(String sn, BigDecimal amount, String ip, String body) {
        return webPay(sn, amount, ip, body, this.notifyUrl);
    }

    public WxPayResponse webPay(String sn, BigDecimal amount, String ip, String body, String notifyUrl) {
        return WxPayResponse.of(pay(sn, amount, ip, body, notifyUrl, TradeType.MWEB));
    }

    /**
     * 发起支付
     *
     * @param sn        订单号
     * @param amount    金额，单位 元
     * @param ip        ip
     * @param body      描述
     * @param notifyUrl 通知
     * @param tradeType 支付类型
     */
    public Map<String, String> pay(String sn, BigDecimal amount, String ip, String body, String notifyUrl,
                                   @NotNull TradeType tradeType) {
        Map<String, String> params = Maps.newHashMapWithExpectedSize(6);
        params.put("body", body);
        params.put("out_trade_no", sn);
        params.put("total_fee", yuanToFen(amount));
        params.put("spbill_create_ip", ip);
        params.put("notify_url", notifyUrl);
        params.put("trade_type", tradeType.toString());

        return request(params, RequestSuffix.UNIFIEDORDER);
    }

    /**
     * 查询订单
     *
     * @param sn   平台订单号
     * @param wxSn 微信订单号
     * @return 微信订单查询结果
     */
    public WxPayOrderQueryResponse query(String sn, String wxSn) {
        if (null == sn && null == wxSn) {
            throw new IllegalArgumentException("参数 sn 和 wxSn 不能同时为空");
        }
        Map<String, String> params = Maps.newHashMapWithExpectedSize(6);
        params.put("out_trade_no", sn);
        params.put("transaction_id", wxSn);
        return WxPayOrderQueryResponse.of(request(params, RequestSuffix.ORDERQUERY));
    }

    /**
     * 向微信发起请求
     *
     * @param params 参数
     * @param rs     请求后缀
     */
    public Map<String, String> request(@NotNull Map<String, String> params, RequestSuffix rs) {
        Map<String, String> map = Maps.newHashMapWithExpectedSize(params.size() + 3);
        map.putAll(params);

        // 添加必须参数
        map.put("appid", this.appId);
        map.put("mch_id", this.mchId);
        map.put("nonce_str", WxPayUtil.generateNonceStr());
        // 设置签名类型; 沙箱使用 md5, 正式使用 hmac sha256
        map.put(WxPayConstant.FIELD_SIGN_TYPE, this.sandbox ? SignType.MD5.getStr() : SignType.HMAC_SHA256.getStr());
        // 签名
        map.put(WxPayConstant.FIELD_SIGN, WxPayUtil.sign(map, this.mckKey));

        return this.domain.request(map, rs);
    }

    /**
     * 金额单位转换，元 转为 分
     *
     * @param amount 支付金额，单位 元
     * @return java.lang.String
     */
    public String yuanToFen(@NotNull BigDecimal amount) {
        return amount.multiply(WxPayConstant.HUNDRED).setScale(2, RoundingMode.UP).toBigInteger().toString();
    }

    /**
     * 验证回调签名
     *
     * @param callback 回调数据
     * @return java.lang.Boolean
     */
    public boolean checkSign(@NotNull WxPayCallback callback) {
        String sign = callback.getSign();
        // 原签名不存在时，直接失败
        if ($.isBlank(sign)) {
            return false;
        }

        Map<String, String> params = new HashMap<>(callback.getRaw());

        // 存在签名类型，直接验签
        if (params.containsKey(WxPayConstant.FIELD_SIGN_TYPE)) {
            return WxPayUtil.sign(params, this.mckKey).equals(sign);
        }

        // 两种签名类型都试一次
        if (WxPayUtil.sign(params, SignType.HMAC_SHA256, this.mckKey).equals(sign)) {
            return true;
        }

        return WxPayUtil.sign(params, SignType.MD5, this.mckKey).equals(sign);
    }

}

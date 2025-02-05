package ext.library.pay.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * 交易状态
 */
public enum TradeState {

    /**
     * 支付成功
     */
    SUCCESS,
    /**
     * 转入退款
     */
    REFUND,
    /**
     * 未支付
     */
    NOTPAY,
    /**
     * 已关闭
     */
    CLOSED,
    /**
     * 已撤销 (刷卡支付)
     */
    REVOKED,
    /**
     * 用户支付中
     */
    USERPAYING,
    /**
     * 支付失败 (其他原因，如银行返回失败)
     */
    PAYERROR,
    /**
     * 已接收，等待扣款
     */
    ACCEPT,
    /**
     * 异常
     */
    ERROR;

    @JsonCreator
    public static TradeState of(String status) {
        return switch (status) {
            case "SUCCESS" -> SUCCESS;
            case "REFUND" -> REFUND;
            case "NOTPAY" -> NOTPAY;
            case "CLOSED" -> CLOSED;
            case "REVOKED" -> REVOKED;
            case "USERPAYING" -> USERPAYING;
            case "PAYERROR" -> PAYERROR;
            case "ACCEPT" -> ACCEPT;
            default -> ERROR;
        };
    }

}

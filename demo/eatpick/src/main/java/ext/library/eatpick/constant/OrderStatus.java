package ext.library.eatpick.constant;

public interface OrderStatus {
    /** 取消 */
    Integer CANCEL = 0;
    /** 已下单 */
    Integer ORDER = 1;
    /** 制作中 */
    Integer COOK = 2;
    /** 已完成 */
    Integer DONE = 3;
    /** 已评价 */
    Integer REVIEW = 4;
}

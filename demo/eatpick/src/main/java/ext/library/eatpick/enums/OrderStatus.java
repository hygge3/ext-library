package ext.library.eatpick.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    CANCEL(0, "取消"),
    PLACE_ORDER(1, "已下单"),
    MAKING(2, "制作中"),
    FINISH(3, "已完成"),
    reviews(4, "已评价"),
    ;
    private final int code;
    private final String desc;
}

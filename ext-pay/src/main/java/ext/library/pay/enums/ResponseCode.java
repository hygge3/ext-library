package ext.library.pay.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 返回 code
 */
@Getter
@AllArgsConstructor
public enum ResponseCode {

    /**
     * 成功
     */
    SUCCESS,
    /**
     * 失败
     */
    FAIL,
    /**
     * 异常
     */
    ERROR;

    @JsonCreator
    public static ResponseCode of(String status) {
        return switch (status) {
            case "SUCCESS" -> SUCCESS;
            case "FAIL" -> FAIL;
            default -> ERROR;
        };
    }

}

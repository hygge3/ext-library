package ext.library.eatpick.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.AllArgsConstructor;

/**
 * 管理员状态
 *
 * @author zlh
 * @since 2024/05/29
 */
@AllArgsConstructor
public enum UserStatus {

    /**
     * 正常
     */
    NORMAL(1, "正常"),
    /**
     * 封禁
     */
    BAN(2, "封禁"),
    ;

    /**
     * 编码
     */
    @EnumValue
    public final int code;

    /**
     * 描述
     */
    public final String desc;

}

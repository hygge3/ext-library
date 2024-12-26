package ext.library.eatpick.param;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordParam {

    /**
     * 旧密码
     */
    private String oldPassword;
    /**
     * 新密码
     */
    private String newPassword;

}

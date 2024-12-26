package ext.library.eatpick.param;

import ext.library.eatpick.Env;
import ext.library.eatpick.entity.User;
import ext.library.web.validation.constraints.OneOfStrings;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@AutoMapper(target = User.class)
public class RegisterParam {
    /**
     * 用户名
     */
    @Length(min = 3, max = 20)
    private String username;
    /**
     * 密码
     */
    @Length(min = 6, max = 20)
    private String password;
    /**
     * 昵称
     */
    @Length(min = 1, max = 20)
    private String nickname;
    /**
     * 头像
     */
    private String avatar;

    /** 检查代码 */
    @OneOfStrings(value = Env.CHECK_CODE, message = "校验码错误，不允许注册")
    private String checkCode;
    /** 验证码 */
    private String captcha;
}

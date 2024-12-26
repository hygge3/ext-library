package ext.library.eatpick.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ext.library.eatpick.entity.User;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户
 */
@Getter
@Setter
@AutoMapper(target = User.class)
public class AuthUser {
    /**
     * 主键
     */
    private Long id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    @JsonIgnore
    private String password;

    /**
     * 昵称
     */
    private String nickname;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 角色：customer 顾客；chef 厨师；admin 管理员
     */
    private String role;

}

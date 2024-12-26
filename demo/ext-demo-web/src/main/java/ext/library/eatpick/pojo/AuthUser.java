package ext.library.eatpick.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ext.library.desensitize.annotion.Sensitive;
import ext.library.desensitize.strategy.SensitiveStrategy;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户
 */
@Getter
@Setter
public class AuthUser {
    private Long id;
    private String nickname;
    private String username;
    @Sensitive(strategy = SensitiveStrategy.PHONE)
    private String phone;
    @JsonIgnore
    private String password;
}

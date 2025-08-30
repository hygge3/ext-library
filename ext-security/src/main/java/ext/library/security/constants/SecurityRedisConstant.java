package ext.library.security.constants;

/**
 * <p>
 * 认证相关常量
 * </p>
 */
public interface SecurityRedisConstant {

    /**
     * key 前缀
     */
    String KEY_PREFIX = "security";

    /**
     * session 信息 key {} loginId
     */
    String SESSION_INFO_KEY = KEY_PREFIX + ":session:{}";

    /**
     * 登录 ID 关联 session 信息 key {} token
     */
    String TOKEN_REL_LOGIN_ID_KEY = KEY_PREFIX + ":token:{}";

    /**
     * 登录账户密码错误次数 redis key
     */
    String PWD_ERR_CNT_KEY = KEY_PREFIX + ":pwd_err_cnt:";

    /**
     * 三方认证 redis key
     */
    String SOCIAL_AUTH_CODE_KEY = KEY_PREFIX + ":social_auth_codes:";
}
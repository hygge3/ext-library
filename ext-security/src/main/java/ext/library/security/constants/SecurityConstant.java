package ext.library.security.constants;

/**
 * <p>
 * 常量
 * </p>
 */
public interface SecurityConstant {

	/**
	 * 不过期
	 */
	Long NON_EXPIRING = -1L;

	/**
	 * 不限制
	 */
	Integer NON_LIMIT = -1;

	/**
	 * token-正常状态
	 */
	String TOKEN_STATE_NORMAL = "1";

	/**
	 * token-被踢下线
	 */
	String TOKEN_STATE_KICKED_OFFLINE = "2";

	/**
	 * token-被顶下线
	 */
	String TOKEN_STATE_REPLACE_OFFLINE = "3";

	/**
	 * token-封禁
	 */
	String TOKEN_STATE_BANNED = "4";

	/**
	 * authorization 前缀
	 */
	String AUTHORIZATION_PREFIX = "Bearer ";

	/**
	 * 未知
	 */
	String UNKNOWN = "unknown";

	/**
	 * security session id
	 */
	String SECURITY_SESSION_ID = "security_session_id";

	/**
	 * 自定义 token 参数名称
	 */
	String SECURITY_CUSTOM_IDENTITY_TOKEN = "security_custom_identity_token";

}

package ext.library.security.repository;

import ext.library.security.domain.SecuritySession;
import ext.library.security.domain.SecurityToken;
import java.util.List;

/**
 * <p>
 * 持久化接口定义
 * </p>
 */
public interface SecurityRepository {

	// -----------------------security session----------------------------------

	/**
	 * 根据登录 Id 查询 SecuritySession
	 * @param loginId 登录 Id
	 * @return SecuritySession
	 */
	SecuritySession getSecuritySessionByLoginId(String loginId);

	/**
	 * 根据登录 Id 查询 SecuritySession 的过期时间
	 * @param loginId 登录 Id
	 * @return 过期时间（秒）
	 */
	Long getSessionTimeoutByLoginId(String loginId);

	/**
	 * 保存 SecuritySession
	 * @param session SecuritySession
	 * @return true 成功 false 失败
	 */
	boolean saveSecuritySession(SecuritySession session);

	/**
	 * 根据登录 Id 删除 SecuritySession
	 * @param loginId 登录 Id
	 * @return true 成功 false 失败
	 */
	boolean removeSecuritySessionByLoginId(String loginId);

	// -----------------------security token----------------------------------

	/**
	 * 根据 tokenValue 获取 SecurityToken
	 * @param tokenValue token
	 * @return 登录 Id
	 */
	SecurityToken getSecurityTokenByTokenValue(String tokenValue);

	/**
	 * 根据 tokenValue 获取活跃时间
	 * @param tokenValue token
	 * @return 活跃时间点
	 */
	String getActivityTimeByTokenValue(String tokenValue);

	/**
	 * 根据 tokenValue 获取 token 超时时间
	 * @param tokenValue token
	 * @return 超时时间（秒）
	 */
	Long getTokenTimeOutByTokenValue(String tokenValue);

	/**
	 * 根据 tokenValue 获取 token 活跃超时时间
	 * @param tokenValue token
	 * @return 活跃超时时间（秒）
	 */
	Long getTokenActivityTimeOutByTokenValue(String tokenValue);

	/**
	 * 保存 token 信息
	 * @param token token 信息
	 * @return true 成功 false 失败
	 */
	boolean saveToken(SecurityToken token);

	/**
	 * 根据 tokenValue 删除 token 信息
	 * @param tokenValue token
	 * @return true 成功 false 失败
	 */
	boolean removeTokenByTokenValue(String tokenValue);

	/**
	 * 根据 token 值续约
	 * @param tokenValue token 值
	 */
	boolean renewalTokenByTokenValue(String tokenValue);

	/**
	 * 查询所有 token 列表
	 * @param tokenValue token 值，支持模糊匹配
	 * @param sortedDesc 是否降序
	 * @return token 列表
	 */
	List<String> queryTokenList(String tokenValue, boolean sortedDesc);

}

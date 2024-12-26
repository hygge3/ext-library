package ext.library.security.repository;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import ext.library.json.util.JsonUtil;
import ext.library.redis.util.RedisUtil;
import ext.library.security.constants.SecurityConstant;
import ext.library.security.constants.SecurityRedisConstant;
import ext.library.security.domain.SecuritySession;
import ext.library.security.domain.SecurityToken;
import ext.library.tool.$;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * redis 存储
 * </p>
 */
@Slf4j
public class SecurityRedisRepository implements SecurityRepository {

	/**
	 * 根据登录 Id 查询 SecuritySession
	 * @param loginId 登录 Id
	 * @return SecuritySession
	 */
	@Override
	public SecuritySession getSecuritySessionByLoginId(String loginId) {
		String redisKey = MessageFormat.format(SecurityRedisConstant.SESSION_INFO_KEY, loginId);
		SecuritySession session = RedisUtil.get(redisKey, SecuritySession.class);
		return Objects.isNull(session) ? null : session;
	}

	/**
	 * 根据登录 Id 查询 SecuritySession 的过期时间
	 * @param loginId 登录 Id
	 * @return Long
	 */
	@Override
	public Long getSessionTimeoutByLoginId(String loginId) {
		String redisKey = MessageFormat.format(SecurityRedisConstant.SESSION_INFO_KEY, loginId);
		return RedisUtil.ttl(redisKey);
	}

	@Override
	public boolean saveSecuritySession(@NotNull SecuritySession session) {
		String redisKey = MessageFormat.format(SecurityRedisConstant.SESSION_INFO_KEY, session.getLoginId());
		String sessionJson = JsonUtil.toJson(session);
		if (null == session.getTimeout() || SecurityConstant.NON_EXPIRING.equals(session.getTimeout())) {
			RedisUtil.set(redisKey, sessionJson);
		}
		else {
			// session 为每个 token 共用，所以每次更新时超时时间都重置为配置的最大超时时间
			RedisUtil.setEx(redisKey, sessionJson, session.getTimeout(), TimeUnit.SECONDS);
		}
		return true;
	}

	/**
	 * 根据登录 Id 删除 SecuritySession
	 * @param loginId 登录 Id
	 * @return boolean
	 */
	@Override
	public boolean removeSecuritySessionByLoginId(String loginId) {
		// 删除 session 信息
		String redisKey = MessageFormat.format(SecurityRedisConstant.SESSION_INFO_KEY, loginId);
		return Boolean.TRUE.equals(RedisUtil.del(redisKey));
	}

	/**
	 * 根据 tokenValue 获取 SecurityToken
	 * @param tokenValue token
	 * @return SecurityToken
	 */
	@Override
	public SecurityToken getSecurityTokenByTokenValue(String tokenValue) {
		String redisKey = MessageFormat.format(SecurityRedisConstant.TOKEN_REL_LOGIN_ID_KEY, tokenValue);
		SecurityToken token = RedisUtil.get(redisKey, SecurityToken.class);
		return Objects.isNull(token) ? null : token;
	}

	/**
	 * 根据 tokenValue 获取活跃时间
	 * @param tokenValue token
	 * @return String
	 */
	@Override
	public String getActivityTimeByTokenValue(String tokenValue) {
		SecurityToken securityToken = getSecurityTokenByTokenValue(tokenValue);
		if (Objects.isNull(securityToken)) {
			return null;
		}
		return securityToken.getActivityTime();
	}

	/**
	 * 根据 tokenValue 获取 token 活跃超时时间
	 * @param tokenValue token
	 * @return Long
	 */
	@Override
	public Long getTokenTimeOutByTokenValue(String tokenValue) {
		String redisKey = MessageFormat.format(SecurityRedisConstant.TOKEN_REL_LOGIN_ID_KEY, tokenValue);
		return RedisUtil.ttl(redisKey);
	}

	/**
	 * 根据 tokenValue 获取 token 活跃超时时间
	 * @param tokenValue token
	 * @return Long
	 */
	@Override
	public Long getTokenActivityTimeOutByTokenValue(String tokenValue) {
		SecurityToken securityToken = getSecurityTokenByTokenValue(tokenValue);
		if (Objects.isNull(securityToken)) {
			return null;
		}
		String activityTime = securityToken.getActivityTime();
		// 计算超时时间
		long timeout = securityToken.getActivityTimeout();
		if (SecurityConstant.NON_EXPIRING.equals(timeout)) {
			return SecurityConstant.NON_EXPIRING;
		}
		// 计算剩余时间

		long second = $.parseDateTime(activityTime).plusSeconds(timeout).toEpochSecond(ZoneOffset.ofHours(8))
				- LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8));
		return second > 0 ? second : 0L;
	}

	/**
	 * 保存 token 信息
	 * @param token token 信息
	 * @return boolean
	 */
	@Override
	public boolean saveToken(@NotNull SecurityToken token) {
		String redisKey = MessageFormat.format(SecurityRedisConstant.TOKEN_REL_LOGIN_ID_KEY, token.getToken());
		String tokenJson = JsonUtil.toJson(token);
		if (null == token.getTimeout() || SecurityConstant.NON_EXPIRING.equals(token.getTimeout())) {
			RedisUtil.set(redisKey, tokenJson);
		}
		else {
			// 获取超时时间
			Long timeout = RedisUtil.ttl(redisKey);
			if (Objects.isNull(timeout) || timeout <= 0) {
				timeout = token.getTimeout();
			}
			// 重新保存 token，超时时间需要延用创建 token 时的超时时间
			RedisUtil.setEx(redisKey, tokenJson, timeout, TimeUnit.SECONDS);
		}
		return true;
	}

	/**
	 * 根据 tokenValue 删除 token 信息
	 * @param tokenValue token
	 * @return boolean
	 */
	@Override
	public boolean removeTokenByTokenValue(String tokenValue) {
		String redisKey = MessageFormat.format(SecurityRedisConstant.TOKEN_REL_LOGIN_ID_KEY, tokenValue);
		return Boolean.TRUE.equals(RedisUtil.del(redisKey));
	}

	/**
	 * 根据 token 值续约
	 * @param tokenValue token 值
	 * @return boolean
	 */
	@Override
	public boolean renewalTokenByTokenValue(String tokenValue) {
		SecurityToken securityToken = getSecurityTokenByTokenValue(tokenValue);
		if (Objects.isNull(securityToken)) {
			return false;
		}
		securityToken.setActivityTime($.formatDateTime(LocalDateTime.now()));
		return saveToken(securityToken);
	}

	/**
	 * 查询所有 token 列表
	 * @param tokenValue token 值，支持模糊匹配
	 * @param sortedDesc 是否降序
	 * @return List<String>
	 */
	@Override
	public List<String> queryTokenList(String tokenValue, boolean sortedDesc) {
		String redisKey = MessageFormat.format(SecurityRedisConstant.TOKEN_REL_LOGIN_ID_KEY,
				$.isNotBlank(tokenValue) ? tokenValue + "*" : "*");
		Set<String> setList = RedisUtil.keys(redisKey);
		List<String> list = null == setList ? new ArrayList<>()
				: setList.stream()
					.map(key -> key.substring(key.lastIndexOf(":") + 1))
					.sorted(Comparator.comparing(String::toString))
					.collect(Collectors.toList());
		if (Boolean.TRUE.equals(sortedDesc)) {
			Collections.reverse(list);
		}
		return list;
	}

}

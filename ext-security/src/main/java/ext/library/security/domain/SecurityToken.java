package ext.library.security.domain;

import ext.library.core.util.SpringUtil;
import ext.library.json.util.JsonUtil;
import ext.library.security.repository.SecurityRepository;
import ext.library.tool.core.Exceptions;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * SecurityToken
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SecurityToken implements Serializable {

	@Serial
	 static final long serialVersionUID = 1L;

	/**
	 * token
	 */
	 String token;

	/**
	 * 登录 Id
	 */
	 String loginId;

	/**
	 * 设备类型
	 */
	 String deviceType;

	/**
	 * 过期时间 单位秒
	 */
	 Long timeout;

	/**
	 * 活跃时间 格式 yyyy-MM-dd HH:mm:ss
	 */
	 String activityTime;

	/**
	 * 活跃超时时间 单位秒
	 */
	 Long activityTimeout;

	/**
	 * 状态标记 1 正常 2 被踢下线 3 被顶下线 4 封禁
	 */
	 String state;

	/**
	 * 创建时间 格式 yyyy-MM-dd HH:mm:ss
	 */
	 String createTime;

	/**
	 * 更新时间 格式 yyyy-MM-dd HH:mm:ss
	 */
	 String updateTime;

	/**
	 * token 挂载数据
	 */
	 final Map<String, Object> tokenMountData = new HashMap<>();

	/**
	 * 设置存储信息
	 * @param key key 值
	 * @param value value 值
	 * @return SecuritySession
	 */
	public SecurityToken setAttribute(String key, Object value) {
		tokenMountData.put(key, value);
		flushTokenStorage();
		return this;
	}

	/**
	 * 获取存储信息
	 * @param key key 值
	 * @return Object
	 */
	public Object getAttribute(String key) {
		return tokenMountData.get(key);
	}

	/**
	 * 获取存储信息
	 * @param key 字符串 key
	 * @param tClass 类型
	 * @param <T> 泛型
	 * @return 转换后的对象
	 */
	public <T> T getAttribute(String key, Class<T> tClass) {
		Object obj = getAttribute(key);
		return JsonUtil.readObj(JsonUtil.toJson(obj), tClass);
	}

	/**
	 * 刷新 token 存储
	 */
	public void flushTokenStorage() {
		SecurityRepository repository = SpringUtil.getBean(SecurityRepository.class);

		boolean result = repository.saveToken(this);
		if (!result) {
			throw Exceptions.throwOut("保存 token 认证数据失败");
		}
	}

}

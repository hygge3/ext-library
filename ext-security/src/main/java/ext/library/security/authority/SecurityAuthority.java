package ext.library.security.authority;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 权限认证接口
 * </p>
 */
public interface SecurityAuthority {

	/**
	 * 权限码集合
	 * @param loginId 登录 Id
	 * @return List<String>
	 */
	default List<String> getPermissionCodeList(String loginId) {
		return Collections.emptyList();
	}

	/**
	 * 角色码集合
	 * @param loginId 登录 Id
	 * @return List<String>
	 */
	default List<String> getRoleCodeList(String loginId) {
		return Collections.emptyList();
	}

}

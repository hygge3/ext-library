package ext.library.satoken.domain;

import java.util.Set;

/**
 * 用户信息
 */
public record SecurityUser(
		/*
		 * 登录 Id
		 */
		String loginId,
		/*
		 * 租户 ID
		 */
		String tenantId,
		/*
		 * 登录用户名
		 */
		String username,
		/*
		 * 姓名
		 */
		String name,
		/*
		 * 密码
		 */
		String password,
		/*
		 * 菜单权限
		 */
		Set<String> permissions,
		/*
		 * 角色权限
		 */
		Set<String> roles) {
}

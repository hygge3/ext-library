package ext.library.eatpick.service;

import java.util.List;

import com.mybatisflex.core.service.IService;
import ext.library.eatpick.entity.Permission;
import ext.library.eatpick.entity.Role;
import ext.library.eatpick.entity.User;

/**
 * 用户 服务层。
 *
 * @author Auto Codegen By Ext
 * @since 2024-11-29
 */
public interface UserService extends IService<User> {
    void assignRole(List<Long> userIds, List<Long> roleIds);
    List<Role> rolesByUserId(Long userId);
    void assignPermission(List<Long> roleIds, List<Long> permissionIds);
    List<Permission> permissionsByRoleId(Long roleId);
}

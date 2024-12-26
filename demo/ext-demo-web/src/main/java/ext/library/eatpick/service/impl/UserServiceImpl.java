package ext.library.eatpick.service.impl;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import ext.library.eatpick.entity.Permission;
import ext.library.eatpick.entity.Role;
import ext.library.eatpick.entity.RolePermission;
import ext.library.eatpick.entity.User;
import ext.library.eatpick.entity.UserRole;
import ext.library.eatpick.mapper.UserMapper;
import ext.library.eatpick.service.PermissionService;
import ext.library.eatpick.service.RolePermissionService;
import ext.library.eatpick.service.RoleService;
import ext.library.eatpick.service.UserRoleService;
import ext.library.eatpick.service.UserService;
import ext.library.tool.$;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static ext.library.eatpick.entity.table.RolePermissionTableDef.ROLE_PERMISSION;
import static ext.library.eatpick.entity.table.UserRoleTableDef.USER_ROLE;

/**
 * 用户 服务层实现。
 *
 * @author Auto Codegen By Ext
 * @since 2024-11-29
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    private final RoleService roleService;

    private final PermissionService permissionService;

    private final UserRoleService userRoleService;

    private final RolePermissionService rolePermissionService;

    /**
     * 分配角色
     *
     * @param userIds 用户 ID
     * @param roleIds 角色 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRole(List<Long> userIds, List<Long> roleIds) {
        userRoleService.remove(USER_ROLE.USER_ID.in(userIds));
        List<UserRole> userRoles = Lists.newArrayListWithCapacity(userIds.size() * roleIds.size());
        for (Long userId : userIds) {
            for (Long roleId : roleIds) {
                userRoles.add(new UserRole(userId, roleId));
            }
        }
        userRoleService.saveBatch(userRoles);
    }

    public List<Role> rolesByUserId(Long userId) {
        List<UserRole> userRoles = userRoleService.list(USER_ROLE.USER_ID.eq(userId));
        if ($.isEmpty(userRoles)) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();
        return roleService.listByIds(roleIds);
    }

    /**
     * 分配权限
     *
     * @param roleIds       角色 ID
     * @param permissionIds 权限 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignPermission(List<Long> roleIds, List<Long> permissionIds) {
        rolePermissionService.remove(ROLE_PERMISSION.ROLE_ID.in(roleIds));
        List<RolePermission> roleRolePermissions = Lists.newArrayListWithCapacity(permissionIds.size() * roleIds.size());
        for (Long roleId : roleIds) {
            for (Long permissionId : permissionIds) {
                roleRolePermissions.add(new RolePermission(roleId, permissionId));
            }
        }
        rolePermissionService.saveBatch(roleRolePermissions);
    }

    public List<Permission> permissionsByRoleId(Long roleId) {
        List<RolePermission> rolePermissions = rolePermissionService.list(ROLE_PERMISSION.ROLE_ID.eq(roleId));
        if ($.isEmpty(rolePermissions)) {
            return Collections.emptyList();
        }
        List<Long> permissionIds = rolePermissions.stream().map(RolePermission::getPermissionId).toList();
        return permissionService.listByIds(permissionIds);
    }

}

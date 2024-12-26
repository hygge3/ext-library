package ext.library.eatpick.config;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import ext.library.eatpick.service.PermissionService;
import ext.library.eatpick.service.RolePermissionService;
import ext.library.eatpick.service.RoleService;
import ext.library.eatpick.service.UserRoleService;
import ext.library.json.util.JsonUtil;
import ext.library.redis.util.RedisUtil;
import ext.library.security.authority.SecurityAuthority;
import ext.library.tool.$;
import ext.library.tool.core.ThreadPools;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static ext.library.eatpick.entity.table.PermissionTableDef.PERMISSION;
import static ext.library.eatpick.entity.table.RolePermissionTableDef.ROLE_PERMISSION;
import static ext.library.eatpick.entity.table.RoleTableDef.ROLE;
import static ext.library.eatpick.entity.table.UserRoleTableDef.USER_ROLE;

@Component
@RequiredArgsConstructor
public class SecurityAuthorityImpl implements SecurityAuthority {

    private final RoleService roleService;
    private final UserRoleService userRoleService;
    private final PermissionService permissionService;
    private final RolePermissionService rolePermissionService;

    private static final long TIME_OUT = TimeUnit.MINUTES.toSeconds(5);

    /**
     * 权限码集合
     *
     * @param loginId 登录 Id
     * @return List<String>
     */
    public List<String> getPermissionCodeList(String loginId) {
        String redisKey = $.format("security:permission:{}", loginId);
        String codeJson = RedisUtil.get(redisKey);
        if ($.isNotBlank(codeJson)) {
            return JsonUtil.readList(codeJson, String.class);
        }
        List<String> roleCodeSet = getRoleCodeList(loginId);
        if ($.isEmpty(roleCodeSet)) {
            return Collections.emptyList();
        }
        List<String> permissionIds = rolePermissionService.queryChain().select(ROLE_PERMISSION.PERMISSION_ID).where(ROLE_PERMISSION.ROLE_ID.in(roleCodeSet)).listAs(String.class);
        List<String> permissionCodes = permissionService.queryChain().select(PERMISSION.CODE).where(PERMISSION.ID.in(permissionIds)).listAs(String.class);
        List<String> codeSet =  Lists.newArrayList(permissionCodes);
        ThreadPools.INSTANCE.execute(() -> RedisUtil.setEx(redisKey, JsonUtil.toJson(codeSet), TIME_OUT));
        return codeSet;
    }

    /**
     * 角色码集合
     *
     * @param loginId 登录 Id
     * @return List<String>
     */
    public List<String> getRoleCodeList(String loginId) {
        String redisKey = $.format("security:role:{}", loginId);
        String codeJson = RedisUtil.get(redisKey);
        if ($.isNotBlank(codeJson)) {
            return JsonUtil.readList(codeJson, String.class);
        }
        List<String> roleIds = userRoleService.queryChain().select(USER_ROLE.ROLE_ID).where(USER_ROLE.USER_ID.eq(loginId)).listAs(String.class);
        List<String> roleCodes = roleService.queryChain().select(ROLE.CODE).where(ROLE.ID.in(roleIds)).listAs(String.class);
        List<String> codeSet = Lists.newArrayList(roleCodes);
        ThreadPools.INSTANCE.execute(() -> RedisUtil.setEx(redisKey, JsonUtil.toJson(codeSet), TIME_OUT));
        return codeSet;
    }
}

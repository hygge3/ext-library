package ext.library.eatpick.config;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ext.library.eatpick.constant.Permission;
import ext.library.json.util.JsonUtil;
import ext.library.redis.util.RedisUtil;
import ext.library.security.authority.SecurityAuthority;
import ext.library.security.domain.SecuritySession;
import ext.library.security.util.SecurityUtil;
import ext.library.tool.$;
import ext.library.tool.core.ThreadPools;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityAuthorityImpl implements SecurityAuthority {

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
        List<String> codeSet = Permission.getCodeSet(roleCodeSet.getFirst());
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
        SecuritySession securitySession = SecurityUtil.getCurrentSecuritySession();
        String role = securitySession.getAttribute("role", String.class);
        List<String> roles = List.of(role);
        ThreadPools.INSTANCE.execute(() -> RedisUtil.setEx(redisKey, JsonUtil.toJson(roles), TIME_OUT));
        return roles;
    }
}

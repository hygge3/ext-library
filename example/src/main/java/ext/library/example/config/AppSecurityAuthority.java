package ext.library.example.config;

import ext.library.example.domain.AppUser;
import ext.library.example.mapper.AppUserMapper;
import ext.library.security.authority.SecurityAuthority;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 角色来源：角色存在用户表里（{@code ADMIN} / {@code USER}），按登录 Id（邮箱）查出来。
 * <p>
 * 不实现这个接口时 ext-security 用默认实现，任何人都是「无角色」，{@code @RequiresRoles("ADMIN")} 一律拒绝。
 */
@Component
@RequiredArgsConstructor
public class AppSecurityAuthority implements SecurityAuthority {

    private final AppUserMapper userMapper;

    @Override
    public List<String> getRoleCodeList(String loginId) {
        AppUser user = userMapper.get(where -> where.eq(AppUser::getEmail, loginId));
        return user == null ? List.of() : List.of(user.getRole());
    }
}

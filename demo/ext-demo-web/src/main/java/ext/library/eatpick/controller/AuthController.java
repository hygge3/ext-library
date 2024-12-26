package ext.library.eatpick.controller;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.google.common.base.Preconditions;
import ext.library.captcha.service.ICaptchaService;
import ext.library.core.util.BeanUtil;
import ext.library.eatpick.entity.Permission;
import ext.library.eatpick.entity.Role;
import ext.library.eatpick.entity.User;
import ext.library.eatpick.enums.UserStatus;
import ext.library.eatpick.param.RolePermissionParam;
import ext.library.eatpick.param.UserRoleParam;
import ext.library.eatpick.pojo.AuthUser;
import ext.library.eatpick.service.UserService;
import ext.library.security.annotion.RequiresRoles;
import ext.library.security.annotion.SecurityIgnore;
import ext.library.security.crypto.Encipher;
import ext.library.security.domain.SecurityToken;
import ext.library.security.exception.UnauthorizedException;
import ext.library.security.util.SecurityUtil;
import ext.library.tool.$;
import ext.library.tool.core.Exceptions;
import ext.library.web.annotation.RestWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestWrapper
@RestController
@RequiredArgsConstructor
@RequestMapping("auth")
public class AuthController {

    private final ICaptchaService captchaService;
    private final UserService userService;

    /**
     * 验证码
     *
     * @param session  会话
     * @param response 响应
     */
    @GetMapping("captcha")
    @SecurityIgnore
    public void captcha(@NotNull HttpSession session, @NotNull HttpServletResponse response) {
        try (ServletOutputStream sos = response.getOutputStream()) {
            captchaService.generate(session.getId(), sos);
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    @GetMapping("login")
    @SecurityIgnore
    public SecurityToken login(@RequestParam @NotNull Long id, @RequestParam @NotBlank String password, @RequestParam @NotBlank String captcha, HttpSession session) {
        Preconditions.checkArgument(captchaService.validate(session.getId(), captcha), "验证码错误");
        User user = userService.getById(id);
        // 验证密码
        if ($.isNull(user) || !Encipher.checkByBCrypt(password, user.getPassword())) {
            throw new UnauthorizedException("账号或密码错误");
        }
        // 验证状态
        if (UserStatus.BAN.code == user.getEnabled()) {
            throw new UnauthorizedException("账号已封禁");
        }
        // 登录成功
        SecurityUtil.doLogin(String.valueOf(user.getId()));
        return SecurityUtil.getCurrentToken();
    }

    @GetMapping("logout")
    public void logout() {
        SecurityUtil.loginOut();
    }

    /**
     *
     *
     * @return {@code AuthUser }
     */
    @GetMapping("info")
    public AuthUser info() {
        String id = SecurityUtil.getCurrentLoginId();
        User user = userService.getById($.toLong(id));
        return BeanUtil.convert(user, AuthUser.class);
    }


    /**
     * 分配角色
     *
     * @param map 地图
     */
    @PostMapping("role")
    @RequiresRoles("admin")
    public void assignRole(@RequestBody UserRoleParam param) {
        userService.assignRole(param.getUserIds(), param.getRoleIds());
    }

    /**
     * 查询用户的角色
     *
     * @param userId 用户主键
     * @return {@code List<RoleVO> }
     */
    @GetMapping("roles/{userId}")
    @RequiresRoles("admin")
    public List<Role> rolesByUserId(@PathVariable Long userId) {
        return userService.rolesByUserId(userId);
    }

    /**
     * 分配权限
     *
     * @param map 地图
     */
    @PostMapping("permission")
    @RequiresRoles("admin")
    public void assignPermission(@RequestBody RolePermissionParam param) {
        userService.assignPermission(param.getRoleIds(), param.getPermissionIds());
    }

    /**
     * 查询用户的角色
     *
     * @param roleId 角色主键
     * @return {@code List<RoleVO> }
     */
    @GetMapping("permissions/{roleId}")
    @RequiresRoles("admin")
    public List<Permission> permissionsByRoleId(@PathVariable Long roleId) {
        return userService.permissionsByRoleId(roleId);
    }

}

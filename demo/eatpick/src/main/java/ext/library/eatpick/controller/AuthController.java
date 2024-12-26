package ext.library.eatpick.controller;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;

import com.google.common.base.Preconditions;
import ext.library.captcha.service.ICaptchaService;
import ext.library.core.util.BeanUtil;
import ext.library.eatpick.constant.Role;
import ext.library.eatpick.entity.User;
import ext.library.eatpick.param.RegisterParam;
import ext.library.eatpick.pojo.AuthUser;
import ext.library.eatpick.service.UserService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static ext.library.eatpick.entity.table.UserTableDef.USER;

/**
 * 身份验证控制器
 */
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
    public void captcha(HttpSession session, HttpServletResponse response) {
        try (ServletOutputStream sos = response.getOutputStream()) {
            captchaService.generate(session.getId(), sos);
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    @PostMapping("register")
    @SecurityIgnore
    public SecurityToken login(@RequestBody @Validated RegisterParam param, HttpSession session) {
        Preconditions.checkArgument(captchaService.validate(session.getId(), param.getCaptcha()), "验证码错误");
        User user = BeanUtil.convert(param, User.class);
        user.setRole(Role.CUSTOMER);
        user.setAvatar($.defaultIfEmpty(param.getAvatar(), $.format("https://api.multiavatar.com/{}.png", param.getUsername())));
        String encryptPassword = Encipher.encryptByBCrypt(param.getPassword());
        user.setPassword(encryptPassword);
        userService.save(user);
        // 登录成功
        SecurityUtil.doLogin(String.valueOf(user.getId()), Map.of("role", user.getRole()));
        return SecurityUtil.getCurrentToken();
    }

    @GetMapping("login")
    @SecurityIgnore
    public SecurityToken login(@RequestParam @NotBlank(message = "请输入用户名") String username,
                               @RequestParam @NotBlank(message = "请输入密码") String password,
                               @RequestParam @NotBlank(message = "请输入验证码") String captcha,
                               HttpSession session) {
        Preconditions.checkArgument(captchaService.validate(session.getId(), captcha), "验证码错误");
        User user = userService.getOne(USER.USERNAME.eq(username));
        // 验证密码
        if ($.isNull(user) || !Encipher.checkByBCrypt(password, user.getPassword())) {
            throw new UnauthorizedException("账号或密码错误");
        }
        // 登录成功
        SecurityUtil.doLogin(String.valueOf(user.getId()), Map.of("role", user.getRole()));
        return SecurityUtil.getCurrentToken();
    }

    @GetMapping("logout")
    public void logout() {
        SecurityUtil.loginOut();
    }

    /**
     * @return {@code AuthUser }
     */
    @GetMapping("info")
    public AuthUser info() {
        String id = SecurityUtil.getCurrentLoginId();
        User user = userService.getById($.toLong(id));
        return BeanUtil.convert(user, AuthUser.class);
    }

}

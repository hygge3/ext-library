package ext.library.example.web;

import ext.library.captcha.model.CaptchaResponse;
import ext.library.captcha.service.CaptchaService;
import ext.library.example.domain.AppUser;
import ext.library.example.service.AuthService;
import ext.library.security.annotation.SecurityIgnore;
import ext.library.servlet.util.ServletUtil;
import ext.library.tool.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注册 / 发码 / 登录 / 登出。
 * <p>
 * 图形验证码走 ext-captcha（答案存 ext-cache），注册时必须验；验证码邮件走 ext-mail。
 */
@SecurityIgnore
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    private final CaptchaService captchaService;

    @GetMapping("/captcha")
    public CaptchaResponse captcha() {
        return captchaService.generateCaptchaResponse();
    }

    /** 发验证码前必须过图形验证码：否则任何人都能拿任意邮箱刷邮件 */
    @PostMapping("/code")
    public Map<String, Object> code(@RequestParam String email, @RequestParam String captchaUuid,
                                   @RequestParam String captchaCode, HttpServletRequest request, Locale locale) {
        if (!captchaService.validate(captchaUuid, captchaCode)) {
            throw new BizException(400, "图形验证码错误");
        }
        authService.sendVerifyCode(email, ServletUtil.getIpAddr(request), locale);
        return Map.of("sent", true, "email", email);
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestParam String email, @RequestParam String nickname,
                                       @RequestParam(required = false, defaultValue = "") String password,
                                       @RequestParam String captchaUuid, @RequestParam String captchaCode,
                                       HttpServletRequest request) {
        if (!captchaService.validate(captchaUuid, captchaCode)) {
            throw new BizException(400, "图形验证码错误");
        }
        AppUser user = authService.register(email, nickname, password,
                ServletUtil.getIpAddr(request), request.getHeader("User-Agent"));
        return Map.of("email", user.getEmail(), "nickname", user.getNickname(), "role", user.getRole());
    }

    /** 两种登录方式二选一：邮箱验证码登录（需先过图形验证码才发码）或邮箱 + 密码登录 */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String email,
                                     @RequestParam(required = false, defaultValue = "") String code,
                                     @RequestParam(required = false, defaultValue = "") String password,
                                     HttpServletRequest request) {
        if (code.isBlank() && password.isBlank()) {
            throw new BizException(400, "请填写邮箱验证码或密码");
        }
        AppUser user = authService.login(email, code, password,
                ServletUtil.getIpAddr(request), request.getHeader("User-Agent"));
        return Map.of("email", user.getEmail(), "nickname", user.getNickname(), "role", user.getRole());
    }

    @PostMapping("/logout")
    public Map<String, Object> logout() {
        authService.logout();
        return Map.of("logout", true);
    }
}

package ext.library.example.service;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.crypto.PasswordEncoder;
import ext.library.example.domain.AppUser;
import ext.library.example.domain.LoginLog;
import ext.library.example.mapper.AppUserMapper;
import ext.library.example.mapper.LoginLogMapper;
import ext.library.resilience.ratelimit.RateLimit;
import ext.library.security.domain.SecurityLoginParams;
import ext.library.security.domain.SecuritySession;
import ext.library.security.service.SecurityService;
import ext.library.tool.exception.BizException;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.IdUtil;
import ext.library.tool.util.StringUtil;
import ext.library.useragent.Platform;
import ext.library.useragent.UserAgent;
import ext.library.useragent.UserAgentParser;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 注册、邮箱验证码、登录。
 * <p>
 * 验证码与失败计数落在 ext-cache；登录态完全交给 ext-security 的 {@link SecurityService}。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String MODULE = "AUTH";

    private static final String CODE_CACHE = "verify-code";

    private static final String FAIL_CACHE = "login-fail";

    private static final int MAX_FAIL = 5;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AppUserMapper userMapper;

    private final LoginLogMapper loginLogMapper;

    private final CacheStrategy cache;

    private final SecurityService securityService;

    private final MailService mailService;

    @Value("${app.admin-email:admin@example.com}")
    private String adminEmail;

    public Optional<AppUser> findByEmail(String email) {
        return Optional.ofNullable(userMapper.get(where -> where.eq(AppUser::getEmail, email)));
    }

    /** 发送验证码：同一邮箱 60 秒内最多 3 次，超出由 ext-resilience 直接拒绝。 */
    @RateLimit(key = "'code:' + #email", limit = 3, window = 60)
    public void sendVerifyCode(String email, String ip, Locale locale) {
        if (findByEmail(email).isEmpty()) {
            throw new BizException(400, "邮箱未注册，请先注册");
        }
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        cache.put(CODE_CACHE, email, code, Duration.ofMinutes(5));
        mailService.sendVerifyCode(email, code, ip, locale);
        Logs.info(MODULE, "验证码已下发：email={} ip={}", email, ip);
    }

    public AppUser register(String email, String nickname, String password, String ip, String userAgent) {
        if (findByEmail(email).isPresent()) {
            throw new BizException(400, "邮箱已注册");
        }
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setNickname(nickname);
        user.setPasswordHash(PasswordEncoder.encode(password));
        user.setRole(email.equalsIgnoreCase(adminEmail) ? "ADMIN" : "USER");
        user.setCreatedAt(LocalDateTime.now());
        userMapper.save(user);
        Logs.info(MODULE, "注册成功：email={} role={}", email, user.getRole());
        return user;
    }

    /** 验证码登录 / 密码登录二选一，凭据校验通过后写会话并记录设备。 */
    public AppUser login(String email, String code, String password, String ip, String userAgent) {
        AppUser user = findByEmail(email).orElseThrow(() -> new BizException(400, "邮箱或密码错误"));
        if (StringUtil.isNotEmpty(password)) {
            if (!PasswordEncoder.matches(password, user.getPasswordHash())) {
                recordLogin(user.getEmail(), ip, userAgent, false);
                throw new BizException(400, "邮箱或密码错误");
            }
        } else {
            consumeCode(email, code);
        }
        doLogin(user, userAgent);
        recordLogin(user.getEmail(), ip, userAgent, true);
        return user;
    }

    public void logout() {
        String loginId = currentLoginId();
        if (loginId != null) {
            securityService.logout();
            Logs.info(MODULE, "登出：loginId={}", loginId);
        }
    }

    public String currentLoginId() {
        Boolean login = securityService.isLogin();
        return Boolean.TRUE.equals(login) ? securityService.getCurrentSecuritySession().getLoginId() : null;
    }

    /** 当前登录用户，未登录或用户已删除时抛 401。 */
    public AppUser currentUser() {
        String loginId = currentLoginId();
        if (loginId == null) {
            throw new BizException(401, "未登录");
        }
        return findByEmail(loginId).orElseThrow(() -> new BizException(401, "账号不存在"));
    }

    public AppUser currentUserOrNull() {
        String loginId = currentLoginId();
        return loginId == null ? null : findByEmail(loginId).orElse(null);
    }

    public SecuritySession currentSession() {
        return securityService.getCurrentSecuritySession();
    }

    public void recordLogin(String email, String ip, String userAgent, boolean success) {
        UserAgent parsed = UserAgentParser.parse(userAgent);
        LoginLog log = new LoginLog();
        log.setEmail(email);
        log.setIp(ip);
        log.setUserAgent(cut(userAgent, 240));
        log.setBrowser(cut(String.valueOf(parsed.getBrowser()), 30));
        log.setOs(cut(String.valueOf(parsed.getOs()), 30));
        log.setDevice(parsed.getPlatform() == null ? "unknown" : parsed.getPlatform().getName());
        log.setSuccess(success);
        log.setCreatedAt(LocalDateTime.now());
        loginLogMapper.save(log);
    }

    private static String cut(String text, int max) {
        return text == null || text.length() <= max ? text : text.substring(0, max);
    }

    private void consumeCode(String email, String code) {
        String cached = cache.get(CODE_CACHE, email, String.class);
        if (cached == null || !cached.equals(code)) {
            recordFail(email);
            throw new BizException(400, "验证码错误或已过期");
        }
        cache.evict(CODE_CACHE, email);
        cache.evict(FAIL_CACHE, email);
    }

    private void recordFail(String email) {
        long fails = cache.increment(FAIL_CACHE, email, 1, Duration.ofMinutes(10));
        if (fails > MAX_FAIL) {
            throw new BizException(429, "尝试次数过多，请稍后再试");
        }
    }

    private void doLogin(AppUser user, String userAgent) {
        SecurityLoginParams params = new SecurityLoginParams();
        UserAgent ua = UserAgentParser.parse(userAgent);
        params.setDeviceType(ua.getPlatform() == null ? Platform.UNKNOWN.getName() : ua.getPlatform().getName());
        params.setAttributes(Map.of(
                "email", user.getEmail(),
                "nickname", user.getNickname(),
                "role", user.getRole(),
                "loginAt", LocalDateTime.now().toString(),
                "loginTraceId", IdUtil.getUUIDv7()));
        securityService.doLogin(user.getEmail(), params);
    }
}

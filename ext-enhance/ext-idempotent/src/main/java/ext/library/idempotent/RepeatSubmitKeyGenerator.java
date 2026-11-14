package ext.library.idempotent;

import ext.library.core.util.spel.SpelUtil;
import ext.library.json.util.JsonUtil;
import ext.library.servlet.util.ServletUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/**
 * 防重复提交的键生成器
 * <p>
 * 键 = {@code prefix:hash(用户):方法:URI:hash(参数指纹)}。
 * <p>
 * 用户与参数都只留哈希摘要：用户标识可能是 session id，明文写进缓存键等于把凭证摆到 Redis / 数据库里给人看；
 * 参数里常带手机号、身份证，同样不留明文。
 */
public class RepeatSubmitKeyGenerator {

    private static final String ANONYMOUS = "anon";

    /**
     * 摘要截断长度，16 位十六进制（64 bit）足够区分，又不会把键撑长
     */
    private static final int HASH_LENGTH = 16;

    /**
     * 生成防重提交键
     *
     * @param joinPoint    连接点
     * @param repeatSubmit 防重提交注解
     *
     * @return 键
     */
    public String generate(JoinPoint joinPoint, RepeatSubmit repeatSubmit) {
        HttpServletRequest request = ServletUtil.getRequestOrNull();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        return repeatSubmit.prefix() + ':' + hash(identity(request))
                + ':' + methodSignature(request, signature)
                + ':' + hash(fingerprint(joinPoint, signature, repeatSubmit));
    }

    /**
     * 请求身份：优先容器给出的 {@link Principal}（Spring Security 与自建认证都会写入），
     * 没有则退到 session id，都没有说明是匿名调用（此时同一接口同参数的请求会互相顶掉，属预期）
     */
    private String identity(HttpServletRequest request) {
        if (request == null) {
            return ANONYMOUS;
        }
        Principal principal = request.getUserPrincipal();
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            return principal.getName();
        }
        HttpSession session = request.getSession(false);
        return session == null ? ANONYMOUS : "session:" + session.getId();
    }

    /**
     * 接口标识：优先用请求实际命中的方法 + URI（覆盖 RESTful 路径参数），
     * 无 Servlet 上下文时退回「类名#方法名」
     */
    private String methodSignature(HttpServletRequest request, MethodSignature signature) {
        if (request == null) {
            return signature.getMethod().getDeclaringClass().getSimpleName() + '#' + signature.getMethod().getName();
        }
        return request.getMethod() + ':' + request.getRequestURI();
    }

    /**
     * 参数指纹：参与序列化的只有业务入参，servlet 对象与上传文件本身不可序列化也无需区分
     */
    private String fingerprint(JoinPoint joinPoint, MethodSignature signature, RepeatSubmit repeatSubmit) {
        Object[] args = joinPoint.getArgs();
        List<Object> visible = new ArrayList<>(args == null ? 0 : args.length);
        if (args != null) {
            for (Object arg : args) {
                if (arg != null && isInfrastructureArg(arg)) {
                    continue;
                }
                visible.add(arg);
            }
        }

        String expression = repeatSubmit.uniqueExpression();
        String extra = expression.isBlank()
                ? ""
                : SpelUtil.parseValueToString(
                        SpelUtil.getSpelContext(joinPoint.getTarget(), signature.getMethod(), args), expression);

        // JSON 保留了字段名与嵌套结构，"a"+"b" 与 "ab"+"" 之类不会撞到一起
        return JsonUtil.toJson(visible) + '|' + extra;
    }

    private boolean isInfrastructureArg(Object arg) {
        return arg instanceof ServletRequest
                || arg instanceof ServletResponse
                || arg instanceof HttpSession
                || arg instanceof Principal
                || arg instanceof MultipartFile;
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, HASH_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 是 JVM 必须实现的算法，走到这里说明运行环境被改坏了
            throw new IllegalStateException("JVM 缺少 SHA-256", e);
        }
    }

}

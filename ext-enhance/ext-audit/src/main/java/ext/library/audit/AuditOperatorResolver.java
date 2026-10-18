package ext.library.audit;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 操作人解析器
 * <p>
 * 默认实现在 Servlet 环境下优先读取 ext-security 的当前登录标识，
 * 不可用时回退到请求的客户端 IP，再拿不到就返回 {@code anonymous}。
 * 业务可实现本接口覆盖默认行为，比如从自定义的 Token 头里解析。
 */
@FunctionalInterface
public interface AuditOperatorResolver {

    /** 未识别到操作人时的占位值 */
    String ANONYMOUS = "anonymous";

    /**
     * 解析当前操作人标识
     *
     * @return 操作人标识
     */
    String resolve();

    /**
     * 默认实现
     */
    class Default implements AuditOperatorResolver {

        /** ext-security 的 {@code SecurityUtil} 类名，用于反射探测，避免硬依赖 */
        private static final String SECURITY_UTIL = "ext.library.security.util.SecurityUtil";

        @Override
        public String resolve() {
            if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
                return ANONYMOUS;
            }
            // 优先取当前登录标识
            String loginId = resolveLoginId();
            if (loginId != null && !loginId.isBlank()) {
                return loginId;
            }
            // 退化到客户端 IP
            String remoteIp = attributes.getRequest().getRemoteAddr();
            return remoteIp == null || remoteIp.isBlank() ? ANONYMOUS : remoteIp;
        }

        /**
         * 通过反射调用 ext-security 的 {@code SecurityUtil.getCurrentLoginId()}
         *
         * @return 登录标识，未登录或依赖不存在时返回 {@code null}
         */
        private static String resolveLoginId() {
            try {
                Class<?> util = Class.forName(SECURITY_UTIL);
                Object session = util.getMethod("getCurrentSecuritySession").invoke(null);
                if (session == null) {
                    return null;
                }
                return (String) session.getClass().getMethod("getLoginId").invoke(session);
            } catch (ReflectiveOperationException | RuntimeException e) {
                // 未引入 ext-security 或未登录，静默降级
                return null;
            }
        }

    }

}

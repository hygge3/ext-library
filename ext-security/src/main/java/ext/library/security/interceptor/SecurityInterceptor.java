package ext.library.security.interceptor;

import ext.library.security.router.SecurityRouter;
import org.jspecify.annotations.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * <p>
 * 拦截器
 * </p>
 */
public class SecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            Method method = ((HandlerMethod) handler).getMethod();
            if ("error".equals(method.getName())) {
                return false;
            }
            // 执行自定义路由
            return SecurityRouter.build().run(method);
        }
        return true;
    }

}

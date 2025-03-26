package ext.library.security.interceptor;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import ext.library.security.router.SecurityRouter;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * <p>
 * 拦截器
 * </p>
 */
@Slf4j
public class SecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
                             @Nonnull Object handler) throws Exception {
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

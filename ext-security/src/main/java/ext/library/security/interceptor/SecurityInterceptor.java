package ext.library.security.interceptor;

import ext.library.core.datascope.DataScopeContext;
import ext.library.core.datascope.DataScopeMeta;
import ext.library.security.annotation.DataScope;
import ext.library.security.annotation.RequiresPermissions;
import ext.library.security.annotation.RequiresRoles;
import ext.library.security.annotation.SecurityIgnore;
import ext.library.security.authority.DataScopeProvider;
import ext.library.security.enums.DataScopeType;
import ext.library.security.exception.ForbiddenException;
import ext.library.security.util.PermissionUtil;
import ext.library.security.util.SecurityUtil;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.util.ClassUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 拦截器
 * </p>
 */
public class SecurityInterceptor implements HandlerInterceptor {

    private final @Nullable DataScopeProvider dataScopeProvider;

    public SecurityInterceptor() {
        this(null);
    }

    public SecurityInterceptor(@Nullable DataScopeProvider dataScopeProvider) {
        this.dataScopeProvider = dataScopeProvider;
    }

    /**
     * 检查权限
     */
    private static void checkMethodPermission(Method method) {

        // 检查登录权限
        SecurityUtil.checkToken();

        // 判断是否检查角色
        RequiresRoles requiresRoles = ClassUtil.getAnnotation(method, RequiresRoles.class);
        if (Objects.nonNull(requiresRoles)) {
            if (!PermissionUtil.hasMultiPermValid(List.of(requiresRoles.value()), requiresRoles.logical(), PermissionUtil.getRoles())) {
                throw new ForbiddenException("无角色权限");
            }
        }

        // 判断是否检查权限
        RequiresPermissions requiresPermissions = ClassUtil.getAnnotation(method, RequiresPermissions.class);
        if (Objects.nonNull(requiresPermissions)) {
            if (!PermissionUtil.hasMultiPermValid(List.of(requiresPermissions.value()), requiresPermissions.logical(), PermissionUtil.getPermissions())) {
                throw new ForbiddenException("无访问权限");
            }
        }

    }

    /**
     * 解析并写入数据权限范围
     *
     * @param method 当前处理方法
     */
    void applyDataScope(Method method) {
        DataScope dataScope = ClassUtil.getAnnotation(method, DataScope.class);
        if (dataScope == null || dataScope.type() == DataScopeType.ALL) {
            return;
        }
        if (dataScopeProvider == null) {
            throw new ExtException(EmojiSymbol.SECURITY, "接口标注了 @DataScope 但未实现 DataScopeProvider");
        }
        DataScopeContext.set(new DataScopeMeta(dataScope.field(), dataScopeProvider.resolve(dataScope.type())));
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            Method method = ((HandlerMethod) handler).getMethod();
            // 注解忽略
            SecurityIgnore securityIgnore = ClassUtil.getAnnotation(method, SecurityIgnore.class);
            if (Objects.isNull(securityIgnore)) {
                checkMethodPermission(method);
                applyDataScope(method);
            }
            return true;
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                @NonNull Object handler, @Nullable Exception ex) {
        DataScopeContext.clear();
    }
}

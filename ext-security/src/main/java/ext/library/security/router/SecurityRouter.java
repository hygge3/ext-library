package ext.library.security.router;

import ext.library.core.util.ServletUtil;
import ext.library.security.annotion.RequiresPermissions;
import ext.library.security.annotion.RequiresRoles;
import ext.library.security.annotion.SecurityIgnore;
import ext.library.security.exception.ForbiddenException;
import ext.library.security.function.RouterFunction;
import ext.library.security.util.PermissionUtil;
import ext.library.security.util.SecurityUtil;
import ext.library.tool.$;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * <p>
 * 路由方法
 * </p>
 */
@Slf4j
public class SecurityRouter implements RouterFunction<Method> {

    /**
     * 忽略路由匹配列表
     */
     final List<String> excludePathList = new ArrayList<>();

    private SecurityRouter() {
        // 全局忽略
        excludePathList.add("/error");
    }

    public static SecurityRouter build() {
        return new SecurityRouter();
    }

    /**
     * 路由匹配校验
     *
     * @param pattern  匹配路径
     * @param supplier 校验函数
     * @return SecurityRouter
     */
    public SecurityRouter match(String pattern, BooleanSupplier supplier) {
        if (checkExcludeMatch()) {
            return this;
        }
        String path = ServletUtil.getRequest().getRequestURI();
        // 判断路由是否匹配
        boolean pathMatchResult = routePathMatch(pattern, path);
        log.debug("[🛡️] 路由匹配 pattern:{},path:{},result：{}", pattern, path, pathMatchResult);
        if (pathMatchResult && !supplier.getAsBoolean()) {
            throw new ForbiddenException("路由方法权限验证不通过");
        }
        return this;
    }

    /**
     * 路由匹配
     *
     * @param pattern 匹配值
     * @param path    路径
     * @return 是否匹配
     */
    private boolean routePathMatch(String pattern, String path) {
        PathMatcher pathMatcher = new AntPathMatcher();
        return pathMatcher.match(pattern, path);
    }

    /**
     * 忽略路由匹配
     *
     * @param pathPattern 路由匹配路径
     * @return SecurityRouter
     */
    public SecurityRouter excludeMatch(String... pathPattern) {
        if (null != pathPattern && pathPattern.length > 0) {
            excludePathList.addAll(Arrays.asList(pathPattern));
        }
        return this;
    }

    /**
     * 忽略校验
     *
     * @return true 忽略 false 不忽略
     */
    private boolean checkExcludeMatch() {
        String path = ServletUtil.getRequest().getRequestURI();
        if (excludePathList.isEmpty()) {
            return false;
        }
        for (String pattern : excludePathList) {
            if (routePathMatch(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 执行路由方法
     *
     * @param method Method
     * @return boolean
     */
    @Override
    public boolean run(Method method) {
        // 判断忽略鉴权
        SecurityIgnore securityIgnore = $.getAnnotation(method, SecurityIgnore.class);
        if (!checkExcludeMatch() && Objects.isNull(securityIgnore)) {
            // 验证方法权限
            checkMethodPermission(method);
        }
        return true;
    }

    /**
     * 检查权限
     */
    private static void checkMethodPermission(Method method) {

        // 检查登录权限
        SecurityUtil.checkToken();

        // 判断是否检查角色
        RequiresRoles requiresRoles = $.getAnnotation(method, RequiresRoles.class);
        if (Objects.nonNull(requiresRoles)) {
            if (!PermissionUtil.hasMultiPermValid(List.of(requiresRoles.value()), requiresRoles.logical(),
                    PermissionUtil.getRoles())) {
                throw new ForbiddenException("无角色权限");
            }
        }

        // 判断是否检查权限
        RequiresPermissions requiresPermissions = $.getAnnotation(method, RequiresPermissions.class);
        if (Objects.nonNull(requiresPermissions)) {
            if (!PermissionUtil.hasMultiPermValid(List.of(requiresPermissions.value()), requiresPermissions.logical(),
                    PermissionUtil.getPermissions())) {
                throw new ForbiddenException("无访问权限");
            }
        }

    }

}

package ext.library.web.resolver.page;

import ext.library.tool.util.TypeCastUtil;
import ext.library.web.response.PageQuery;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 自定义当前用户参数解析器
 */
@Component
public class PageQueryArgumentResolver implements HandlerMethodArgumentResolver {
    /**
     * 判断参数是否需要解析：如果参数类型是 PageQuery，就用这个解析器
     */
    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        // 判断参数类型是否是 PageQuery
        return parameter.getParameterType().equals(PageQuery.class);
    }

    /**
     * 具体的参数解析逻辑：从请求参数解析出分页参数，封装成 PageQuery 对象
     */
    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        PageQuery pageQuery = new PageQuery();
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        pageQuery.setPage(TypeCastUtil.getAsLong(request.getParameter("page"), 1L));
        pageQuery.setSize(TypeCastUtil.getAsLong(request.getParameter("size"), 10L));
        pageQuery.setTotal(TypeCastUtil.getAsLong(request.getParameter("total"), 0L));
        return pageQuery;
    }
}

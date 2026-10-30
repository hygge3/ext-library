package ext.library.servlet.util;

import ext.library.json.util.JsonUtil;
import ext.library.servlet.response.R;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.util.ObjectUtil;
import ext.library.tool.util.StringUtil;
import ext.library.tool.util.TypeCastUtil;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Servlet 工具类
 * <p>
 * 提供 HttpServletRequest/Response 的便捷操作方法
 */
public final class ServletUtil {

    /**
     * Node 服务转发 IP 头
     * <p>
     * 如果在前端和服务端中间还有一层 Node 服务，
     * 在 Node 对前端数据进行处理并发起新请求时，需携带此头部信息便于获取真实 IP
     */
    private static final String nodeForwardedIp = "Node-Forwarded-IP";

    /**
     * 客户端 IP 获取优先级头列表
     */
    private static final String[] clientIpHeaders = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    private ServletUtil() {
    }

    // region Request/Response

    /**
     * 获取 request
     */
    public static HttpServletRequest getRequest() {
        return getRequestAttributes().getRequest();
    }

    /**
     * 获取 response
     */
    public static @Nullable HttpServletResponse getResponse() {
        return getRequestAttributes().getResponse();
    }

    /**
     * 获取 request，不在 Web 线程（定时任务、异步线程、单元测试）时返回 {@code null}
     * <p>
     * 需要判空的调用方用这个方法，不必再手写 {@code RequestContextHolder} 的 {@code instanceof} 样板。
     *
     * @return request，非 Web 线程返回 {@code null}
     */
    public static @Nullable HttpServletRequest getRequestOrNull() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes
                ? attributes.getRequest()
                : null;
    }

    /**
     * 获取 response，不在 Web 线程时返回 {@code null}
     *
     * @return response，非 Web 线程返回 {@code null}
     */
    public static @Nullable HttpServletResponse getResponseOrNull() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes
                ? attributes.getResponse()
                : null;
    }

    /**
     * 获取 session
     */
    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    public static ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        Assert.notNull(attributes, "Request Attributes does not exist");
        return (ServletRequestAttributes) attributes;
    }

    // endregion

    // region Parameter

    /**
     * 获取 String 参数
     */
    public static String getParameter(String name) {
        return getRequest().getParameter(name);
    }

    /**
     * 获取 String 参数
     */
    public static String getParameter(String name, String defaultValue) {
        return TypeCastUtil.getAsString(getParameter(name), defaultValue);
    }

    /**
     * 获取 Integer 参数
     */
    public static Integer getParameterToInt(String name) {
        return TypeCastUtil.getAsInteger(getParameter(name));
    }

    /**
     * 获取 Integer 参数
     */
    public static Integer getParameterToInt(String name, Integer defaultValue) {
        return TypeCastUtil.getAsInteger(getParameter(name), defaultValue);
    }

    /**
     * 获取 Boolean 参数
     */
    public static Boolean getParameterToBool(String name) {
        return TypeCastUtil.getAsBoolean(getParameter(name));
    }

    /**
     * 获取 Boolean 参数
     */
    public static Boolean getParameterToBool(String name, Boolean defaultValue) {
        return TypeCastUtil.getAsBoolean(getParameter(name), defaultValue);
    }

    /**
     * 获得所有请求参数
     *
     * @param request 请求对象{@link ServletRequest}
     *
     * @return Map
     */
    public static Map<String, String[]> getParams(ServletRequest request) {
        final Map<String, String[]> map = request.getParameterMap();
        return Collections.unmodifiableMap(map);
    }

    /**
     * 获得所有请求参数
     *
     * @param request 请求对象{@link ServletRequest}
     *
     * @return Map
     */
    public static Map<String, String> getParamMap(ServletRequest request) {
        Map<String, String[]> paramsMap = getParams(request);
        Map<String, String> params = new HashMap<>(paramsMap.size());
        for (Map.Entry<String, String[]> entry : paramsMap.entrySet()) {
            params.put(entry.getKey(), StringUtil.join(entry.getValue()));
        }
        return params;
    }

    // endregion

    // region Attribute

    public static void setRequestAttribute(String name, Object value) {
        getRequest().setAttribute(name, value);
    }

    /**
     * 获取请求属性，如果指定的属性不存在，则返回 null
     *
     * @param name 属性的名称
     *
     * @return 属性的值，如果属性不存在，则返回 null
     */
    public static Object getRequestAttribute(String name) {
        return getRequest().getAttribute(name);
    }

    /**
     * 从当前请求中移除一个属性
     *
     * @param name 要移除的属性的名称
     */
    public static void removeRequestAttribute(String name) {
        getRequest().removeAttribute(name);
    }

    // endregion

    // region Header

    public static String getHeader(HttpServletRequest request, String name) {
        return ObjectUtil.defaultIfEmpty(request.getHeader(name), "");
    }

    public static String getHeader(String name) {
        return getHeader(getRequest(), name);
    }

    /**
     * 设置响应的 Header
     *
     * @param name  名
     * @param value 值
     */
    public static void setHeader(String name, String value) {
        HttpServletResponse response = getResponse();
        if (response != null) {
            response.setHeader(name, value);
        }
    }

    public static void addHeader(String name, String value) {
        HttpServletResponse response = getResponse();
        if (response != null) {
            response.addHeader(name, value);
        }
    }

    public static Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> map = new LinkedCaseInsensitiveMap<>();
        Enumeration<String> enumeration = request.getHeaderNames();
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                String key = enumeration.nextElement();
                String value = request.getHeader(key);
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * 获取 User-Agent
     */
    public static String getUA() {
        return getHeader(HttpHeaders.USER_AGENT);
    }

    // endregion

    // region Cookie

    /**
     * 将 cookie 封装到 Map 里面
     *
     * @return Cookie map
     */
    public static Map<String, Cookie> readCookieMap() {
        final Cookie[] cookies = getRequest().getCookies();
        if (ObjectUtil.isEmpty(cookies)) {
            return new HashMap<>(0);
        }
        return Arrays.stream(cookies).collect(Collectors.toMap(Cookie::getName, Function.identity(), (key1, key2) -> key2));
    }

    public static @Nullable Cookie getCookie(String name) {
        return readCookieMap().get(name);
    }

    public static @Nullable String getCookieValue(String name) {
        Cookie cookie = getCookie(name);
        return cookie != null ? cookie.getValue() : null;
    }

    public static void addCookie(String name, @Nullable String value, Integer maxAge) {
        HttpServletResponse response = getResponse();
        if (response == null) {
            return;
        }
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    /**
     * 清除指定的 cookie
     *
     * @param key cookie key
     */
    public static void removeCookie(String key) {
        addCookie(key, null, 0);
    }

    // endregion

    // region IP

    /**
     * 获取客户端 IP
     */
    public static String getIpAddr() {
        return getIpAddr(getRequest(), nodeForwardedIp);
    }

    /**
     * 获取客户端 IP
     */
    public static String getIpAddr(HttpServletRequest request) {
        return getIpAddr(request, nodeForwardedIp);
    }

    /**
     * 获取客户端 IP
     *
     * @param request          请求对象
     * @param otherHeaderNames 额外的 IP 头名称
     *
     * @return IP 地址
     */
    public static String getIpAddr(HttpServletRequest request, String... otherHeaderNames) {
        return getClientIpByHeader(request, mergeClientIpHeaders(otherHeaderNames));
    }

    /**
     * 通过 Header 获取客户端 IP
     * <p>
     * 注意：使用此方法获取的客户端 IP 地址必须在 HTTP 服务器（如 Nginx）中正确配置头信息，
     * 否则可能导致 IP 伪造
     *
     * @param request     请求对象{@link HttpServletRequest}
     * @param headerNames 自定义头，通常在 HTTP 服务器中配置
     *
     * @return IP 地址
     */
    public static String getClientIpByHeader(HttpServletRequest request, String... headerNames) {
        String ip;
        for (String header : headerNames) {
            ip = request.getHeader(header);
            if (ip != null && checkNotUnknown(ip)) {
                return getMultistageReverseProxyIp(ip);
            }
        }
        ip = request.getRemoteAddr();
        return getMultistageReverseProxyIp(ip);
    }

    private static String[] mergeClientIpHeaders(String... otherHeaderNames) {
        if (ObjectUtils.isEmpty(otherHeaderNames)) {
            return clientIpHeaders;
        }
        return Stream.concat(Stream.of(clientIpHeaders), Stream.of(otherHeaderNames)).toArray(String[]::new);
    }

    /**
     * 多次反向代理后会有多个 IP 值，第一个 IP 才是真实 IP
     *
     * @param ip IP 字符串
     *
     * @return 真实 IP
     */
    private static String getMultistageReverseProxyIp(String ip) {
        if (ip.indexOf(",") <= 0) {
            return ip;
        }
        String[] ips = ip.trim().split(",");
        for (String subIp : ips) {
            if (checkNotUnknown(subIp.trim())) {
                return subIp.trim();
            }
        }
        return ip;
    }

    private static boolean checkNotUnknown(String checkString) {
        return !"unknown".equalsIgnoreCase(checkString);
    }

    // endregion

    // region JSON 响应

    /**
     * 按统一序列化口径写出 JSON 响应
     * <p>
     * 供 Filter、WebSocket 握手拦截器等 servlet 级代码使用，序列化规则与 MVC 的响应一致。
     *
     * @param response 响应对象
     * @param status   HTTP 状态码
     * @param body     响应体，建议为 {@link R}
     */
    public static void writeJson(HttpServletResponse response, int status, Object body) {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(JsonUtil.toJson(body));
            response.getWriter().flush();
        } catch (IOException e) {
            throw new ExtException(EmojiSymbol.WEB, e, "写出 JSON 响应失败");
        }
    }

    /**
     * 写出统一响应体
     * <p>
     * 响应的 HTTP 状态码取 {@code R.code}：落在 100-599 内视为 HTTP 状态码，
     * 否则（如业务码 6xx）视为业务码，HTTP 状态仍为 200。
     *
     * @param response 响应对象
     * @param body     统一响应体
     */
    public static void writeJson(HttpServletResponse response, R<?> body) {
        int code = body.getCode();
        boolean httpStatus = code >= 100 && code <= 599;
        writeJson(response, httpStatus ? code : HttpServletResponse.SC_OK, body);
    }

    // endregion

}

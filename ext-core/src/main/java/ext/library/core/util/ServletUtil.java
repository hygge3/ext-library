package ext.library.core.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.net.HttpHeaders;
import ext.library.tool.$;
import ext.library.tool.constant.Symbol;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.UnmodifiableView;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 客户端工具类
 */
@UtilityClass
public class ServletUtil {

    /**
     * 获取 request
     */
    public HttpServletRequest getRequest() {
        return getRequestAttributes().getRequest();
    }

    /**
     * 获取 response
     */
    public HttpServletResponse getResponse() {
        return getRequestAttributes().getResponse();
    }

    // region 请求

    /**
     * 获取 String 参数
     */
    public String getParameter(String name) {
        return getRequest().getParameter(name);
    }

    /**
     * 获取 String 参数
     */
    public String getParameter(String name, String defaultValue) {
        return $.toStr(getParameter(name), defaultValue);
    }

    /**
     * 获取 Integer 参数
     */
    public Integer getParameterToInt(String name) {
        return $.toInt(getParameter(name));
    }

    /**
     * 获取 Integer 参数
     */
    public Integer getParameterToInt(String name, Integer defaultValue) {
        return $.toInt(getParameter(name), defaultValue);
    }

    /**
     * 获取 Boolean 参数
     */
    public Boolean getParameterToBool(String name) {
        return $.toBoolean(getParameter(name));
    }

    /**
     * 获取 Boolean 参数
     */
    public Boolean getParameterToBool(String name, Boolean defaultValue) {
        return $.toBoolean(getParameter(name), defaultValue);
    }

    /**
     * 获得所有请求参数
     *
     * @param request 请求对象{@link ServletRequest}
     * @return Map
     */
    @UnmodifiableView
    public Map<String, String[]> getParams(ServletRequest request) {
        final Map<String, String[]> map = request.getParameterMap();
        return Collections.unmodifiableMap(map);
    }

    /**
     * 获得所有请求参数
     *
     * @param request 请求对象{@link ServletRequest}
     * @return Map
     */
    public Map<String, String> getParamMap(ServletRequest request) {
        Map<String, String[]> paramsMap = getParams(request);
        Map<String, String> params = Maps.newHashMapWithExpectedSize(paramsMap.size());
        for (Map.Entry<String, String[]> entry : paramsMap.entrySet()) {
            params.put(entry.getKey(), Joiner.on(Symbol.C_COMMA).skipNulls().join(entry.getValue()));
        }
        return params;
    }

    /**
     * 获取 session
     */
    public HttpSession getSession() {
        return getRequest().getSession();
    }

    public ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return (ServletRequestAttributes) attributes;
    }

    public void setRequestAttribute(String name, Object value) {
        getRequest().setAttribute(name, value);
    }

    /**
     * 获取请求属性 如果指定的属性不存在，则返回 null。
     *
     * @param name 属性的名称
     * @return 属性的值，如果属性不存在，则返回 null
     */
    public Object getRequestAttribute(String name) {
        return getRequest().getAttribute(name);
    }

    /**
     * 从当前请求中移除一个属性。
     *
     * @param name 要移除的属性的名称。这是一个字符串值，用于唯一标识要移除的属性。
     */
    public void removeRequestAttribute(String name) {
        getRequest().removeAttribute(name);
    }

    public String getHeader(HttpServletRequest request, String name) {
        return $.defaultIfEmpty(request.getHeader(name), Symbol.EMPTY);
    }

    public String getHeader(String name) {
        return getHeader(getRequest(), name);
    }

    /**
     * 设置响应的 Header
     *
     * @param name  名
     * @param value 值，可以是 String，Date，int
     */
    public void setHeader(String name, String value) {
        getResponse().setHeader(name, value);
    }

    public void addHeader(String name, String value) {
        getResponse().addHeader(name, value);
    }

    public Map<String, String> getHeaders(HttpServletRequest request) {
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
     * 将 cookie 封装到 Map 里面
     *
     * @return Cookie map
     */
    public Map<String, Cookie> readCookieMap() {
        final Cookie[] cookies = getRequest().getCookies();
        if ($.isEmpty(cookies)) {
            return new HashMap<>(0);
        }
        return Arrays.stream(cookies)
                .collect(Collectors.toMap(Cookie::getName, Function.identity(), (key1, key2) -> key2));
    }

    public Cookie getCookie(String name) {
        return readCookieMap().get(name);
    }

    public String getCookieValue(String name) {
        Cookie cookie = getCookie(name);
        return cookie != null ? cookie.getValue() : null;
    }

    /**
     * 清除 某个指定的 cookie
     *
     * @param key cookie key
     */
    public void removeCookie(String key) {
        addCookie(key, null, 0);
    }

    public void addCookie(String name, String value, Integer maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(Symbol.SLASH);
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        getResponse().addCookie(cookie);
    }

    /**
     * 获取 UA
     *
     * @return {@code String }
     */
    public String getUA() {
        return getHeader(HttpHeaders.USER_AGENT);
    }

    // region ip 获取

    private final String[] CLIENT_IP_HEADERS = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP",
            "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};

    /**
     * 如果在前端和服务端中间还有一层 Node 服务 在 Node 对前端数据进行处理并发起新请求时，需携带此头部信息 便于获取真实 IP
     */
    public final String NODE_FORWARDED_IP = "Node-Forwarded-IP";

    /**
     * 获取客户端 IP
     */
    public String getIpAddr(HttpServletRequest request) {
        return getIpAddr(request, NODE_FORWARDED_IP);
    }

    /**
     * 获取客户端 IP
     * <p>
     * 参考 huTool 稍微调整了下 headers 顺序
     */
    public String getIpAddr(HttpServletRequest request, String... otherHeaderNames) {
        return getClientIpByHeader(request, mergeClientIpHeaders(otherHeaderNames));
    }

    /**
     * 获取客户端 IP
     *
     * <p>
     * headerNames 参数用于自定义检测的 Header<br>
     * 需要注意的是，使用此方法获取的客户 IP 地址必须在 Http 服务器（例如 Nginx）中配置头信息，否则容易造成 IP 伪造。
     * </p>
     *
     * @param request     请求对象{@link HttpServletRequest}
     * @param headerNames 自定义头，通常在 Http 服务器（例如 Nginx）中配置
     * @return IP 地址
     * @since 4.4.1
     */
    public String getClientIpByHeader(HttpServletRequest request, String... headerNames) {
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

    private String[] mergeClientIpHeaders(String... otherHeaderNames) {
        if (ObjectUtils.isEmpty(otherHeaderNames)) {
            return CLIENT_IP_HEADERS;
        }
        return Stream.concat(Stream.of(CLIENT_IP_HEADERS), Stream.of(otherHeaderNames)).toArray(String[]::new);
    }

    /**
     * 多次反向代理后会有多个 ip 值，第一个 ip 才是真实 ip
     *
     * @param ip ip
     * @return 真实 ip
     */
    private String getMultistageReverseProxyIp(String ip) {
        if (null == ip || ip.indexOf(Symbol.COMMA) <= 0) {
            return ip;
        }
        String[] ips = ip.trim().split(Symbol.COMMA);
        for (String subIp : ips) {
            if (checkNotUnknown(subIp)) {
                return subIp;
            }
        }
        return ip;
    }

    private boolean checkNotUnknown(String checkString) {
        return !"unknown".equalsIgnoreCase(checkString);
    }

    // endregion

}

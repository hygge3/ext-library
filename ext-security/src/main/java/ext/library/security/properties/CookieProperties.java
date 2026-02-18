package ext.library.security.properties;

import jakarta.validation.constraints.Pattern;

public class CookieProperties {

    /**
     * 是否禁止 js 操作 Cookie
     */
    private Boolean httpOnly = true;
    /**
     * cookie 名称
     */
    private String cookieName = "Token";
    /**
     * 域设置
     */
    private String domain;
    /**
     * 路径设置
     */
    @Pattern(regexp = "^/(?:[a-zA-Z0-9\\-._~!$&'()*+,;=:@/%]*|\\*{1,2})*$")
    private String path;
    /**
     * 是否应该只在加密的（即 SSL）连接上发送
     */
    private Boolean secure;

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public Boolean getHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(Boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getSecure() {
        return secure;
    }

    public void setSecure(Boolean secure) {
        this.secure = secure;
    }
}

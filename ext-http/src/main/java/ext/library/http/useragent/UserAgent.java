package ext.library.http.useragent;

import java.io.Serial;
import java.io.Serializable;

/**
 * User-Agent 信息对象
 */
public class UserAgent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否为移动平台
     */
    private boolean mobile;

    /**
     * 浏览器类型
     */
    private Browser browser;

    /**
     * 浏览器版本
     */
    private String version;

    /**
     * 平台类型
     */
    private Platform platform;

    /**
     * 系统类型
     */
    private OS os;

    /**
     * 系统版本
     */
    private String osVersion;

    /**
     * 引擎类型
     */
    private Engine engine;

    /**
     * 引擎版本
     */
    private String engineVersion;

    public boolean isMobile() {
        return mobile;
    }

    public void setMobile(boolean mobile) {
        this.mobile = mobile;
    }

    public Browser getBrowser() {
        return browser;
    }

    public void setBrowser(Browser browser) {
        this.browser = browser;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public OS getOs() {
        return os;
    }

    public void setOs(OS os) {
        this.os = os;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }
}
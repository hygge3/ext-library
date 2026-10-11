package ext.library.useragent;

import java.io.Serial;
import java.io.Serializable;

/**
 * User-Agent 解析结果
 * <p>
 * 封装解析后的完整 User-Agent 信息，包含浏览器、操作系统、平台、引擎等组件。
 * </p>
 *
 * @since 1.0.0
 */
public class UserAgent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private boolean mobile;
    private Browser browser;
    private String version;
    private Platform platform;
    private OS os;
    private String osVersion;
    private Engine engine;
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

    @Override
    public String toString() {
        return String.format("UserAgent{mobile=%s, browser=%s %s, os=%s %s, platform=%s, engine=%s %s}",
                mobile, browser, version, os, osVersion, platform, engine, engineVersion);
    }
}

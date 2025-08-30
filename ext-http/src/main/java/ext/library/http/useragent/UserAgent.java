package ext.library.http.useragent;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * User-Agent 信息对象
 */
@Getter
@Setter
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

}
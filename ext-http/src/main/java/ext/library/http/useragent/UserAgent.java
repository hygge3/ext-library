package ext.library.http.useragent;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * User-Agent 信息对象
 */
@Getter
@Setter
public class UserAgent implements Serializable {

    @Serial
    static final long serialVersionUID = 1L;

    /**
     * 是否为移动平台
     */
    boolean mobile;

    /**
     * 浏览器类型
     */
    Browser browser;

    /**
     * 浏览器版本
     */
    String version;

    /**
     * 平台类型
     */
    Platform platform;

    /**
     * 系统类型
     */
    OS os;

    /**
     * 系统版本
     */
    String osVersion;

    /**
     * 引擎类型
     */
    Engine engine;

    /**
     * 引擎版本
     */
    String engineVersion;

}

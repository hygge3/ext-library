package ext.library.useragent;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * 平台信息
 * <p>
 * 识别设备平台类型（移动/桌面/iOS/Android 等）。
 * </p>
 *
 * @since 1.0.0
 */
public class Platform extends UserAgentInfo {

    /**
     * 所有移动平台列表
     */
    public static final List<Platform> MOBILE_PLATFORM_LIST;
    /**
     * 所有桌面平台列表
     */
    public static final List<Platform> DESKTOP_PLATFORM_LIST;
    /**
     * 所有平台列表
     */
    public static final List<Platform> PLATFORM_LIST;
    /**
     * 未知平台
     */
    public static final Platform UNKNOWN = new Platform(NAME_UNKNOWN, null);
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 移动平台定义
     */
    private static final String[][] mobileDefinitions = {
            {"Windows Phone", "windows (ce|phone|mobile)( os)?"},
            {"iPad", "ipad"},
            {"iPod", "ipod"},
            {"iPhone", "iphone"},
            {"Android", "XiaoMi|MI\\s+"},
            {"Android", "android"},
            {"GoogleTV", "googletv"},
            {"htcFlyer", "htc_flyer"},
            {"Symbian", "symbian(os)?"},
            {"Blackberry", "blackberry"}
    };
    /**
     * 桌面平台定义
     */
    private static final String[][] desktopDefinitions = {
            {"Windows", "windows"},
            {"Mac", "(macintosh|darwin)"},
            {"Linux", "linux"},
            {"Wii", "wii"},
            {"Playstation", "playstation"},
            {"Java", "java"}
    };

    static {
        MOBILE_PLATFORM_LIST = createPlatforms(mobileDefinitions);
        DESKTOP_PLATFORM_LIST = createPlatforms(desktopDefinitions);

        List<Platform> all = new ArrayList<>(MOBILE_PLATFORM_LIST.size() + DESKTOP_PLATFORM_LIST.size());
        all.addAll(MOBILE_PLATFORM_LIST);
        all.addAll(DESKTOP_PLATFORM_LIST);
        PLATFORM_LIST = List.copyOf(all);
    }

    /**
     * 构造平台
     *
     * @param name  平台名称
     * @param regex 匹配正则
     */
    private Platform(String name, String regex) {
        super(name, regex);
    }

    private static List<Platform> createPlatforms(String[][] definitions) {
        List<Platform> list = new ArrayList<>(definitions.length);
        for (String[] def : definitions) {
            list.add(new Platform(def[0], def[1]));
        }
        return List.copyOf(list);
    }

    /**
     * 是否为移动平台
     *
     * @return 是否为移动平台
     */
    public boolean isMobile() {
        return MOBILE_PLATFORM_LIST.stream().anyMatch(p -> p.getName().equals(this.getName()));
    }

    /**
     * 是否为 iPhone 或 iPod 设备
     *
     * @return 是否为 iPhone 或 iPod
     */
    public boolean isIPhoneOrIPod() {
        String name = getName();
        return "iPhone".equals(name) || "iPod".equals(name);
    }

    /**
     * 是否为 iPad 设备
     *
     * @return 是否为 iPad
     */
    public boolean isIPad() {
        return "iPad".equals(getName());
    }

    /**
     * 是否为 iOS 平台（iPhone、iPod、iPad）
     *
     * @return 是否为 iOS 平台
     */
    public boolean isIos() {
        return isIPhoneOrIPod() || isIPad();
    }

    /**
     * 是否为 Android 平台（Android、Google TV）
     *
     * @return 是否为 Android 平台
     */
    public boolean isAndroid() {
        String name = getName();
        return "Android".equals(name) || "GoogleTV".equals(name);
    }
}

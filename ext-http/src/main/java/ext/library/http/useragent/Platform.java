package ext.library.http.useragent;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 平台信息
 * <p>
 * 识别设备平台类型（移动/桌面/iOS/Android等）。
 * </p>
 *
 * @since 1.0.0
 */
public class Platform extends UserAgentInfo {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 移动平台定义
     */
    private static final String[][] MOBILE_PLATFORMS = {
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
    private static final String[][] DESKTOP_PLATFORMS = {
            {"Windows", "windows"},
            {"Mac", "(macintosh|darwin)"},
            {"Linux", "linux"},
            {"Wii", "wii"},
            {"Playstation", "playstation"},
            {"Java", "java"}
    };

    /**
     * 所有移动平台列表
     */
    public static final List<Platform> MOBILE_PLATFORMS_LIST;

    /**
     * 所有桌面平台列表
     */
    public static final List<Platform> DESKTOP_PLATFORMS_LIST;

    /**
     * 所有平台列表
     */
    public static final List<Platform> PLATFORMS;

    static {
        MOBILE_PLATFORMS_LIST = createPlatforms(MOBILE_PLATFORMS);
        DESKTOP_PLATFORMS_LIST = createPlatforms(DESKTOP_PLATFORMS);

        List<Platform> all = new ArrayList<>(MOBILE_PLATFORMS_LIST.size() + DESKTOP_PLATFORMS_LIST.size());
        all.addAll(MOBILE_PLATFORMS_LIST);
        all.addAll(DESKTOP_PLATFORMS_LIST);
        PLATFORMS = List.copyOf(all);
    }

    private static List<Platform> createPlatforms(String[][] definitions) {
        List<Platform> list = new ArrayList<>(definitions.length);
        for (String[] def : definitions) {
            list.add(new Platform(def[0], def[1]));
        }
        return List.copyOf(list);
    }

    /**
     * 未知平台
     */
    public static final Platform UNKNOWN = new Platform(NAME_UNKNOWN, (String) null);

    /**
     * iPhone 平台
     */
    public static final Platform IPHONE = new Platform("iPhone", "iphone");

    /**
     * iPod 平台
     */
    public static final Platform IPOD = new Platform("iPod", "ipod");

    /**
     * iPad 平台
     */
    public static final Platform IPAD = new Platform("iPad", "ipad");

    /**
     * Android 平台
     */
    public static final Platform ANDROID = new Platform("Android", "android");

    /**
     * Google TV 平台
     */
    public static final Platform GOOGLE_TV = new Platform("GoogleTV", "googletv");

    /**
     * Windows Phone 平台
     */
    public static final Platform WINDOWS_PHONE = new Platform("Windows Phone", "windows (ce|phone|mobile)( os)?");

    /**
     * 构造平台
     *
     * @param name  平台名称
     * @param regex 匹配正则
     */
    public Platform(String name, String regex) {
        super(name, regex);
    }

    /**
     * 是否为移动平台
     *
     * @return 是否为移动平台
     */
    public boolean isMobile() {
        return MOBILE_PLATFORMS_LIST.contains(this);
    }

    /**
     * 是否为 iPhone 或 iPod 设备
     *
     * @return 是否为 iPhone 或 iPod
     */
    public boolean isIPhoneOrIPod() {
        return this.equals(IPHONE) || this.equals(IPOD);
    }

    /**
     * 是否为 iPad 设备
     *
     * @return 是否为 iPad
     */
    public boolean isIPad() {
        return this.equals(IPAD);
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
        return this.equals(ANDROID) || this.equals(GOOGLE_TV);
    }
}

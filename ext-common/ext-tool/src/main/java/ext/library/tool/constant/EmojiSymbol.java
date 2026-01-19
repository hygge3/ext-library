package ext.library.tool.constant;

/**
 * Emoji 符号常量
 * <p>
 * 用于日志输出时的模块标识，提升日志可读性
 */
public final class EmojiSymbol {

    private EmojiSymbol() {
        // 防止实例化
    }

    // region 通用符号
    public static final String WARN = "⚠️";
    public static final String EXT = "🍃";
    // endregion

    // region ext-common 层
    public static final String TOOL = "🛠️";
    public static final String CORE = "📎";
    public static final String JSON = "📃";
    // endregion

    // region ext-infra 层
    public static final String REDIS = "🔴";
    public static final String CACHE = "💾";
    // endregion

    // region 安全/加密层
    public static final String SECURITY = "🔒";
    public static final String CRYPTO = "🔐";
    // endregion

    // region ext-web 层
    public static final String WEB = "🌐";
    public static final String OPENAPI = "📖";
    public static final String SSE = "📢";
    public static final String WEBSOCKET = "💬";
    // endregion

    // region ext-enhance 层
    public static final String CAPTCHA = "🔢";
    public static final String IDEMPOTENT = "🔁";
    public static final String DESENSITIZE = "🎭";
    public static final String API_CRYPTO = "🔐";
    // endregion

    // region ext-misc 层
    public static final String HTTP = "🖥️";
    public static final String QRCODE = "📱";
    // endregion

}

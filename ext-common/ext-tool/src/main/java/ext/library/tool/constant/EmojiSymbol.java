package ext.library.tool.constant;

/**
 * Emoji 符号常量
 * <p>
 * 用于日志输出时的模块标识，提升日志可读性
 */
public final class EmojiSymbol {

    // region 通用符号
    public static final String WARN = "⚠️";
    public static final String EXT = "🍃";
    // region ext-common 层
    public static final String TOOL = "🛠️";
    // endregion
    public static final String CORE = "📎";
    public static final String JSON = "📃";
    // region ext-infra 层
    public static final String REDIS = "🔴";
    // endregion
    public static final String CACHE = "💾";
    // region 安全/加密层
    public static final String SECURITY = "🔒";
    // endregion
    public static final String CRYPTO = "🔐";
    // region ext-web 层
    public static final String WEB = "🌐";
    // endregion
    public static final String OPENAPI = "📖";
    public static final String SSE = "📢";
    public static final String WEBSOCKET = "💬";
    // region ext-enhance 层
    public static final String CAPTCHA = "🔢";
    // endregion
    public static final String IDEMPOTENT = "🔂";
    public static final String DESENSITIZE = "🎭";
    public static final String TRANS = "🔁";
    public static final String API_CRYPTO = "🔏";
    // region ext-misc 层
    public static final String HTTP = "🖥️";
    // endregion
    public static final String QRCODE = "📱";

    private EmojiSymbol() {
        // 防止实例化
    }
    // endregion

}

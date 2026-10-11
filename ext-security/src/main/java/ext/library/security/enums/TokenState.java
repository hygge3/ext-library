package ext.library.security.enums;

/**
 * Token 状态枚举
 */
public enum TokenState {

    /**
     * 正常状态
     */
    NORMAL("1", "正常"),

    /**
     * 被踢下线
     */
    KICKED_OFFLINE("2", "被踢下线"),

    /**
     * 被顶下线
     */
    REPLACED_OFFLINE("3", "被顶下线"),

    /**
     * 已封禁
     */
    BANNED("4", "已封禁");

    private final String code;
    private final String description;

    TokenState(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据 code 获取枚举值
     *
     * @param code 状态码
     * @return TokenState
     */
    public static TokenState fromCode(String code) {
        for (TokenState state : values()) {
            if (state.code.equals(code)) {
                return state;
            }
        }
        return null;
    }
}

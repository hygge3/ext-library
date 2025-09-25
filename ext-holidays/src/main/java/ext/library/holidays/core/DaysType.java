package ext.library.holidays.core;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 日期类型，工作日对应结果为 0, 休息日对应结果为 1, 节假日对应的结果为 2；
 */
public enum DaysType {

    /**
     * 工作日
     */
    WEEKDAYS((byte) 0),
    /**
     * 休息日
     */
    REST_DAYS((byte) 1),
    /**
     * 节假日
     */
    HOLIDAYS((byte) 2);

    @JsonValue
    private final byte type;

    DaysType(byte type) {
        this.type = type;
    }

    /**
     * 将 type 转换成枚举
     *
     * @param type type
     *
     * @return DaysType
     */
    public static DaysType from(byte type) {
        return switch (type) {
            case 0 -> WEEKDAYS;
            case 1 -> REST_DAYS;
            case 2 -> HOLIDAYS;
            default -> throw new IllegalArgumentException("Unbeknown DaysType:" + type);
        };
    }

    public byte getType() {
        return type;
    }
}
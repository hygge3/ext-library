package ext.library.security.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * TokenState 枚举单元测试
 */
class TokenStateTest {

    @Test
    @DisplayName("枚举值正确定义")
    void tokenState_valuesCorrectlyDefined() {
        assertEquals(4, TokenState.values().length);
        assertNotNull(TokenState.NORMAL);
        assertNotNull(TokenState.KICKED_OFFLINE);
        assertNotNull(TokenState.REPLACED_OFFLINE);
        assertNotNull(TokenState.BANNED);
    }

    @ParameterizedTest
    @CsvSource({
            "1, NORMAL",
            "2, KICKED_OFFLINE",
            "3, REPLACED_OFFLINE",
            "4, BANNED"
    })
    @DisplayName("fromCode: 根据 code 正确获取枚举值")
    void fromCode_validCode_returnsCorrectState(String code, TokenState expectedState) {
        TokenState result = TokenState.fromCode(code);
        assertEquals(expectedState, result);
    }

    @Test
    @DisplayName("fromCode: 无效 code 返回 null")
    void fromCode_invalidCode_returnsNull() {
        assertNull(TokenState.fromCode("invalid"));
        assertNull(TokenState.fromCode("0"));
        assertNull(TokenState.fromCode("5"));
        assertNull(TokenState.fromCode(null));
    }

    @Test
    @DisplayName("getCode: 返回正确的 code 值")
    void getCode_returnsCorrectCode() {
        assertEquals("1", TokenState.NORMAL.getCode());
        assertEquals("2", TokenState.KICKED_OFFLINE.getCode());
        assertEquals("3", TokenState.REPLACED_OFFLINE.getCode());
        assertEquals("4", TokenState.BANNED.getCode());
    }

    @Test
    @DisplayName("getDescription: 返回正确的描述")
    void getDescription_returnsCorrectDescription() {
        assertEquals("正常", TokenState.NORMAL.getDescription());
        assertEquals("被踢下线", TokenState.KICKED_OFFLINE.getDescription());
        assertEquals("被顶下线", TokenState.REPLACED_OFFLINE.getDescription());
        assertEquals("已封禁", TokenState.BANNED.getDescription());
    }
}

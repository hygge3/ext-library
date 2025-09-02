package ext.library.tool.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class SqidsTest {
    @Test
    public void simpleAlphabet() {
        Sqids sqids = Sqids.builder()
                .alphabet("0123456789abcdef")
                .build();
        List<Long> numbers = Arrays.asList(1L, 2L, 3L);
        String id = "489158";
        Assertions.assertEquals(id, sqids.encode(numbers));
        Assertions.assertEquals(sqids.decode(id), numbers);
    }

    @Test
    public void shortAlphabet() {
        Sqids sqids = Sqids.builder()
                .alphabet("abc")
                .build();
        List<Long> numbers = Arrays.asList(1L, 2L, 3L);
        Assertions.assertEquals(sqids.decode(sqids.encode(numbers)), numbers);
    }

    @Test
    public void specialCharsAlphabet() {
        Sqids sqids = Sqids.builder()
                .alphabet(".\\?")
                .build();
        List<Long> numbers = Arrays.asList(1L, 2L, 3L);
        Assertions.assertEquals(sqids.decode(sqids.encode(numbers)), numbers);
    }

    @Test
    public void multibyteCharacters() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Sqids.builder()
                .alphabet("ë1092")
                .build());
    }

    @Test
    public void repeatingAlphabetCharacters() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Sqids.builder()
                .alphabet("aabcdefg")
                .build());
    }

    @Test
    public void tooShortOfAnAlphabet() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Sqids.builder()
                .alphabet("ab")
                .build());
    }
}
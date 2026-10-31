package ext.library.web.validation.validator;

import ext.library.web.validation.constraints.Exclusion;
import ext.library.web.validation.constraints.Mutual;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * {@code BeanUtil.beanToMap} 对每个属性无条件 put，用 {@code containsKey} 判断会让
 * 「至少一个有值」永远通过、「只能一个有值」永远失败。
 */
@DisplayName("组合校验器")
class MutualAndExclusionValidatorTest {

    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    @Mutual({"mobile", "email"})
    @Exclusion({"mobile", "email"})
    static class Annotated {
    }

    public static class Params {

        private String mobile;

        private String email;

        Params(String mobile, String email) {
            this.mobile = mobile;
            this.email = email;
        }

        public String getMobile() {
            return mobile;
        }

        public String getEmail() {
            return email;
        }

    }

    @Test
    @DisplayName("@Mutual 按字段是否有值判断")
    void mutualChecksValue() {
        MutualValidator validator = new MutualValidator();
        validator.initialize(Annotated.class.getAnnotation(Mutual.class));

        assertThat(validator.isValid(new Params(null, null), context)).isFalse();
        assertThat(validator.isValid(new Params("138", null), context)).isTrue();
        assertThat(validator.isValid(new Params(null, "a@b.c"), context)).isTrue();
    }

    @Test
    @DisplayName("@Exclusion 按字段是否有值判断")
    void exclusionChecksValue() {
        ExclusionValidator validator = new ExclusionValidator();
        validator.initialize(Annotated.class.getAnnotation(Exclusion.class));

        assertThat(validator.isValid(new Params(null, null), context)).isTrue();
        assertThat(validator.isValid(new Params("138", null), context)).isTrue();
        assertThat(validator.isValid(new Params("138", "a@b.c"), context)).isFalse();
    }


}

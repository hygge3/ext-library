package ext.library.idempotent;

import ext.library.tool.exception.ExtException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 未填 {@link Idempotent#uniqueExpression()} 时曾直接返回 {@code prefix}，
 * 于是所有业务共用同一个常量 key，第一个请求就把整个应用锁死一个 duration。
 */
@DisplayName("默认幂等键生成")
class DefaultKeyGeneratorTest {

    private final DefaultKeyGenerator generator = new DefaultKeyGenerator();

    static class Service {

        @Idempotent
        public void create() {
        }

        @Idempotent
        public void update() {
        }

        @Idempotent(uniqueExpression = "#p0")
        public void withExpression(String orderNo) {
        }

    }

    @Test
    @DisplayName("未填表达式时按类名与方法名区分")
    void fallsBackToMethodIdentity() throws Exception {
        String create = generator.generate(point("create"), annotation("create"));
        String update = generator.generate(point("update"), annotation("update"));

        assertThat(create).endsWith("#create").contains(Service.class.getName());
        assertThat(update).endsWith("#update");
        assertThat(create).isNotEqualTo(update);
    }

    @Test
    @DisplayName("表达式求值为空时拒绝生成，不共用常量键")
    void rejectsBlankExpressionValue() throws Exception {
        // null 入参会让 #p0 求值为 null，此时若直接拼键就会变成 "idem:null"
        assertThatThrownBy(() -> generator.generate(point("withExpression", new Object[]{null}), annotation("withExpression")))
                .isInstanceOf(ExtException.class)
                .hasMessageContaining("幂等表达式求值为空");
    }

    @Test
    @DisplayName("表达式求值有值时拼入键")
    void usesExpressionValue() throws Exception {
        String key = generator.generate(point("withExpression", new Object[]{"A001"}), annotation("withExpression"));

        assertThat(key).endsWith(":A001");
    }

    private JoinPoint point(String methodName) throws Exception {
        return point(methodName, new Object[0]);
    }

    private JoinPoint point(String methodName, Object[] args) throws Exception {
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method(methodName));
        JoinPoint point = mock(JoinPoint.class);
        when(point.getSignature()).thenReturn(signature);
        when(point.getTarget()).thenReturn(new Service());
        when(point.getArgs()).thenReturn(args);
        return point;
    }

    private Method method(String methodName) throws NoSuchMethodException {
        Class<?>[] paramTypes = "withExpression".equals(methodName) ? new Class<?>[]{String.class} : new Class<?>[0];
        return Service.class.getMethod(methodName, paramTypes);
    }

    private Idempotent annotation(String methodName) throws Exception {
        return method(methodName).getAnnotation(Idempotent.class);
    }

}

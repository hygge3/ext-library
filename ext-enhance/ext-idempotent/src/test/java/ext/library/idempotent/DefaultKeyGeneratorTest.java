package ext.library.idempotent;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
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

    private JoinPoint point(String methodName) throws Exception {
        Method method = Service.class.getMethod(methodName);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        JoinPoint point = mock(JoinPoint.class);
        when(point.getSignature()).thenReturn(signature);
        when(point.getTarget()).thenReturn(new Service());
        return point;
    }

    private Idempotent annotation(String methodName) throws Exception {
        return Service.class.getMethod(methodName).getAnnotation(Idempotent.class);
    }

}

package ext.library.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("审计切面")
class AuditAspectTest {

    private final List<AuditRecord> records = new ArrayList<>();

    /**
     * 被审计的业务方法
     */
    static class DemoService {

        @OperationLog(module = "订单", desc = "下单商品 #orderId")
        public String illegalDesc(Long orderId) {
            return "ok";
        }

        @OperationLog(module = "文件", recordResult = true)
        public String unserializableArgs(String name, Object broken) {
            return "ok";
        }

    }

    /**
     * 序列化就会报错的入参
     */
    public static class Broken {

        public String getValue() {
            throw new IllegalStateException("boom");
        }

    }

    private AuditAspect aspect(ProceedingJoinPoint joinPoint) {
        AuditDispatcher dispatcher = new AuditDispatcher(List.of(records::add));
        return new AuditAspect(dispatcher, () -> "tester");
    }

    private ProceedingJoinPoint joinPoint(Method method, Object result, Object... args) {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(new DemoService());
        when(joinPoint.getArgs()).thenReturn(args);
        try {
            when(joinPoint.proceed()).thenReturn(result);
        } catch (Throwable ignored) {
            // mock 的 proceed 不会抛
        }
        return joinPoint;
    }

    @Test
    @DisplayName("desc 表达式非法时业务照常执行、退回原串")
    void illegalDescFallsBackToRawString() throws Throwable {
        Method method = DemoService.class.getMethod("illegalDesc", Long.class);

        Object result = aspect(joinPoint(method, "ok", 1L)).around(joinPoint(method, "ok", 1L), method.getAnnotation(OperationLog.class));

        assertThat(result).isEqualTo("ok");
        assertThat(records).hasSize(1);
        assertThat(records.getFirst().desc()).isEqualTo("下单商品 #orderId");
        assertThat(records.getFirst().success()).isTrue();
    }

    @Test
    @DisplayName("入参不可序列化时仍然留痕，并用占位符")
    void unserializableArgsStillRecorded() throws Throwable {
        Method method = DemoService.class.getMethod("unserializableArgs", String.class, Object.class);
        ProceedingJoinPoint joinPoint = joinPoint(method, "ok", "a.txt", new Broken());

        aspect(joinPoint).around(joinPoint, method.getAnnotation(OperationLog.class));

        assertThat(records).hasSize(1);
        assertThat(records.getFirst().module()).isEqualTo("文件");
        assertThat(records.getFirst().args()).contains("序列化失败");
    }

}

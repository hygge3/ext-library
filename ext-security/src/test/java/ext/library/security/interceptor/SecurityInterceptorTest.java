package ext.library.security.interceptor;

import ext.library.core.datascope.DataScopeContext;
import ext.library.core.datascope.DataScopeMeta;
import ext.library.security.annotation.DataScope;
import ext.library.security.enums.DataScopeType;
import ext.library.tool.exception.ExtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 数据权限解析测试
 */
@DisplayName("数据权限解析测试")
class SecurityInterceptorTest {

    @DataScope(field = "dept_id", type = DataScopeType.DEPT)
    private void scoped() {
    }

    @DataScope(field = "dept_id", type = DataScopeType.ALL)
    private void all() {
    }

    private void none() {
    }

    private Method method(String name) throws NoSuchMethodException {
        return SecurityInterceptorTest.class.getDeclaredMethod(name);
    }

    @Test
    @DisplayName("按类型解析范围并写入上下文")
    void resolvesScope() throws Exception {
        SecurityInterceptor interceptor = new SecurityInterceptor(type -> List.of(1L, 2L));

        interceptor.applyDataScope(method("scoped"));

        DataScopeMeta meta = DataScopeContext.get();
        assertEquals("dept_id", meta.field());
        assertEquals(List.of(1L, 2L), meta.values());
        DataScopeContext.clear();
    }

    @Test
    @DisplayName("ALL 类型不写入上下文")
    void allTypeSkips() throws Exception {
        SecurityInterceptor interceptor = new SecurityInterceptor(type -> List.of(1L));

        interceptor.applyDataScope(method("all"));

        assertNull(DataScopeContext.get());
    }

    @Test
    @DisplayName("无注解时不写入上下文")
    void noAnnotationSkips() throws Exception {
        SecurityInterceptor interceptor = new SecurityInterceptor(type -> List.of(1L));

        interceptor.applyDataScope(method("none"));

        assertNull(DataScopeContext.get());
    }

    @Test
    @DisplayName("缺少 DataScopeProvider 时报错")
    void missingProviderThrows() throws Exception {
        SecurityInterceptor interceptor = new SecurityInterceptor();

        assertThrows(ExtException.class, () -> interceptor.applyDataScope(method("scoped")));
        assertNull(DataScopeContext.get());
    }

}

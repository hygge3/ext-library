package ext.library.core.datascope;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 数据权限范围测试
 */
@DisplayName("数据权限范围测试")
class DataScopeMetaTest {

    @Test
    @DisplayName("condition 生成 IN 条件并用占位符绑定值")
    void conditionWithValues() {
        DataScopeMeta meta = new DataScopeMeta("dept_id", List.of(1L, 2L));
        assertEquals("dept_id IN (?, ?)", meta.condition());
    }

    @Test
    @DisplayName("可见值为空时返回恒假条件")
    void conditionWithEmptyValues() {
        assertEquals("1=0", new DataScopeMeta("dept_id", List.of()).condition());
        assertEquals("1=0", new DataScopeMeta("dept_id", null).condition());
    }

    @Test
    @DisplayName("上下文按线程存取，clear 后为空")
    void contextSetGetClear() {
        assertNull(DataScopeContext.get());

        DataScopeMeta meta = new DataScopeMeta("dept_id", List.of(9L));
        DataScopeContext.set(meta);
        assertEquals(meta, DataScopeContext.get());

        DataScopeContext.clear();
        assertNull(DataScopeContext.get());
    }

}

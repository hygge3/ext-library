package ext.library.servlet.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 分页模型测试
 */
@DisplayName("分页模型测试")
class PageModelTest {

    @Test
    @DisplayName("默认第 1 页、每页 10 条")
    void defaults() {
        PageQuery query = new PageQuery();
        assertEquals(1, query.getPage());
        assertEquals(10, query.getSize());
        assertEquals(0, query.offset());
    }

    @Test
    @DisplayName("offset：第 3 页每页 20 条 -> 40")
    void offset() {
        PageQuery query = new PageQuery();
        query.setPage(3);
        query.setSize(20);
        assertEquals(40, query.offset());
    }

    @Test
    @DisplayName("offset：非法页码/条数按最小值兜底")
    void offsetWithIllegalValue() {
        PageQuery query = new PageQuery();
        query.setPage(0);
        query.setSize(0);
        assertEquals(0, query.offset());
    }

    @Test
    @DisplayName("empty：回填页码与条数，records 为空")
    void emptyResult() {
        PageQuery query = new PageQuery();
        query.setPage(2);
        query.setSize(5);

        PageResult<String> result = PageResult.empty(query);

        assertEquals(2, result.page());
        assertEquals(5, result.size());
        assertEquals(0L, result.total());
        assertEquals(0L, result.pages());
        assertTrue(result.records().isEmpty());
    }

    @Test
    @DisplayName("records 原样保留")
    void recordsPreserved() {
        PageResult<String> result = new PageResult<>(1, 10, 2L, 1L, List.of("a", "b"));
        assertEquals(List.of("a", "b"), result.records());
        assertEquals(2L, result.total());
    }

}

package ext.library.servlet.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("分页参数")
class PageQueryTest {

    @Test
    @DisplayName("每页条数被限制在上限内，页码最小为 1")
    void clampsSizeAndPage() {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setSize(100_000_000L);
        pageQuery.setPage(0);

        assertThat(pageQuery.getSize()).isEqualTo(500);
        assertThat(pageQuery.getPage()).isEqualTo(1);

        pageQuery.setSize(-1);
        assertThat(pageQuery.getSize()).isEqualTo(1);
    }

    @Test
    @DisplayName("页码离谱时偏移量不溢出成负数")
    void offsetDoesNotOverflow() {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setSize(500);
        pageQuery.setPage(Long.MAX_VALUE);

        assertThat(pageQuery.offset()).isPositive();
    }

}

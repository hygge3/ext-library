package ext.library.mybatis.page;

import com.mybatisflex.core.paginate.Page;

import jakarta.validation.constraints.Min;

/**
 * 分页查询参数
 */
public class PageParam {

    /** 当前页码，从 1 开始 */
    @Min(value = 1, message = "当前页不能小于 1")
    private long page = 1;

    /** 每页显示条数，最大值为系统设置，默认 10 */
    @Min(value = 1, message = "每页显示条数不能小于 1")
    private long size = 10;

    /**
     * 数据总量
     */
    private long total = Page.INIT_VALUE;

    public <T> Page<T> toPage() {
        return Page.of(this.page, this.size, this.total);
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
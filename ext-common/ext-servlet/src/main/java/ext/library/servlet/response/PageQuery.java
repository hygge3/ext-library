package ext.library.servlet.response;

/**
 * 分页查询参数
 * <p>
 * Web 层与 infra 层共用同一份定义：控制器参数绑定、DAO 分页查询都直接用它。
 *
 * @since 4.1.0
 */
public class PageQuery {

    /**
     * 当前页码，从 1 开始
     */
    private long page = 1;

    /**
     * 每页显示条数，默认 10
     */
    private long size = 10;

    /**
     * 数据总量，由查询结果回填
     */
    private long total;

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

    /**
     * 计算偏移量
     *
     * @return 偏移量，从 0 开始
     */
    public long offset() {
        return (Math.max(page, 1) - 1) * Math.max(size, 1);
    }

}

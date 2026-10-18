package ext.library.jdbc;

/**
 * 分页查询参数
 * <p>
 * <b>这是 infra 层自有的分页 DTO。</b>{@code ext-web/ext-mvc} 中的同名类属于 Web 层，
 * 由于 infra 不能反向依赖 web，此处独立定义一份，两者字段保持一致。
 * Web 层使用时由 MVC 层负责转换。
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

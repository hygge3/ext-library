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
     * 每页条数上限
     * <p>
     * 没有上限时 {@code ?size=100000000} 会被直接拼进 {@code LIMIT}，一个请求就能把实例打挂。
     */
    private static final long MAX_SIZE = 500;

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
        this.page = Math.max(page, 1);
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = Math.min(Math.max(size, 1), MAX_SIZE);
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
        // 页码离谱时避免真实溢出：溢出说明这条查询没有意义，放到一个肯定越过后全部数据的位置
        try {
            return Math.multiplyExact(page - 1, size);
        } catch (ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }

}

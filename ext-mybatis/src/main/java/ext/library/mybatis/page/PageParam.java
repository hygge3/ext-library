package ext.library.mybatis.page;

import com.mybatisflex.core.paginate.Page;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Min;

/**
 * 分页查询参数
 */
@Getter
@Setter
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

}
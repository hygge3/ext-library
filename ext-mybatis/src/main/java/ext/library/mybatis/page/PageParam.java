package ext.library.mybatis.page;

import jakarta.validation.constraints.Min;

import com.github.pagehelper.PageHelper;
import com.mybatisflex.core.paginate.Page;
import lombok.Data;

/**
 * 分页查询参数
 */
@Data
public class PageParam {

    /** 当前页码，从 1 开始 */
    @Min(value = 1, message = "当前页不能小于 1")
    long page = 1;

    /** 每页显示条数，最大值为系统设置，默认 10 */
    @Min(value = 1, message = "每页显示条数不能小于 1")
    long size = 10;

    public <T> Page<T> toPage() {
        return Page.of(this.getPage(), this.getSize());
    }

    public void startPage() {
        PageHelper.startPage(Math.toIntExact(page), Math.toIntExact(size));
    }
}

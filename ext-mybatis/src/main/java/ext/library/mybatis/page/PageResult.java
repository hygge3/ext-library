package ext.library.mybatis.page;

import jakarta.annotation.Nonnull;

import com.mybatisflex.core.paginate.Page;
import ext.library.core.util.BeanUtil;
import ext.library.tool.$;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 分页返回结果
 */
@Getter
@Setter
@AllArgsConstructor
public class PageResult<T> {

    /** 当前页码 */
    final long page;

    /** 每页显示条数 */
    final long size;

    /**
     * 数据总量
     */
    final Long total;

    /** 页数 */
    final Long pages;

    /**
     * 查询数据列表
     */
    final List<T> records;

    public static <T> PageResult<T> empty(PageParam param) {
        return new PageResult<>(param.getPage(), param.getSize(), 0L, 0L, Collections.emptyList());
    }

    public static <T> PageResult<T> of(@Nonnull Page<T> page) {
        return new PageResult<>(page.getPageNumber(), page.getPageSize(), page.getTotalRow(), page.getTotalPage(), page.getRecords());
    }

    public static <T> PageResult<T> of(@Nonnull Page<?> page, Class<T> targetClass) {
        List<T> records;
        if ($.isEmpty(page.getRecords())) {
            records = Collections.emptyList();
        } else {
            records = BeanUtil.convert(page.getRecords(), targetClass);
        }
        return new PageResult<>(page.getPageNumber(), page.getPageSize(), page.getTotalRow(), page.getTotalPage(), records);
    }

}

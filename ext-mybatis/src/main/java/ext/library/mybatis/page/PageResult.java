package ext.library.mybatis.page;

import jakarta.annotation.Nonnull;

import com.github.pagehelper.PageInfo;
import com.mybatisflex.core.paginate.Page;
import ext.library.core.util.BeanUtil;
import ext.library.tool.$;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分页返回结果
 */
@Getter
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

    /**
     * 查询数据列表
     */
    final List<T> records;

    public static <T> PageResult<T> empty(PageParam param) {
        return new PageResult<>(param.getPage(), param.getSize(), 0L, Collections.emptyList());
    }

    public static <T> PageResult<T> of(@Nonnull Page<T> page) {
        return new PageResult<>(page.getPageNumber(), page.getPageSize(), page.getTotalRow(), page.getRecords());
    }

    public static <T> PageResult<T> of(@Nonnull Page<?> page, Class<T> targetClass) {
        List<T> records;
        if ($.isEmpty(page.getRecords())) {
            records = Collections.emptyList();
        } else {
            records = BeanUtil.convert(page.getRecords(), targetClass);
        }
        return new PageResult<>(page.getPageNumber(), page.getPageSize(), page.getTotalRow(), records);
    }

    public static <T> PageResult<T> of(@Nonnull com.github.pagehelper.Page<T> page) {
        return new PageResult<>(page.getPages(), page.getPageSize(), page.getTotal(), page.getResult());
    }

    public static <T> PageResult<T> of(@Nonnull com.github.pagehelper.Page<?> page, Class<T> targetClass) {
        List<T> records;
        if ($.isEmpty(page.getResult())) {
            records = Collections.emptyList();
        } else {
            records = BeanUtil.convert(page.getResult(), targetClass);
        }
        return new PageResult<>(page.getPages(), page.getPageSize(), page.getTotal(), records);
    }

    public static <T> PageResult<T> of(@Nonnull PageInfo<T> page) {
        return new PageResult<>(page.getPages(), page.getPageSize(), page.getTotal(), page.getList());
    }

    public static <T> PageResult<T> of(@Nonnull PageInfo<?> page, Class<T> targetClass) {
        List<T> records;
        if ($.isEmpty(page.getList())) {
            records = Collections.emptyList();
        } else {
            records = BeanUtil.convert(page.getList(), targetClass);
        }
        return new PageResult<>(page.getPages(), page.getPageSize(), page.getTotal(), records);
    }

}

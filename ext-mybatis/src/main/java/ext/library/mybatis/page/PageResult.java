package ext.library.mybatis.page;

import com.mybatisflex.core.paginate.Page;
import ext.library.core.util.BeanUtil;
import ext.library.tool.util.ObjectUtil;

import jakarta.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * 分页返回结果
 *
 * @param page    当前页码
 * @param size    每页显示条数
 * @param total   数据总量
 * @param pages   页数
 * @param records 查询数据列表
 */
public record PageResult<T>(long page, long size, Long total, Long pages, List<T> records) {

    public static <T> PageResult<T> empty(PageParam param) {
        return new PageResult<>(param.getPage(), param.getSize(), 0L, 0L, Collections.emptyList());
    }

    public static <T> PageResult<T> of(@Nonnull Page<T> page) {
        return new PageResult<>(page.getPageNumber(), page.getPageSize(), page.getTotalRow(), page.getTotalPage(), page.getRecords());
    }

    public static <T> PageResult<T> of(@Nonnull Page<?> page, Class<T> targetClass) {
        List<T> records;
        if (ObjectUtil.isEmpty(page.getRecords())) {
            records = Collections.emptyList();
        } else {
            records = BeanUtil.convert(page.getRecords(), targetClass);
        }
        return new PageResult<>(page.getPageNumber(), page.getPageSize(), page.getTotalRow(), page.getTotalPage(), records);
    }

}
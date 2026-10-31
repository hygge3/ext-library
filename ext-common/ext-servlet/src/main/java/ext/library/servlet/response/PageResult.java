package ext.library.servlet.response;

import java.util.Collections;
import java.util.List;

/**
 * 分页返回结果
 *
 * @param page    当前页码
 * @param size    每页显示条数
 * @param total   数据总量
 * @param pages   总页数
 * @param records 查询数据列表
 * @param <T>     记录类型
 * @since 4.1.0
 */
public record PageResult<T>(long page, long size, Long total, Long pages, List<T> records) {

    /**
     * 空分页结果
     *
     * @param query 分页参数
     * @param <T>   记录类型
     *
     * @return 空分页结果
     */
    public static <T> PageResult<T> empty(PageQuery query) {
        return new PageResult<>(query.getPage(), query.getSize(), 0L, 0L, Collections.emptyList());
    }

}

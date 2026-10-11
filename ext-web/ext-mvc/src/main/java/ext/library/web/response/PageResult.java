package ext.library.web.response;

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

    public static <T> PageResult<T> empty(PageQuery param) {
        return new PageResult<>(param.getPage(), param.getSize(), 0L, 0L, Collections.emptyList());
    }

}

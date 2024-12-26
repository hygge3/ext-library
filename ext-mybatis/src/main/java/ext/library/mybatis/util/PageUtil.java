package ext.library.mybatis.util;

import java.util.List;
import java.util.function.Supplier;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mybatisflex.core.constant.SqlConsts;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryOrderBy;
import ext.library.mybatis.page.PageParam;
import ext.library.tool.$;
import org.jetbrains.annotations.NotNull;

/**
 * 分页工具类
 */
public class PageUtil extends PageHelper {

    /**
     * 设置请求分页数据
     */
    public static void startPage(@NotNull PageParam param) {
        PageHelper.startPage(Math.toIntExact(param.getPage()), Math.toIntExact(param.getSize()));
    }

    /**
     * 设置请求分页数据
     */
    public static <E> PageInfo<E> startPage(@NotNull PageParam param, Supplier<List<E>> select) {
        return PageHelper.startPage(Math.toIntExact(param.getPage()), Math.toIntExact(param.getSize())).doSelectPageInfo(select::get);
    }

    /**
     * 清理分页的线程变量
     */
    public static void clearPage() {
        PageHelper.clearPage();
    }

    /**
     * 构建排序
     */
    @NotNull
    public static QueryOrderBy[] buildOrderBy(@NotNull PageParam param) {
        List<PageParam.Sort> sorts = param.getSorts();
        if ($.isEmpty(sorts)) {
            return new QueryOrderBy[]{};
        }
        QueryOrderBy[] orderBys = new QueryOrderBy[sorts.size()];
        // 每个字段各自排序
        for (int i = 0; i < sorts.size(); i++) {
            PageParam.Sort sort = sorts.get(i);
            if (sort.isAsc()) {
                orderBys[i] = new QueryOrderBy(new QueryColumn(sort.getField()), SqlConsts.ASC);
            } else {
                orderBys[i] = new QueryOrderBy(new QueryColumn(sort.getField()), SqlConsts.DESC);
            }
        }
        return orderBys;
    }

}

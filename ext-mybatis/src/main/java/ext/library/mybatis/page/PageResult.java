package ext.library.mybatis.page;

import jakarta.annotation.Nonnull;

import com.github.pagehelper.PageInfo;
import com.mybatisflex.core.paginate.Page;
import ext.library.core.util.BeanUtil;
import ext.library.tool.$;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.List;
import lombok.Data;

/**
 * 分页返回结果
 */
@Data
@Schema(title = "分页返回结果")
public class PageResult<T> {

    /**
     * 查询数据列表
     */
    @Schema(title = "分页数据")
    protected List<T> records = Collections.emptyList();

    @Schema(title = "当前页码")
    long page = 1;

    @Schema(title = "每页显示条数")
    long size = 10;

    /**
     * 总数
     */
    @Schema(title = "数据总量")
    protected Long total = 0L;

    public PageResult() {
    }

    public PageResult(long total) {
        this.total = total;
    }

    /**
     * 分页
     *
     * @param records 列表数据
     * @param total   总记录数
     */
    public PageResult(List<T> records, long total) {
        this.records = records;
        this.total = total;
    }

    public PageResult(@Nonnull Page<T> page) {
        this.page = page.getPageNumber();
        this.size = page.getPageSize();
        this.records = page.getRecords();
        this.total = page.getTotalRow();
    }

    public <S> PageResult(@Nonnull Page<S> page, Class<T> targetClass) {
        this.page = page.getPageNumber();
        this.size = page.getPageSize();
        if ($.isEmpty(page.getRecords())) {
            this.records = Collections.emptyList();
        } else {
            this.records = BeanUtil.convert(page.getRecords(), targetClass);
        }
        this.total = page.getTotalRow();
    }

    public PageResult(@Nonnull com.github.pagehelper.Page<T> page) {
        this.page = page.getPages();
        this.size = page.getPageSize();
        this.records = page.getResult();
        this.total = page.getTotal();
    }

    public <S> PageResult(@Nonnull com.github.pagehelper.Page<S> page, Class<T> targetClass) {
        this.page = page.getPages();
        this.size = page.getPageSize();
        if ($.isEmpty(page.getResult())) {
            this.records = Collections.emptyList();
        } else {
            this.records = BeanUtil.convert(page.getResult(), targetClass);
        }
        this.total = page.getTotal();
    }

    public PageResult(@Nonnull PageInfo<T> page) {
        this.page = page.getPages();
        this.size = page.getPageSize();
        this.records = page.getList();
        this.total = page.getTotal();
    }

    public <S> PageResult(@Nonnull PageInfo<S> page, Class<T> targetClass) {
        this.page = page.getPages();
        this.size = page.getPageSize();
        if ($.isEmpty(page.getList())) {
            this.records = Collections.emptyList();
        } else {
            this.records = BeanUtil.convert(page.getList(), targetClass);
        }
        this.total = page.getTotal();
    }

    public PageResult(long page, long size, @Nonnull List<T> list) {
        this.page = page;
        this.size = size;
        this.total = (long) list.size();
        if ($.isEmpty(list)) {
            this.records = Collections.emptyList();
        } else {
            page = page - 1;
            long fromIndex = page * size;
            if (fromIndex >= total) {
                this.records = list;
                return;
            }
            long toIndex = ((page + 1) * size);
            if (toIndex > total) {
                toIndex = total;
            }
            this.records = list.subList(Math.toIntExact(fromIndex), Math.toIntExact(toIndex));
        }
    }

    public PageResult(@Nonnull List<T> list) {
        this.records = list;
        this.total = (long) list.size();
    }

}

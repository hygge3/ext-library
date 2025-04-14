package ext.library.mybatis.page;

import jakarta.annotation.Nonnull;

import com.github.pagehelper.PageInfo;
import com.mybatisflex.core.paginate.Page;
import ext.library.core.util.BeanUtil;
import ext.library.tool.$;
import java.util.Collections;
import java.util.List;
import lombok.Data;

/**
 * 分页返回结果
 */
@Data
public class PageResult<T> {

    /**
     * 查询数据列表
     */
    protected List<T> records;

    /** 当前页码 */
    long page;

    /** 每页显示条数 */
    long size;

    /**
     * 数据总量
     */
    protected Long total;

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

    /**
     * 构造函数，用于初始化 PageResult 对象
     * 此构造函数接收当前页码、页面大小和一个列表，计算总记录数，并根据当前页码和大小从列表中提取当前页的记录
     *
     * @param page 当前页码，用于确定从列表中提取哪一部分作为当前页的记录
     * @param size 页面大小，用于确定每页包含的记录数
     * @param list 原始列表，从中提取当前页的记录
     */
    public PageResult(long page, long size, @Nonnull List<T> list) {
        this.page = page;
        this.size = size;
        // 计算总记录数
        this.total = (long) list.size();
        // 判断列表是否为空，如果为空，则将 records 设置为空列表
        if ($.isEmpty(list)) {
            this.records = Collections.emptyList();
        } else {
            // 由于页码从 1 开始计数，所以在计算索引时需要减 1
            page = page - 1;
            // 计算当前页的起始索引
            long fromIndex = page * size;
            // 如果起始索引大于等于总记录数，说明当前页码已经超出列表范围，直接将整个列表作为当前页的记录返回
            if (fromIndex >= total) {
                this.records = list;
                return;
            }
            // 计算当前页的结束索引
            long toIndex = ((page + 1) * size);
            // 如果结束索引大于总记录数，说明已经超出列表范围，需要将结束索引设置为总记录数
            if (toIndex > total) {
                toIndex = total;
            }
            // 从列表中提取当前页的记录
            this.records = list.subList(Math.toIntExact(fromIndex), Math.toIntExact(toIndex));
        }
    }

}

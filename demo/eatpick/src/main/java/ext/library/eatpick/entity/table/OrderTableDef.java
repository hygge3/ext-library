package ext.library.eatpick.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

import java.io.Serial;

/**
 * 订单 表定义层。
 *
 * @author Auto Codegen By Ext
 * @since 2024-12-18
 */
public class OrderTableDef extends TableDef {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单
     */
    public static final OrderTableDef ORDER = new OrderTableDef();

    /**
     * 主键，按时间生成
     */
    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 备注
     */
    public final QueryColumn NOTES = new QueryColumn(this, "notes");

    /**
     * 评论
     */
    public final QueryColumn REVIEW = new QueryColumn(this, "review");

    /**
     * 订单状态：0 取消；1 已下单；2 制作中；3 已完成；4 已评价
     */
    public final QueryColumn STATUS = new QueryColumn(this, "status");

    /**
     * 记录创建时间
     */
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    /**
     * 顾客 ID
     */
    public final QueryColumn CUSTOMER_ID = new QueryColumn(this, "customer_id");

    /**
     * 记录删除时间
     */
    public final QueryColumn DELETE_TIME = new QueryColumn(this, "delete_time");

    /**
     * 记录上次修改时间
     */
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CUSTOMER_ID, NOTES, STATUS, REVIEW, CREATE_TIME, UPDATE_TIME, };

    public OrderTableDef() {
        super("", "order");
    }

    private OrderTableDef(String schema, String name, String alisa) {
        super(schema, name, alisa);
    }

    public OrderTableDef as(String alias) {
        String key = getNameWithSchema() + "." + alias;
        return getCache(key, k -> new OrderTableDef("", "order", alias));
    }

}

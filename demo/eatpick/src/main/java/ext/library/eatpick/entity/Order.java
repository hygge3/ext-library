package ext.library.eatpick.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单 实体类。
 *
 * @author Auto Codegen By Ext
 * @since 2024-12-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("order")
public class Order implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键，按时间生成
     */
    @Id
    private String id;

    /**
     * 顾客 ID
     */
    private Long customerId;

    /**
     * 备注
     */
    private String notes;

    /**
     * 订单状态：0 取消；1 已下单；2 制作中；3 已完成；4 已评价
     */
    private Integer status;

    /**
     * 评论
     */
    private String review;

    /**
     * 记录创建时间
     */
    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    /**
     * 记录上次修改时间
     */
    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    /**
     * 记录删除时间
     */
    @Column(isLogicDelete = true)
    private LocalDateTime deleteTime;

}

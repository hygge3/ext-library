package ext.library.eatpick.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.mybatisflex.annotation.Id;
import ext.library.eatpick.entity.Order;
import ext.library.eatpick.entity.Recipe;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AutoMapper(target = Order.class)
public class OrderVO {

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
    private LocalDateTime createTime;

    /** 关联菜谱 */
    private List<Recipe> recipes;


}

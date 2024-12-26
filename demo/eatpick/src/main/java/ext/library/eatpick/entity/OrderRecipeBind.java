package ext.library.eatpick.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;

import java.io.Serial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单菜谱关联 实体类。
 *
 * @author Auto Codegen By Ext
 * @since 2024-12-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("order_recipe_bind")
public class OrderRecipeBind implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单 ID
     */
    @Id
    private String orderId;

    /**
     * 菜谱 ID
     */
    private Long recipeId;

}

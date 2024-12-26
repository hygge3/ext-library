package ext.library.eatpick.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 食材 实体类。
 *
 * @author Auto Codegen By Ext
 * @since 2024-12-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("ingredient")
public class Ingredient implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 图片
     */
    private String cover;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String desc;

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

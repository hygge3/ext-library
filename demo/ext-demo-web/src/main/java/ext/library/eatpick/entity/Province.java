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
 * 地区 - 省 实体类。
 *
 * @author Auto Codegen By Ext
 * @since 2024-11-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("region_province")
public class Province implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 编码
     */
    @Id
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 是否显示
     */
    private Integer display;

}

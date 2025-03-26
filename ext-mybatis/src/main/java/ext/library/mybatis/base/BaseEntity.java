package ext.library.mybatis.base;

import com.mybatisflex.annotation.Column;
import ext.library.core.util.BeanUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 实体类基类
 */
@Getter
@Setter
public abstract class BaseEntity implements Serializable {

	@Serial
	 static final long serialVersionUID = 1L;

	/**
	 * 创建者
	 */
	@Schema(title = "创建者")
	 Long createBy;

	/**
	 * 创建时间
	 */
	@Schema(title = "创建时间")
	@Column(onInsertValue = "NOW()")
	 LocalDateTime createTime;

	/**
	 * 更新者
	 */
	@Schema(title = "更新者")
	 Long updateBy;

	/**
	 * 修改时间
	 */
	@Schema(title = "修改时间")
	@Column(onUpdateValue = "NOW()")
	 LocalDateTime updateTime;

	/**
	 * 记录删除时间
	 */
	@Schema(title = "逻辑删除标识，已删除：删除时间戳，未删除：NULL")
	@Column(isLogicDelete = true)
	 LocalDateTime deleteTime;

	/**
	 * 类型转换
	 * @param targetType 目标类型
	 * @return {@code T }
	 */
	public <T> T cast(Class<T> targetType) {
		return BeanUtil.convert(this, targetType);
	}

}

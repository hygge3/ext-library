package ext.library.mybatis.base;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.mybatisflex.annotation.Column;
import ext.library.core.util.BeanUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 实体类基类
 */
@Getter
@Setter
public abstract class BaseEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 创建者
	 */
	@Schema(title = "创建者")
	private Long createBy;

	/**
	 * 创建时间
	 */
	@Schema(title = "创建时间")
	@Column(onInsertValue = "NOW()")
	private LocalDateTime createTime;

	/**
	 * 更新者
	 */
	@Schema(title = "更新者")
	private Long updateBy;

	/**
	 * 修改时间
	 */
	@Schema(title = "修改时间")
	@Column(onUpdateValue = "NOW()")
	private LocalDateTime updateTime;

	/**
	 * 记录删除时间
	 */
	@Schema(title = "逻辑删除标识，已删除：删除时间戳，未删除：NULL")
	@Column(isLogicDelete = true)
	private LocalDateTime deleteTime;

	/**
	 * 类型转换
	 * @param targetType 目标类型
	 * @return {@code T }
	 */
	public <T> T cast(Class<T> targetType) {
		return BeanUtil.convert(this, targetType);
	}

}

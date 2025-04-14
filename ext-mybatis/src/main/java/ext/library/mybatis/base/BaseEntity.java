package ext.library.mybatis.base;

import ext.library.core.util.BeanUtil;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * 实体类基类
 */
@Getter
@Setter
public abstract class BaseEntity implements Serializable {

    /**
     * 类型转换
     *
     * @param targetType 目标类型
     * @return {@code T }
     */
    public <T> T convert(Class<T> targetType) {
        return BeanUtil.convert(this, targetType);
    }

}

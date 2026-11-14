package ext.library.security.authority;

import ext.library.security.enums.DataScopeType;

import java.util.List;

/**
 * 数据权限解析 SPI
 * <p>
 * 业务方实现此接口，按类型返回当前用户可见的数据范围值。
 * 当前登录用户可通过 {@code SecurityUtil.getCurrentLoginId()} 获取。
 *
 * @since 4.1.0
 */
public interface DataScopeProvider {

    /**
     * 解析数据范围值
     *
     * @param type 数据范围类型
     *
     * @return 可见值集合；空集合表示无数据可见
     */
    List<Object> resolve(DataScopeType type);

}

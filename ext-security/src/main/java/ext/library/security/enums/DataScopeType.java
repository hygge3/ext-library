package ext.library.security.enums;

/**
 * 数据权限范围类型
 *
 * @since 4.1.0
 */
public enum DataScopeType {

    /**
     * 全部数据，不追加条件
     */
    ALL,

    /**
     * 本部门
     */
    DEPT,

    /**
     * 本部门及以下
     */
    DEPT_AND_CHILD,

    /**
     * 仅本人
     */
    SELF,

    /**
     * 自定义部门集合
     */
    CUSTOM

}

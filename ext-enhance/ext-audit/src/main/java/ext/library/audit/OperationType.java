package ext.library.audit;

/**
 * 操作类型
 */
public enum OperationType {

    /** 查询 */
    SELECT,
    /** 新增 */
    INSERT,
    /** 修改 */
    UPDATE,
    /** 删除 */
    DELETE,
    /** 导入 */
    IMPORT,
    /** 导出 */
    EXPORT,
    /** 授权 */
    GRANT,
    /** 登录 */
    LOGIN,
    /** 登出 */
    LOGOUT,
    /** 其他 */
    OTHER

}

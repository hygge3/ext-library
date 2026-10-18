package ext.library.audit;

/**
 * 审计记录处理器
 * <p>
 * 业务侧实现本接口，把审计记录落库、投递到 MQ 或打到独立日志文件。
 * 可注册多个实现，全部会被调用；任一实现抛异常不影响其他实现，也不影响业务方法。
 */
@FunctionalInterface
public interface AuditHandler {

    /**
     * 处理一条审计记录
     *
     * @param record 审计记录
     */
    void handle(AuditRecord record);

}

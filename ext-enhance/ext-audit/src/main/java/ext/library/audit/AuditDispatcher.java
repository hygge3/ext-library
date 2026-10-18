package ext.library.audit;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;

import java.util.List;

/**
 * 审计记录分发器
 * <p>
 * 持有所有 {@link AuditHandler}，逐个投递审计记录。
 * 单个处理器异常会被吞掉并记日志——审计不应影响业务主流程。
 */
public class AuditDispatcher {

    /** 审计处理器列表 */
    private final List<AuditHandler> handlers;

    /**
     * 构造分发器
     *
     * @param handlers 审计处理器列表
     */
    public AuditDispatcher(List<AuditHandler> handlers) {
        this.handlers = List.copyOf(handlers);
    }

    /**
     * 投递一条审计记录
     *
     * @param record 审计记录
     */
    public void dispatch(AuditRecord record) {
        for (AuditHandler handler : handlers) {
            try {
                handler.handle(record);
            } catch (RuntimeException e) {
                Logs.warn(EmojiSymbol.AUDIT, e, "审计处理器 {} 执行失败，audit: {}",
                        handler.getClass().getSimpleName(), record.module());
            }
        }
    }

}

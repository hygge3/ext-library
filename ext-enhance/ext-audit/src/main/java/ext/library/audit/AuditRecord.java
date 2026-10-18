package ext.library.audit;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 审计记录
 *
 * @param operator   操作人标识，未登录时为 {@code "anonymous"}
 * @param module     业务模块名
 * @param type       操作类型
 * @param desc       操作描述
 * @param args       方法入参（已序列化为 JSON），未记录时为 {@code null}
 * @param result     方法返回值（已序列化为 JSON），未记录时为 {@code null}
 * @param success    方法是否执行成功
 * @param error      异常摘要，成功时为 {@code null}
 * @param duration   方法执行耗时
 * @param timestamp  记录时间
 */
public record AuditRecord(
        String operator,
        String module,
        OperationType type,
        String desc,
        String args,
        String result,
        boolean success,
        String error,
        Duration duration,
        LocalDateTime timestamp
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

}

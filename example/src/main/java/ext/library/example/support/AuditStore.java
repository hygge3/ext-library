package ext.library.example.support;

import ext.library.audit.AuditHandler;
import ext.library.audit.AuditRecord;
import ext.library.tool.util.IdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * 把 ext-audit 的 @OperationLog 记录落进 audit_log（库表见 db/schema.sql）。
 * <p>
 * 处理器抛异常不会影响业务方法，所以这里直接写库，不做重试。
 */
@Component
@RequiredArgsConstructor
public class AuditStore implements AuditHandler {

    private final JdbcClient jdbcClient;

    @Override
    public void handle(AuditRecord record) {
        jdbcClient.sql("""
                        INSERT INTO audit_log (id, operator, module, type, description, args, success, error,
                                               duration_ms, created_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """)
                .params(IdUtil.getUUIDv7(), record.operator(), record.module(), record.type().name(),
                        record.desc(), truncate(record.args(), 4000), record.success(),
                        truncate(record.error(), 500), record.duration().toMillis(), record.timestamp())
                .update();
    }

    private static String truncate(String text, int max) {
        return text == null || text.length() <= max ? text : text.substring(0, max);
    }
}

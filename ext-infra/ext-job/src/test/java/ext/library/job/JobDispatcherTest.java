package ext.library.job;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 队列名重复必须启动失败：两个处理器共用一个队列时，后注册者会顶掉前者，负载被静默吞掉。
 */
class JobDispatcherTest {

    record Payload(String value) {
    }

    @Test
    void rejectsTwoHandlersOnSameQueue() {
        JobHandler<Payload> first = JobHandler.of("dup", Payload.class, _ -> {
        });
        JobHandler<Payload> second = JobHandler.of("dup", Payload.class, _ -> {
        });

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> new JobDispatcher(null, List.of(first, second)));
        assertTrue(error.getMessage().contains("dup"), "报错要带队列名：" + error.getMessage());
    }
}

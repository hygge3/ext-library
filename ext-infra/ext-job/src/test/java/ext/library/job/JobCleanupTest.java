package ext.library.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ext.library.job.config.JobAutoConfiguration.JobPollingTask;
import ext.library.job.config.JobProperties;
import ext.library.postgres.properties.PostgresProperties;
import ext.library.postgres.queue.Job;
import ext.library.postgres.queue.PostgresQueue;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 周期清理测试：已完成任务与死信各按自己的保留时长清理
 */
@DisplayName("任务清理测试")
class JobCleanupTest {

    record Payload() {
    }

    /** 记录清理调用的桩 */
    static class RecordingQueue extends PostgresQueue {

        final List<String> calls = new ArrayList<>();

        RecordingQueue() {
            super(null, new PostgresProperties());
        }

        @Override
        public List<Job> dequeue(String queue, int limit) {
            return List.of();
        }

        @Override
        public Optional<Job> dequeue(String queue) {
            return Optional.empty();
        }

        @Override
        public int cleanupCompleted(String queue, Duration olderThan) {
            calls.add("completed:" + queue + ":" + olderThan);
            return 1;
        }

        @Override
        public int cleanupFailed(String queue, Duration olderThan) {
            calls.add("failed:" + queue + ":" + olderThan);
            return 2;
        }

        @Override
        public int resetStuckJobs(String queue, Duration timeout) {
            return 0;
        }
    }

    private static JobPollingTask task(RecordingQueue queue, JobProperties properties) {
        List<JobHandler<?>> handlers = properties == null ? List.of() : List.of(
                JobHandler.of("share-mail", Payload.class, _ -> {
                }));
        JobProperties effective = properties == null ? new JobProperties() : properties;
        return new JobPollingTask(effective, null, new JobDispatcher(queue, handlers));
    }

    @Test
    void cleansCompletedAndFailedWithOwnRetention() {
        RecordingQueue queue = new RecordingQueue();
        JobProperties properties = new JobProperties();
        properties.setCompletedRetention(Duration.ofDays(7));
        properties.setFailedRetention(Duration.ofDays(30));

        task(queue, properties).cleanup();

        assertEquals(List.of("completed:share-mail:PT168H", "failed:share-mail:PT720H"), queue.calls);
    }

    @Test
    void skipsWhenNoHandlerRegistered() {
        RecordingQueue queue = new RecordingQueue();

        task(queue, null).cleanup();

        assertTrue(queue.calls.isEmpty(), "没有处理器时不该碰库");
    }
}

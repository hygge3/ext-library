package ext.library.tool.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Runtimes 工具类测试")
class RuntimesTest {

    @Test
    @DisplayName("测试获取当前进程 PID")
    void testGetPId() {
        int pid = Runtimes.getPId();

        assertTrue(pid > 0, "PID 应该大于 0");

        // 再次调用应返回相同的 PID（测试缓存机制）
        int pid2 = Runtimes.getPId();
        assertEquals(pid, pid2, "多次调用应返回相同的 PID");
    }

    @Test
    @DisplayName("测试获取应用启动时间")
    void testGetStartTime() {
        Instant startTime = Runtimes.getStartTime();

        assertNotNull(startTime, "启动时间不应为 null");
        assertTrue(startTime.isBefore(Instant.now()), "启动时间应早于当前时间");

        // 验证启动时间应该在合理的范围内（不会是未来时间）
        Instant now = Instant.now();
        assertTrue(Duration.between(startTime, now).toMillis() >= 0,
                "启动时间到现在的时间间隔应大于等于 0");
    }

    @Test
    @DisplayName("测试获取应用运行时间")
    void testGetUpTime() {
        Duration upTime = Runtimes.getUpTime();

        assertNotNull(upTime, "运行时间不应为 null");
        assertTrue(upTime.toMillis() > 0, "运行时间应大于 0");

        // 等待一小段时间后再次获取，应该增加
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Duration upTime2 = Runtimes.getUpTime();
        assertTrue(upTime2.toMillis() >= upTime.toMillis(),
                "第二次获取的运行时间应大于等于第一次");
    }

    @Test
    @DisplayName("测试获取 JVM 参数")
    void testGetJvmArguments() {
        String jvmArgs = Runtimes.getJvmArguments();

        assertNotNull(jvmArgs, "JVM 参数不应为 null");
        // JVM 参数可能为空字符串，但不应为 null
        assertNotNull(jvmArgs, "JVM 参数应该是有效的字符串");
    }

    @Test
    @DisplayName("测试获取 CPU 核数")
    void testGetCpuNum() {
        int cpuNum = Runtimes.getCpuNum();

        assertTrue(cpuNum > 0, "CPU 核数应大于 0");
        assertTrue(cpuNum <= 1024, "CPU 核数应在合理范围内（小于等于 1024）");

        // 验证与系统返回值一致
        int systemCpuNum = Runtime.getRuntime().availableProcessors();
        assertEquals(systemCpuNum, cpuNum, "应返回系统的 CPU 核数");
    }

    @Test
    @DisplayName("测试启动时间与运行时间的一致性")
    void testStartTimeAndUpTimeConsistency() {
        Instant startTime = Runtimes.getStartTime();
        Duration upTime = Runtimes.getUpTime();
        Instant now = Instant.now();

        // 计算的当前时间应该接近实际当前时间
        Instant calculatedNow = startTime.plus(upTime);

        // 允许一定的误差（比如 1 秒）
        long diffMillis = Math.abs(Duration.between(calculatedNow, now).toMillis());
        assertTrue(diffMillis < 1000,
                "通过启动时间加运行时间计算的当前时间应接近实际时间（误差小于 1 秒）");
    }

    @Test
    @DisplayName("测试 PID 的唯一性和稳定性")
    void testPIdStability() {
        int pid1 = Runtimes.getPId();
        int pid2 = Runtimes.getPId();
        int pid3 = Runtimes.getPId();

        assertEquals(pid1, pid2, "PID 应保持一致");
        assertEquals(pid2, pid3, "PID 应保持一致");
        assertTrue(pid1 > 0, "PID 应为正数");
    }

    @Test
    @DisplayName("测试 CPU 核数的稳定性")
    void testCpuNumStability() {
        int cpuNum1 = Runtimes.getCpuNum();
        int cpuNum2 = Runtimes.getCpuNum();
        int cpuNum3 = Runtimes.getCpuNum();

        assertEquals(cpuNum1, cpuNum2, "CPU 核数应保持一致");
        assertEquals(cpuNum2, cpuNum3, "CPU 核数应保持一致");
    }

    @Test
    @DisplayName("测试运行时间的递增性")
    void testUpTimeIncreasing() throws InterruptedException {
        Duration upTime1 = Runtimes.getUpTime();

        // 等待 50 毫秒
        Thread.sleep(50);

        Duration upTime2 = Runtimes.getUpTime();

        assertTrue(upTime2.toMillis() > upTime1.toMillis(),
                "运行时间应随时间递增");

        long diff = upTime2.toMillis() - upTime1.toMillis();
        assertTrue(diff >= 40 && diff <= 200,
                "时间差应在合理范围内（考虑到系统调度的误差）");
    }
}
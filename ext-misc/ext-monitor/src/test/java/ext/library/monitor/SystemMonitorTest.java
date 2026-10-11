package ext.library.monitor;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SystemMonitor 测试")
class SystemMonitorTest {

    private static SystemMonitor monitor;

    @BeforeAll
    static void setUp() {
        monitor = new SystemMonitor();
    }

    @Nested
    @DisplayName("HostInfo 测试")
    class HostInfoTests {

        @Test
        @DisplayName("获取主机信息")
        void getHostInfo() {
            HostInfo info = monitor.getHostInfo();

            assertNotNull(info);
            assertNotNull(info.hostName());
            assertNotNull(info.hostAddress());
            assertNotNull(info.osName());
            assertNotNull(info.osArch());
            assertNotNull(info.userDir());
        }

        @Test
        @DisplayName("静态便捷方法")
        void staticGet() {
            HostInfo info = HostInfo.get();

            assertNotNull(info);
            assertNotNull(info.hostName());
        }
    }

    @Nested
    @DisplayName("CpuInfo 测试")
    class CpuInfoTests {

        @Test
        @DisplayName("获取 CPU 信息")
        void getCpuInfo() {
            // 使用较短的采样时间以加快测试
            CpuInfo info = monitor.getCpuInfo(100);

            assertNotNull(info);
            assertTrue(info.physicalProcessorCount() > 0);
            assertTrue(info.logicalProcessorCount() > 0);
            assertTrue(info.logicalProcessorCount() >= info.physicalProcessorCount());
            assertTrue(info.usePercent() >= 0 && info.usePercent() <= 1);
            assertTrue(info.systemPercent() >= 0 && info.systemPercent() <= 1);
            assertTrue(info.userPercent() >= 0 && info.userPercent() <= 1);
        }

        @Test
        @DisplayName("静态便捷方法")
        void staticGet() {
            CpuInfo info = CpuInfo.get(100);

            assertNotNull(info);
            assertTrue(info.physicalProcessorCount() > 0);
        }
    }

    @Nested
    @DisplayName("MemoryInfo 测试")
    class MemoryInfoTests {

        @Test
        @DisplayName("获取内存信息")
        void getMemoryInfo() {
            MemoryInfo info = monitor.getMemoryInfo();

            assertNotNull(info);
            assertNotNull(info.total());
            assertNotNull(info.used());
            assertNotNull(info.free());
            assertTrue(info.usePercent() >= 0 && info.usePercent() <= 1);
        }

        @Test
        @DisplayName("内存格式化正确")
        void memoryFormatted() {
            MemoryInfo info = MemoryInfo.get();

            // 验证格式化字符串包含单位
            assertTrue(info.total().matches(".*[KMGT]B$"));
            assertTrue(info.used().matches(".*[KMGT]B$"));
            assertTrue(info.free().matches(".*[KMGT]B$"));
        }
    }

    @Nested
    @DisplayName("JvmInfo 测试")
    class JvmInfoTests {

        @Test
        @DisplayName("获取 JVM 信息")
        void getJvmInfo() {
            JvmInfo info = monitor.getJvmInfo();

            assertNotNull(info);
            assertNotNull(info.jdkVersion());
            assertNotNull(info.jdkHome());
            assertNotNull(info.jdkName());
            assertNotNull(info.jvmTotalMemory());
            assertNotNull(info.maxMemory());
            assertNotNull(info.freeMemory());
            assertNotNull(info.usedMemory());
            assertTrue(info.usePercent() >= 0 && info.usePercent() <= 1);
            assertTrue(info.startTime() > 0);
            assertTrue(info.uptime() > 0);
        }

        @Test
        @DisplayName("静态便捷方法")
        void staticGet() {
            JvmInfo info = JvmInfo.get();

            assertNotNull(info);
            assertNotNull(info.jdkVersion());
        }
    }

    @Nested
    @DisplayName("DiskInfo 测试")
    class DiskInfoTests {

        @Test
        @DisplayName("获取磁盘信息")
        void getDiskInfos() {
            List<DiskInfo> infos = monitor.getDiskInfos();

            assertNotNull(infos);
            // 至少有一个磁盘分区
            assertFalse(infos.isEmpty());

            for (DiskInfo info : infos) {
                assertNotNull(info.name());
                assertNotNull(info.mount());
                assertNotNull(info.type());
                assertTrue(info.totalSpace() >= 0);
                assertTrue(info.usableSpace() >= 0);
                assertTrue(info.usableSpace() <= info.totalSpace());
                assertTrue(info.usePercent() >= 0 && info.usePercent() <= 1);
            }
        }

        @Test
        @DisplayName("磁盘便捷方法")
        void diskConvenienceMethods() {
            List<DiskInfo> infos = DiskInfo.getAll();

            assertFalse(infos.isEmpty());

            DiskInfo info = infos.getFirst();
            assertEquals(info.totalSpace() - info.usableSpace(), info.usedSpace());

            // 验证格式化方法
            if (info.totalSpace() > 0) {
                assertNotNull(info.totalSpaceFormatted());
                assertTrue(info.totalSpaceFormatted().matches(".*[KMGT]B$"));
            }
        }
    }

    @Nested
    @DisplayName("NetIoInfo 测试")
    class NetIoInfoTests {

        @Test
        @DisplayName("获取网络 IO 信息")
        void getNetIoInfo() {
            // 使用较短的采样时间
            NetIoInfo info = monitor.getNetIoInfo(200);

            assertNotNull(info);
            assertTrue(info.receivePacketsPerSecond() >= 0);
            assertTrue(info.transmitPacketsPerSecond() >= 0);
            assertTrue(info.receiveBytesPerSecond() >= 0);
            assertTrue(info.transmitBytesPerSecond() >= 0);
        }

        @Test
        @DisplayName("网络 IO 便捷方法")
        void netIoConvenienceMethods() {
            NetIoInfo info = NetIoInfo.get(200);

            assertNotNull(info);
            assertEquals(info.receiveBytesPerSecond() / 1024.0, info.receiveKBPerSecond());
            assertEquals(info.transmitBytesPerSecond() / 1024.0, info.transmitKBPerSecond());
        }
    }

    @Nested
    @DisplayName("FormatUtil 测试")
    class FormatUtilTests {

        @Test
        @DisplayName("字节格式化")
        void formatBytes() {
            assertEquals("1KB", FormatUtil.formatBytes(1024));
            assertEquals("1MB", FormatUtil.formatBytes(1024 * 1024));
            assertEquals("1GB", FormatUtil.formatBytes(1024L * 1024 * 1024));
            assertEquals("1TB", FormatUtil.formatBytes(1024L * 1024 * 1024 * 1024));
        }

        @Test
        @DisplayName("小数舍入")
        void round() {
            assertEquals(0.12, FormatUtil.round(0.124));
            assertEquals(0.13, FormatUtil.round(0.125));
            assertEquals(0.13, FormatUtil.round(0.126));
        }
    }
}

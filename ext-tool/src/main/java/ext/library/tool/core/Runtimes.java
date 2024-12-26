package ext.library.tool.core;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.google.common.base.Joiner;
import ext.library.tool.$;
import ext.library.tool.constant.Symbol;
import lombok.experimental.UtilityClass;

/**
 * 运行时工具类
 */
@UtilityClass
public class Runtimes {

    private volatile int pId = -1;

    private final int CPU_NUM = Runtime.getRuntime().availableProcessors();

    /**
     * 获得当前进程的 PID
     * <p>
     * 当失败时返回 -1
     *
     * @return pid
     */
    public int getPId() {
        if (pId > 0) {
            return pId;
        }
        // something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');
        if (index > 0) {
            pId = $.toInt(jvmName.substring(0, index), -1);
            return pId;
        }
        return pId;
    }

    /**
     * 返回应用启动的时间
     *
     * @return {Instant}
     */
    public Instant getStartTime() {
        return Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime());
    }

    /**
     * 返回应用启动到现在的时间
     *
     * @return {Duration}
     */
    public Duration getUpTime() {
        return Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime());
    }

    /**
     * 返回输入的 JVM 参数列表
     *
     * @return jvm 参数
     */
    public String getJvmArguments() {
        List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        return Joiner.on(Symbol.C_SPACE).skipNulls().join(vmArguments);
    }

    /**
     * 获取 CPU 核数
     *
     * @return cpu count
     */
    public int getCpuNum() {
        return CPU_NUM;
    }

}

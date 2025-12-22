package ext.library.tool.core;

import ext.library.tool.constant.Holder;
import ext.library.tool.util.StringUtil;
import ext.library.tool.util.TypeCastUtil;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * 运行时工具类
 */

public final class Runtimes {

    /**
     * 获得当前进程的 PID
     * <p>
     * 当失败时返回 -1
     *
     * @return pid
     */
    public static int getPId() {
        // something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');
        if (index > 0) {
            return TypeCastUtil.getAsInteger(jvmName.substring(0, index), -1);
        }
        return -1;
    }

    /**
     * 返回应用启动的时间
     *
     * @return {Instant}
     */
    public static Instant getStartTime() {
        return Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime());
    }

    /**
     * 返回应用启动到现在的时间
     *
     * @return {Duration}
     */
    public static Duration getUpTime() {
        return Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime());
    }

    /**
     * 返回输入的 JVM 参数列表
     *
     * @return jvm 参数
     */
    public static String getJvmArguments() {
        List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        return StringUtil.join(vmArguments, " ");
    }

    /**
     * 获取 CPU 核数
     *
     * @return cpu count
     */
    public static int getCpuNum() {
        return Holder.CPU_CORE_NUM;
    }

}

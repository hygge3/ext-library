package ext.library.monitor;

import lombok.Getter;
import lombok.Setter;

/**
 * JVM 信息
 */
@Getter
@Setter
public class JvmInfo {

	/**
	 * jdk 版本
	 */
	String jdkVersion;

	/**
	 * jdk Home
	 */
	String jdkHome;

	/**
	 * jak name
	 */
	private String jdkName;

	/**
	 * 总内存
	 */
	String jvmTotalMemory;

	/**
	 * Java 虚拟机将尝试使用的最大内存量
	 */
	String maxMemory;

	/**
	 * 空闲内存
	 */
	String freeMemory;

	/**
	 * 已使用内存
	 */
	String usedMemory;

	/**
	 * 内存使用率
	 */
	private double usePercent;

	/**
	 * 返回 Java 虚拟机的启动时间（毫秒）。此方法返回 Java 虚拟机启动的大致时间。
	 */
	private long startTime;

	/**
	 * 返回 Java 虚拟机的正常运行时间（毫秒）
	 */
	private long uptime;

}

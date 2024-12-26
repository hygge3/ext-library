package ext.library.monitor;

import lombok.Getter;
import lombok.Setter;

/**
 * CPU 信息
 */
@Getter
@Setter
public class CpuInfo {

	/**
	 * 物理处理器数量
	 */
	private int physicalProcessorCount;

	/**
	 * 逻辑处理器数量
	 */
	private int logicalProcessorCount;

	/**
	 * 系统使用率
	 */
	private double systemPercent;

	/**
	 * 用户使用率
	 */
	private double userPercent;

	/**
	 * 当前等待率
	 */
	private double waitPercent;

	/**
	 * 当前使用率
	 */
	private double usePercent;

}

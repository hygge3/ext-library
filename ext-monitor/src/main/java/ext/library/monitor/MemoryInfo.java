package ext.library.monitor;

import lombok.Getter;
import lombok.Setter;

/**
 * 系统内存信息
 */
@Getter
@Setter
public class MemoryInfo {

	/**
	 * 总计
	 */
	private String total;

	/**
	 * 已使用
	 */
	private String used;

	/**
	 * 未使用
	 */
	private String free;

	/**
	 * 使用率
	 */
	private double usePercent;

}

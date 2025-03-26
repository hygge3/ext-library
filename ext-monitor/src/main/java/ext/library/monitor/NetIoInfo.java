package ext.library.monitor;

import lombok.Getter;
import lombok.Setter;

/**
 * 网络带宽信息
 */
@Getter
@Setter
public class NetIoInfo {

	/**
	 * 每秒钟接收的数据包，rxpck/s
	 */
	 String rxpck;

	/**
	 * 每秒钟发送的数据包，txpck/s
	 */
	 String txpck;

	/**
	 * 每秒钟接收的 KB 数，rxkB/s
	 */
	 String rxbyt;

	/**
	 * 每秒钟发送的 KB 数，txkB/s
	 */
	 String txbyt;

}

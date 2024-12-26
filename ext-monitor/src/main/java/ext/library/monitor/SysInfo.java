package ext.library.monitor;

import lombok.Getter;
import lombok.Setter;

/**
 * 操作系统信息
 */
@Getter
@Setter
public class SysInfo {

	/**
	 * 系统名称
	 */
	private String name;

	/**
	 * 系统 ip
	 */
	private String ip;

	/**
	 * 操作系统
	 */
	private String osName;

	/**
	 * 系统架构
	 */
	private String osArch;

	/**
	 * 项目路径
	 */
	private String userDir;

}

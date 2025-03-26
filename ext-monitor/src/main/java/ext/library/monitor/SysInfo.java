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
	 String name;

	/**
	 * 系统 ip
	 */
	 String ip;

	/**
	 * 操作系统
	 */
	 String osName;

	/**
	 * 系统架构
	 */
	 String osArch;

	/**
	 * 项目路径
	 */
	 String userDir;

}

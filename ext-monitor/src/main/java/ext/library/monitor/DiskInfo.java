package ext.library.monitor;

import lombok.Getter;
import lombok.Setter;

/**
 * 磁盘信息
 */
@Getter
@Setter
public class DiskInfo {

	/**
	 * 名称
	 */
	 String name;

	/**
	 * 文件系统的卷名
	 */
	 String volume;

	/**
	 * 标签
	 */
	 String label;

	/**
	 * 文件系统的逻辑卷名
	 */
	 String logicalVolume;

	/**
	 * 文件系统的挂载点
	 */
	 String mount;

	/**
	 * 文件系统的描述
	 */
	 String description;

	/**
	 * 文件系统的选项
	 */
	 String options;

	/**
	 * 文件系统的类型（FAT、NTFS、etx2、ext4 等）
	 */
	 String type;

	/**
	 * UUID/GUID
	 */
	 String UUID;

	/**
	 * 分区大小
	 */
	 String size;

	 Long totalSpace;

	/**
	 * 已使用
	 */
	 String used;

	 Long usableSpace;

	/**
	 * 可用
	 */
	 String avail;

	/**
	 * 已使用百分比
	 */
	 double usePercent;

}

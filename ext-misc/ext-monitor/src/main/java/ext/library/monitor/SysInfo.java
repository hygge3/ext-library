package ext.library.monitor;

/**
 * 操作系统信息
 */
public record SysInfo(
        // 系统名称
        String name,
        // 系统 ip
        String ip,
        // 操作系统
        String osName,
        // 系统架构
        String osArch,
        // 项目路径
        String userDir
) {}
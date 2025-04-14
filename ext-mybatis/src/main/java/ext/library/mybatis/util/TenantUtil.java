package ext.library.mybatis.util;

import ext.library.tool.core.Threads;
import java.io.Serializable;
import lombok.experimental.UtilityClass;

/**
 * 租户 ID 存取类
 */
@UtilityClass
public class TenantUtil {

    String TENANT_KEY = "TenantId";

    /**
     * 设置租户 ID
     * <p>
     * 本方法用于将给定的租户 ID 存储到线程本地变量中，以便在后续的处理中能够根据需要获取该租户 ID
     * 主要应用于多租户环境下，确保每个线程都能够独立地持有和操作指定租户的相关信息
     *
     * @param tenantId 租户 ID，作为一个可序列化的对象传递，确保其可以在分布式环境中被正确地传输和恢复
     */
    public void set(Serializable tenantId) {
        Threads.put(TENANT_KEY, tenantId);
    }

    /**
     * 从线程本地变量中获取租户标识
     * <p>
     * 此方法使用线程本地变量来存储和获取租户标识，以确保在多线程环境下每个线程都能独立地获取其对应的租户信息
     * 这对于在分布式系统或多租户应用中实现线程安全的租户隔离非常关键
     *
     * @return Serializable 类型的租户标识，该标识是线程本地变量中存储的租户信息
     */
    public Serializable get() {
        return Threads.get(TENANT_KEY);
    }

    /**
     * 清除当前线程的租户信息
     * <p>
     * 本方法通过移除线程局部变量中的租户键值，来实现清除当前线程租户信息的目的
     * 主要用于在业务逻辑完成后，清理线程环境，避免信息泄露
     */
    public void clear() {
        Threads.remove(TENANT_KEY);
    }
}

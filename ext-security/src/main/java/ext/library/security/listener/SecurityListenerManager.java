package ext.library.security.listener;

import ext.library.tool.core.Exceptions;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 监听器管理
 * </p>
 */
public class SecurityListenerManager {

    private static final List<SecurityListener> LISTENER_LIST = new ArrayList<>();

    static {
        SecurityListenerManager.LISTENER_LIST.add(new SecurityListener() {
        });
    }

    /**
     * 获取所有监听器
     *
     * @return List<SecurityListener>
     */
    public static List<SecurityListener> getListener() {
        return SecurityListenerManager.LISTENER_LIST;
    }

    /**
     * 注册监听器
     *
     * @param listener {@link SecurityListener}
     */
    public static void registerListener(SecurityListener listener) {
        if (null == listener) {
            throw Exceptions.throwOut("[🛡️] 注册监听器不能为空");
        }
        LISTENER_LIST.add(listener);
    }

    /**
     * 移除监听器
     *
     * @param listener {@link SecurityListener}
     */
    public static void removeListener(SecurityListener listener) {
        if (null == listener) {
            throw Exceptions.throwOut("[🛡️] 移除监听器不能为空");
        }
        LISTENER_LIST.remove(listener);
    }

    /**
     * 清空所有已注册的监听器
     */
    public static void clearListener() {
        LISTENER_LIST.clear();
    }

}
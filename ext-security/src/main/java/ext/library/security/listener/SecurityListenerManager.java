package ext.library.security.listener;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>
 * 监听器管理
 * </p>
 */
public class SecurityListenerManager {

    private static final List<SecurityListener> listenerList = new CopyOnWriteArrayList<>();

    static {
        SecurityListenerManager.listenerList.add(new SecurityListener() {
        });
    }

    /**
     * 获取所有监听器
     *
     * @return List<SecurityListener>
     */
    public static List<SecurityListener> getListener() {
        return SecurityListenerManager.listenerList;
    }

    /**
     * 注册监听器
     *
     * @param listener {@link SecurityListener}
     */
    public static void registerListener(SecurityListener listener) {
        if (null == listener) {
            throw new ExtException(EmojiSymbol.SECURITY, "要注册的监听器不能为 null");
        }
        listenerList.add(listener);
    }

    /**
     * 移除监听器
     *
     * @param listener {@link SecurityListener}
     */
    public static void removeListener(SecurityListener listener) {
        if (null == listener) {
            throw new ExtException(EmojiSymbol.SECURITY, "要移除的监听器不能为 null");
        }
        listenerList.remove(listener);
    }

    /**
     * 清空所有已注册的监听器
     */
    public static void clearListener() {
        listenerList.clear();
    }

}

package ext.library.i18n;

import java.util.Locale;
import java.util.Map;

/**
 * 翻译来源
 * <p>
 * 文件源与数据库源各实现一份，也可以自行接入远程配置中心。
 * 多个来源按注册顺序串联，前一个查不到才落到后一个。
 *
 * @since 4.1.0
 */
public interface TranslationSource {

    /**
     * 加载指定语言的全部翻译
     *
     * @param locale 语言
     *
     * @return 消息键 → 翻译文本，无数据时返回空 Map
     */
    Map<String, String> load(Locale locale);

    /**
     * 来源名称，用于日志
     *
     * @return 来源名称
     */
    String name();

    /**
     * 来源标识，决定多个来源之间的优先级
     *
     * @return 数值越小越先查询
     */
    default int order() {
        return 0;
    }

    /**
     * 清除已缓存的翻译，供热更新调用；无缓存实现的来源无需覆写
     */
    default void clear() {
    }

}

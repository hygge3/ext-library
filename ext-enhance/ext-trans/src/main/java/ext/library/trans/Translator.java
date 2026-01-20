package ext.library.trans;

/**
 * 翻译器接口
 * <p>
 * 实现类需标注 {@link TranslationType} 注解以声明翻译类型。
 * <p>
 * 使用示例：
 * <pre>{@code
 * @Component
 * @TranslationType("user")
 * public class UserTranslator implements Translator<String> {
 *     @Autowired
 *     private UserService userService;
 *
 *     @Override
 *     public String translate(Object key, String param) {
 *         if (key == null) return null;
 *         User user = userService.getById(key);
 *         return user != null ? user.getNickname() : null;
 *     }
 * }
 * }</pre>
 *
 * @param <T> 翻译结果类型
 * @see TranslationType
 * @see Translate
 */
public interface Translator<T> {

    /**
     * 执行翻译
     *
     * @param key   待翻译的键（非空）
     * @param param 附加参数，由 {@link Translate#param()} 传入
     * @return 翻译后的值
     */
    T translate(Object key, String param);

}

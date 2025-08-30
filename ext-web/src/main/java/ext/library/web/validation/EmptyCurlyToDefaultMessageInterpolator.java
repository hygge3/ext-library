package ext.library.web.validation;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

import jakarta.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * 将消息中空的花括号替换为校验注解的默认值
 * <p>
 * 扩展自原有的 {@link ResourceBundleMessageInterpolator} 消息处理器
 */
public class EmptyCurlyToDefaultMessageInterpolator extends ResourceBundleMessageInterpolator {

    private static final String EMPTY_CURLY_BRACES = "{}";

    public EmptyCurlyToDefaultMessageInterpolator() {
    }

    public EmptyCurlyToDefaultMessageInterpolator(ResourceBundleLocator userResourceBundleLocator) {
        super(userResourceBundleLocator);
    }

    @Override
    public String interpolate(@Nonnull String message, Context context, Locale locale) {

        // 如果包含花括号占位符
        if (message.contains(EMPTY_CURLY_BRACES)) {
            // 获取注解类型
            Class<? extends Annotation> annotationType = context.getConstraintDescriptor()
                    .getAnnotation()
                    .annotationType();

            Method messageMethod;
            try {
                messageMethod = annotationType.getDeclaredMethod("message");
            } catch (NoSuchMethodException e) {
                return super.interpolate(message, context, locale);
            }

            // 找到对应 message 的默认值，将 {} 替换为默认值
            if (messageMethod.getDefaultValue() != null) {
                Object defaultValue = messageMethod.getDefaultValue();
                if (defaultValue instanceof String defaultMessage) {
                    message = message.replace(EMPTY_CURLY_BRACES, defaultMessage);
                }
            }
        }

        return super.interpolate(message, context, locale);
    }

}
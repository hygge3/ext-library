package ext.library.core.util;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.function.Function;

import jakarta.validation.constraints.NotNull;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/**
 * 反射工具类
 */
@UtilityClass
public class ReflectUtil {
    /**
     * <h3>获取 {@code Lambda} 的 {@code Function} 表达式的函数名</h3>
     *
     * @param lambda 表达式
     * @return 函数名
     */
    @SneakyThrows
    public static @NotNull String getLambdaFunctionName(@NotNull Function<?, ?> lambda) {
        Method replaceMethod = lambda.getClass().getDeclaredMethod("writeReplace");
        replaceMethod.setAccessible(true);
        SerializedLambda serializedLambda = (SerializedLambda) replaceMethod.invoke(lambda);
        return serializedLambda.getImplMethodName()
                .replace("get", "");
    }
}

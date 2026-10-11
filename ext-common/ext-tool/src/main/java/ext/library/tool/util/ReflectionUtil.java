package ext.library.tool.util;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;


/**
 * 反射工具类
 */
public final class ReflectionUtil {

    private ReflectionUtil() {
    }

    // ---------------------- Info ----------------------

    /**
     * 获取类的包名
     *
     * @param clazz 目标类
     *
     * @return 包名
     */
    public static String getPackageName(Class<?> clazz) {
        return getPackageName(clazz.getName());
    }

    /**
     * 获取类全限定名对应的包名
     *
     * @param classFullName 类全限定名
     *
     * @return 包名
     */
    public static String getPackageName(String classFullName) {
        int lastDot = classFullName.lastIndexOf('.');
        return (lastDot < 0) ? "" : classFullName.substring(0, lastDot);
    }

    // ---------------------- Method ----------------------

    /**
     * 根据方法名和参数类型查找方法
     *
     * @param clazz      目标类
     * @param name       方法名
     * @param paramTypes 参数类型列表
     *
     * @return Method 对象，未找到时返回 {@code null}
     */
    public static @Nullable Method findMethod(Class<?> clazz, String name, Class<?> @Nullable ... paramTypes) {
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = (searchType.isInterface() ? searchType.getMethods() : getDeclaredMethods(searchType, false));
            for (Method method : methods) {
                if (name.equals(method.getName()) && (paramTypes == null || hasSameParams(method, paramTypes))) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    /**
     * 检查方法的参数类型是否匹配
     */
    private static boolean hasSameParams(Method method, Class<?>[] paramTypes) {
        return (paramTypes.length == method.getParameterCount() && Arrays.equals(paramTypes, method.getParameterTypes()));
    }

    /**
     * 获取类声明的所有方法（包括接口默认方法）
     */
    private static Method[] getDeclaredMethods(Class<?> clazz, boolean defensive) {
        Method[] result;
        try {
            Method[] declaredMethods = clazz.getDeclaredMethods();
            List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
            if (defaultMethods != null) {
                result = new Method[declaredMethods.length + defaultMethods.size()];
                System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
                int index = declaredMethods.length;
                for (Method defaultMethod : defaultMethods) {
                    result[index] = defaultMethod;
                    index++;
                }
            } else {
                result = declaredMethods;
            }
        } catch (Throwable ex) {
            throw new ToolException(EmojiSymbol.TOOL, ex, "无法从类加载器 [{}] 检查 Class [{}]", clazz.getName(), clazz.getClassLoader());
        }
        return (result.length == 0 || !defensive) ? result : result.clone();
    }

    /**
     * 查找接口中的具体（default）方法
     */
    private static @Nullable List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
        List<Method> result = null;
        for (Class<?> ifc : clazz.getInterfaces()) {
            for (Method ifcMethod : ifc.getMethods()) {
                if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                    result.add(ifcMethod);
                }
            }
        }
        return result;
    }

    /**
     * 设置方法为可访问
     *
     * @param method 目标方法
     */
    public static void makeAccessible(Method method) {
        if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
            method.setAccessible(true);
        }
    }

    /**
     * 调用方法
     *
     * @param method 目标方法
     * @param target 目标对象
     * @param args   方法参数
     *
     * @return 方法调用结果
     */
    public static Object invokeMethod(Method method, Object target, Object @Nullable ... args) {
        try {
            return args != null ? method.invoke(target, args) : method.invoke(target);
        } catch (Exception ex) {
            throw new ToolException(EmojiSymbol.TOOL, ex, "无法调用方法 [{}]", method);
        }
    }


    // ---------------------- Field ----------------------

    /**
     * 根据字段名查找字段
     *
     * @param clazz 目标类
     * @param name  字段名
     *
     * @return Field 对象，未找到时返回 {@code null}
     */
    public static @Nullable Field findField(Class<?> clazz, String name) {
        return findField(clazz, name, null);
    }

    /**
     * 根据字段名和类型查找字段
     *
     * @param clazz 目标类
     * @param name  字段名
     * @param type  字段类型
     *
     * @return Field 对象，未找到时返回 {@code null}
     */
    public static @Nullable Field findField(Class<?> clazz, @Nullable String name, @Nullable Class<?> type) {
        Assert.isTrue(name != null || type != null, "必须指定字段的名称或类型");
        Class<?> searchType = clazz;
        while (Object.class != searchType && searchType != null) {
            Field[] fields = getDeclaredFields(searchType);
            for (Field field : fields) {
                if ((name == null || name.equals(field.getName())) && (type == null || type.equals(field.getType()))) {
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    /**
     * 获取类声明的所有字段
     *
     * @param clazz 目标类
     *
     * @return 字段数组
     */
    private static Field[] getDeclaredFields(Class<?> clazz) {
        Field[] result;
        try {
            result = clazz.getDeclaredFields();
        } catch (Throwable ex) {
            throw new ToolException(EmojiSymbol.TOOL, ex, "无法从类加载器 [{}] 检查 Class [{}]", clazz.getName(), clazz.getClassLoader());
        }
        return result;
    }

    /**
     * 设置字段为可访问
     *
     * @param field 目标字段
     */
    public static void makeAccessible(Field field) {
        if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers()) || Modifier.isFinal(field.getModifiers())) {
            field.setAccessible(true);
        }
    }

    /**
     * 设置字段值
     *
     * @param field  目标字段
     * @param target 目标对象
     * @param value  要设置的值
     */
    public static void setField(Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException ex) {
            throw new ToolException(EmojiSymbol.TOOL, ex, "无法将值设置为字段 [{}]", field);
        }
    }

    /**
     * 获取字段值
     *
     * @param field  目标字段
     * @param target 目标对象
     *
     * @return 字段值
     */
    public static Object getField(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException ex) {
            throw new ToolException(EmojiSymbol.TOOL, ex, "无法从字段获取值 [{}]", field);
        }
    }

    /**
     * 获取 Lambda 的 Function 表达式的方法名
     *
     * @param lambda 表达式
     *
     * @return 方法名
     */
    public static String getLambdaMethodName(Function<?, ?> lambda) {
        try {
            Method replaceMethod = lambda.getClass().getDeclaredMethod("writeReplace");
            replaceMethod.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) replaceMethod.invoke(lambda);
            return serializedLambda.getImplMethodName();
        } catch (ReflectiveOperationException ex) {
            throw new ToolException(EmojiSymbol.TOOL, ex, "无法解析 Lambda 表达式");
        }
    }

    /**
     * 检查字段是否为 public static final
     *
     * @param field 目标字段
     *
     * @return 如果字段为 public static final 则返回 true
     */
    public static boolean isPublicStaticFinal(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
    }

    /**
     * 遍历类的所有字段并执行回调（包括父类，不含 Object）
     *
     * @param clazz 目标类
     * @param fc    字段回调
     */
    public static void doWithFields(Class<?> clazz, FieldCallback fc) {
        Class<?> targetClass = clazz;
        do {
            Field[] fields = getDeclaredFields(targetClass);
            for (Field field : fields) {
                try {
                    fc.doWith(field);
                } catch (IllegalAccessException ex) {
                    throw new ToolException(EmojiSymbol.TOOL, ex, "不允许访问字段 '{}'", field.getName());
                }
            }
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null && targetClass != Object.class);
    }

    /**
     * 创建指定接口的代理实例
     *
     * @param interfaceType 接口类型
     * @param handler       调用处理器
     * @param <T>           代理类型
     *
     * @return 代理实例
     */
    public static <T> T newProxy(Class<T> interfaceType, InvocationHandler handler) {
        Assert.isTrue(interfaceType.isInterface(), interfaceType + " 不是接口");
        Object object = Proxy.newProxyInstance(interfaceType.getClassLoader(), new Class<?>[]{interfaceType}, handler);
        return interfaceType.cast(object);
    }

    /**
     * 字段回调接口，用于遍历字段时执行操作
     */
    public interface FieldCallback {

        /**
         * 对给定字段执行操作
         *
         * @param field 目标字段
         */
        void doWith(Field field) throws IllegalArgumentException, IllegalAccessException;
    }

}

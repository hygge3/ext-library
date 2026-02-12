package ext.library.tool.util;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;
import org.jspecify.annotations.Nullable;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 类工具类
 * <p>
 * 提供类型判断、反射操作、注解获取、对象实例化等功能。
 *
 * @since 2025.01.01
 */
public final class ClassUtil {

    /** 包装类型 → 基本类型映射 */
    private static final Map<Class<?>, Class<?>> wrapperToPrimitive = new IdentityHashMap<>(9);

    /** 基本类型 → 包装类型映射 */
    private static final Map<Class<?>, Class<?>> primitiveToWrapper = new IdentityHashMap<>(9);

    static {
        wrapperToPrimitive.put(Boolean.class, boolean.class);
        wrapperToPrimitive.put(Byte.class, byte.class);
        wrapperToPrimitive.put(Character.class, char.class);
        wrapperToPrimitive.put(Double.class, double.class);
        wrapperToPrimitive.put(Float.class, float.class);
        wrapperToPrimitive.put(Integer.class, int.class);
        wrapperToPrimitive.put(Long.class, long.class);
        wrapperToPrimitive.put(Short.class, short.class);
        wrapperToPrimitive.put(Void.class, void.class);

        for (Map.Entry<Class<?>, Class<?>> entry : wrapperToPrimitive.entrySet()) {
            primitiveToWrapper.put(entry.getValue(), entry.getKey());
        }
    }

    private ClassUtil() {
    }

    // region 类型判断

    /**
     * 判断子类型是否可分配给父类型
     * <p>
     * 考虑基本类型和包装类型的兼容性。
     *
     * @param superType 父类型
     * @param subType   子类型
     *
     * @return 如果子类型可分配给父类型返回 true
     */
    public static boolean isAssignable(Class<?> superType, Class<?> subType) {
        if (superType.isAssignableFrom(subType)) {
            return true;
        }
        if (superType.isPrimitive()) {
            Class<?> resolvedPrimitive = wrapperToPrimitive.get(subType);
            return superType == resolvedPrimitive;
        } else {
            Class<?> resolvedWrapper = primitiveToWrapper.get(subType);
            return resolvedWrapper != null && superType.isAssignableFrom(resolvedWrapper);
        }
    }

    /**
     * 判断子类型是否可分配给父类型（支持泛型类型）
     *
     * @param superType 父类型
     * @param subType   子类型
     *
     * @return 如果子类型可分配给父类型返回 true
     */
    public static boolean isAssignable(Type superType, Type subType) {
        // 所有类型都可以分配给自身和 Object
        if (superType.equals(subType) || Object.class == superType) {
            return true;
        }

        if (superType instanceof Class<?> superClass) {
            return switch (subType) {
                case Class<?> subClass -> isAssignable(superClass, subClass);
                case ParameterizedType paramType -> {
                    Type rawType = paramType.getRawType();
                    yield rawType instanceof Class<?> rawClass && isAssignable(superClass, rawClass);
                }
                case GenericArrayType arrayType when superClass.isArray() ->
                        isAssignable(superClass.getComponentType(), arrayType.getGenericComponentType());
                default -> false;
            };
        }

        if (superType instanceof ParameterizedType superParamType) {
            Type superRaw = superParamType.getRawType();
            if (superRaw instanceof Class<?> superRawClass) {
                if (subType instanceof Class<?> subClass) {
                    return isAssignable(superRawClass, subClass);
                }
                if (subType instanceof ParameterizedType subParamType) {
                    Type subRaw = subParamType.getRawType();
                    return subRaw instanceof Class<?> subRawClass && isAssignable(superRawClass, subRawClass);
                }
            }
        }

        if (superType instanceof GenericArrayType superArrayType) {
            Type superComponent = superArrayType.getGenericComponentType();
            if (subType instanceof Class<?> subClass && subClass.isArray()) {
                return isAssignable(superComponent, subClass.getComponentType());
            }
            if (subType instanceof GenericArrayType subArrayType) {
                return isAssignable(superComponent, subArrayType.getGenericComponentType());
            }
        }

        if (superType instanceof WildcardType wildcardType) {
            for (Type upperBound : wildcardType.getUpperBounds()) {
                if (!isAssignable(upperBound, subType)) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    // endregion

    // region 方法获取

    /**
     * 获取公共方法
     * <p>
     * 如果未指定参数类型，则仅在方法名唯一时返回。
     *
     * @param clazz      类
     * @param methodName 方法名
     * @param paramTypes 参数类型（可为 null 表示任意签名）
     *
     * @return 方法，未找到返回 null
     */
    public static @Nullable Method getMethod(Class<?> clazz, String methodName, Class<?> @Nullable ... paramTypes) {
        if (paramTypes != null) {
            return getMethodOrNull(clazz, methodName, paramTypes);
        } else {
            Set<Method> candidates = findMethodCandidatesByName(clazz, methodName);
            if (candidates.size() == 1) {
                return candidates.iterator().next();
            }
            return null;
        }
    }

    private static @Nullable Method getMethodOrNull(Class<?> clazz, String methodName, Class<?>[] paramTypes) {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    private static Set<Method> findMethodCandidatesByName(Class<?> clazz, String methodName) {
        Set<Method> candidates = new HashSet<>(1);
        for (Method method : clazz.getMethods()) {
            if (methodName.equals(method.getName())) {
                candidates.add(method);
            }
        }
        return candidates;
    }

    /**
     * 获取构造函数参数
     *
     * @param constructor    构造函数
     * @param parameterIndex 参数索引
     *
     * @return 参数对象
     */
    public static Parameter getParameter(Constructor<?> constructor, int parameterIndex) {
        return constructor.getParameters()[parameterIndex];
    }

    /**
     * 获取方法参数
     *
     * @param method         方法
     * @param parameterIndex 参数索引
     *
     * @return 参数对象
     */
    public static Parameter getMethodParameter(Method method, int parameterIndex) {
        return method.getParameters()[parameterIndex];
    }

    // endregion

    // region 注解获取

    /**
     * 获取元素上的注解
     *
     * @param element        注解元素
     * @param annotationType 注解类型
     * @param <A>            注解类型
     *
     * @return 注解实例，未找到返回 null
     */
    public static <A extends Annotation> A getAnnotation(AnnotatedElement element, Class<A> annotationType) {
        return element.getDeclaredAnnotation(annotationType);
    }

    /**
     * 获取方法或其声明类上的注解
     * <p>
     * 优先查找方法上的注解，未找到则查找声明类上的注解。
     *
     * @param method         方法
     * @param annotationType 注解类型
     * @param <A>            注解类型
     *
     * @return 注解实例，未找到返回 null
     */
    public static <A extends Annotation> A getAnnotation(Method method, Class<A> annotationType) {
        A annotation = method.getAnnotation(annotationType);
        if (annotation != null) {
            return annotation;
        }
        return method.getDeclaringClass().getAnnotation(annotationType);
    }

    // endregion

    // region 实例化

    /**
     * 实例化对象
     *
     * @param clazz 类
     * @param <T>   对象类型
     *
     * @return 新实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<?> clazz) {
        try {
            return (T) clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 根据类名实例化对象
     *
     * @param className 完全限定类名
     * @param <T>       对象类型
     *
     * @return 新实例
     */
    public static <T> T newInstance(String className) {
        try {
            return newInstance(Class.forName(className));
        } catch (ClassNotFoundException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    // endregion

    // region 属性操作

    /**
     * 获取 Bean 属性值
     *
     * @param bean         Bean 对象
     * @param propertyName 属性名
     *
     * @return 属性值
     */
    public static Object getProperty(Object bean, String propertyName) {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(propertyName, bean.getClass());
            Method readMethod = pd.getReadMethod();
            return readMethod.invoke(bean);
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 设置 Bean 属性值
     *
     * @param bean         Bean 对象
     * @param propertyName 属性名
     * @param value        属性值
     */
    public static void setProperty(Object bean, String propertyName, Object value) {
        try {
            Field field = bean.getClass().getDeclaredField(propertyName);
            field.setAccessible(true);
            field.set(bean, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    // endregion

    // region 对象复制

    /**
     * 浅复制对象
     * <p>
     * 通过反射创建新实例并复制所有字段值。
     *
     * @param source 源对象
     * @param <T>    对象类型
     *
     * @return 复制后的新对象，源对象为 null 时返回 null
     */
    @SuppressWarnings("unchecked")
    public static <T> @Nullable T clone(@Nullable T source) {
        if (source == null) {
            return null;
        }
        Class<T> clazz = (Class<T>) source.getClass();
        Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
        Class<?>[] paramTypes = constructor.getParameterTypes();
        Object[] initArgs = new Object[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            //noinspection DataFlowIssue
            initArgs[i] = getDefaultValue(paramTypes[i]);
        }

        try {
            constructor.setAccessible(true);
            T target = (T) constructor.newInstance(initArgs);
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                field.set(target, field.get(source));
            }
            return target;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    /**
     * 获取类型的默认值
     *
     * @param type 类型
     *
     * @return 默认值
     */
    private static @Nullable Object getDefaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) return false;
        if (type == char.class) return '\u0000';
        if (type == float.class) return 0.0f;
        if (type == double.class) return 0.0d;
        return 0; // byte, short, int, long
    }

    // endregion
}

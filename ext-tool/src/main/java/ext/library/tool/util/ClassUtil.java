package ext.library.tool.util;

import ext.library.tool.core.Exceptions;
import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;

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
 * 类工具
 *
 * @since 2025.08.19
 */
@UtilityClass
public class ClassUtil {


    // ---------------------- judge ----------------------

    private static final Map<Class<?>, Class<?>> primitiveWrapper2TypeMap = new IdentityHashMap<>(9);
    private static final Map<Class<?>, Class<?>> primitiveType2WrapperMap = new IdentityHashMap<>(9);

    static {
        primitiveWrapper2TypeMap.put(Boolean.class, boolean.class);
        primitiveWrapper2TypeMap.put(Byte.class, byte.class);
        primitiveWrapper2TypeMap.put(Character.class, char.class);
        primitiveWrapper2TypeMap.put(Double.class, double.class);
        primitiveWrapper2TypeMap.put(Float.class, float.class);
        primitiveWrapper2TypeMap.put(Integer.class, int.class);
        primitiveWrapper2TypeMap.put(Long.class, long.class);
        primitiveWrapper2TypeMap.put(Short.class, short.class);
        primitiveWrapper2TypeMap.put(Void.class, void.class);

        // Map entry iteration is less expensive to initialize than forEach with lambdas
        for (Map.Entry<Class<?>, Class<?>> entry : primitiveWrapper2TypeMap.entrySet()) {
            primitiveType2WrapperMap.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * Check if the super-type may be assigned to the sub-type
     * Considers primitive wrapper classes as assignable to the corresponding primitive types.
     *
     * @param superType the target type (left-hand side (LHS) type)
     * @param subType   the value type (right-hand side (RHS) type) that should be assigned to the target type
     *
     * @return {@code true} if {@code rhsType} is assignable to {@code lhsType}
     */
    public static boolean isAssignable(Class<?> superType, Class<?> subType) {
        Assert.notNull(superType, "Left-hand side type must not be null");
        Assert.notNull(subType, "Right-hand side type must not be null");
        if (superType.isAssignableFrom(subType)) {
            return true;
        }
        if (superType.isPrimitive()) {
            Class<?> resolvedPrimitive = primitiveWrapper2TypeMap.get(subType);
            return (superType == resolvedPrimitive);
        } else {
            Class<?> resolvedWrapper = primitiveType2WrapperMap.get(subType);
            return (resolvedWrapper != null && superType.isAssignableFrom(resolvedWrapper));
        }
    }


    /**
     * Check if the right-hand side type may be assigned to the left-hand side type
     *
     * @param superType super type
     * @param subType   the sub type that should be assigned to the target type
     *
     * @return true if subType is assignable to superType
     */
    public static boolean isAssignable(Type superType, Type subType) {
        Assert.notNull(superType, "Left-hand side type must not be null");
        Assert.notNull(subType, "Right-hand side type must not be null");

        // all types are assignable to themselves and to class Object
        if (superType.equals(subType) || Object.class == superType) {
            return true;
        }

        if (superType instanceof Class<?> lhsClass) {

            // just comparing two classes
            if (subType instanceof Class) {
                return isAssignable(lhsClass, (Class<?>) subType);
            }

            // parameterized types are only assignable to other parameterized types
            if (subType instanceof ParameterizedType) {
                Type rhsRaw = ((ParameterizedType) subType).getRawType();

                // a parameterized type is always assignable to its raw class type
                if (rhsRaw instanceof Class) {
                    return isAssignable(lhsClass, (Class<?>) rhsRaw);
                }
            } else if (lhsClass.isArray() && subType instanceof GenericArrayType) {
                Type rhsComponent = ((GenericArrayType) subType).getGenericComponentType();

                return isAssignable(lhsClass.getComponentType(), rhsComponent);
            }
        }

        // parameterized types are only assignable to other parameterized types and class types
        if (superType instanceof ParameterizedType) {
            if (subType instanceof Class) {
                Type lhsRaw = ((ParameterizedType) superType).getRawType();

                if (lhsRaw instanceof Class) {
                    return isAssignable((Class<?>) lhsRaw, (Class<?>) subType);
                }
            } else if (subType instanceof ParameterizedType) {
                return isAssignable(superType, subType);
            }
        }

        if (superType instanceof GenericArrayType) {
            Type lhsComponent = ((GenericArrayType) superType).getGenericComponentType();

            if (subType instanceof Class<?> rhsClass) {

                if (rhsClass.isArray()) {
                    return isAssignable(lhsComponent, rhsClass.getComponentType());
                }
            } else if (subType instanceof GenericArrayType) {
                Type rhsComponent = ((GenericArrayType) subType).getGenericComponentType();

                return isAssignable(lhsComponent, rhsComponent);
            }
        }

        if (superType instanceof WildcardType) {
            return isAssignable(superType, subType);
        }

        return false;
    }


    /**
     * Determine whether the given class has a public method with the given signature, and return it if available (else return {@code null}).
     *
     * <p>In case of any signature specified, only returns the method if there is a
     * unique candidate, i.e. a single public method with the specified name.
     * <p>Essentially translates {@code NoSuchMethodException} to {@code null}.
     *
     * @param clazz      the clazz to analyze
     * @param methodName the name of the method
     * @param paramTypes the parameter types of the method (can be {@code null} to indicate any signature)
     *
     * @return the method, or {@code null} if not found
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        Assert.notNull(clazz, "Class must not be null");
        Assert.notNull(methodName, "Method name must not be null");
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

    private static Method getMethodOrNull(Class<?> clazz, String methodName, Class<?>[] paramTypes) {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    private static Set<Method> findMethodCandidatesByName(Class<?> clazz, String methodName) {
        Set<Method> candidates = new HashSet<>(1);
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (methodName.equals(method.getName())) {
                candidates.add(method);
            }
        }
        return candidates;
    }

    /**
     * 获取方法参数信息
     *
     * @param constructor    构造器
     * @param parameterIndex 参数序号
     *
     * @return {MethodParameter}
     */
    public Parameter getMethod(Constructor<?> constructor, int parameterIndex) {
        return constructor.getParameters()[parameterIndex];
    }

    /**
     * 获取方法参数信息
     *
     * @param method         方法
     * @param parameterIndex 参数序号
     *
     * @return {MethodParameter}
     */
    public Parameter getMethodParameter(Method method, int parameterIndex) {
        return method.getParameters()[parameterIndex];
    }

    /**
     * 获取 Annotation 注解
     *
     * @param annotatedElement AnnotatedElement
     * @param annotationType   注解类
     * @param <A>              泛型标记
     *
     * @return {Annotation}
     */

    public <A extends Annotation> A getAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
        return annotatedElement.getDeclaredAnnotation(annotationType);
    }

    /**
     * 获取 Annotation，先找方法，没有则再找方法上的类
     *
     * @param method         Method
     * @param annotationType 注解类
     * @param <A>            泛型标记
     *
     * @return {Annotation}
     */

    public <A extends Annotation> A getAnnotation(Method method, Class<A> annotationType) {
        return ObjectUtil.defaultIfNull(method.getAnnotation(annotationType), method.getDeclaringClass().getAnnotation(annotationType));
    }

    /**
     * 实例化对象
     *
     * @param clazz 类
     * @param <T>   泛型标记
     *
     * @return 对象
     */
    @SuppressWarnings("unchecked")
    public <T> T newInstance(Class<?> clazz) {
        try {
            return (T) clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 实例化对象
     *
     * @param clazzStr 类名
     * @param <T>      泛型标记
     *
     * @return 对象
     */
    public <T> T newInstance(String clazzStr) {
        try {
            return newInstance(Class.forName(clazzStr));
        } catch (ClassNotFoundException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 获取 Bean 的属性
     *
     * @param bean         bean
     * @param propertyName 属性名
     *
     * @return 属性值
     */

    public Object getProperty(Object bean, String propertyName) {
        Class<?> beanClass = bean.getClass();
        try {
            PropertyDescriptor pd = new PropertyDescriptor(propertyName, beanClass);
            Method getMethod = pd.getReadMethod();
            return getMethod.invoke(bean);
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 设置 Bean 属性
     *
     * @param bean         bean
     * @param propertyName 属性名
     * @param value        属性值
     */
    public void setProperty(Object bean, String propertyName, Object value) {
        Class<?> beanClass = bean.getClass();
        try {
            // 获取属性对象
            Field declaredField = beanClass.getDeclaredField(propertyName);
            declaredField.setAccessible(true);
            // 修改属性值
            declaredField.set(bean, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 浅复制
     *
     * @param source 源对象
     * @param <T>    泛型标记
     *
     * @return T
     */

    @SuppressWarnings("unchecked")
    public <T> T clone(T source) {
        if (source == null) {
            return null;
        }
        // 1.获取字节码对象
        Class<T> clz = (Class<T>) source.getClass();
        // 2.获取实例对象（新对象）
        // 获取构造方法（保证一定能获取一个构造方法）
        Constructor<?> c = clz.getDeclaredConstructors()[0];
        // 获取构造方法的参数列表的所有类型
        Class<?>[] cs = c.getParameterTypes();
        // 新建 Object 类型的数组，存放每个参数给与的初始值
        Object[] os = new Object[cs.length];
        // 遍历数组
        for (int i = 0; i < cs.length; i++) {
            // 判断是否是基本数据类型
            if (cs[i].isPrimitive()) {
                // 基本数据类型
                if (cs[i] == byte.class || cs[i] == short.class || cs[i] == int.class || cs[i] == long.class) {
                    // 初始值赋值为 0
                    os[i] = 0;
                }
                if (cs[i] == char.class) {
                    os[i] = '\u0000';
                }
                if (cs[i] == float.class) {
                    os[i] = 0.0F;
                }
                if (cs[i] == double.class) {
                    os[i] = 0.0;
                }
                if (cs[i] == boolean.class) {
                    os[i] = false;
                }

            } else {
                // 引用数据类型
                os[i] = null;
            }
        }
        try {
            // 给定值，执行构造方法
            // 返回实例对象 //返回值有 3 个点，
            T o = (T) c.newInstance(os);
            // 3.获取原对象的所有属性 指定
            Field[] fs = clz.getDeclaredFields();

            // 4.把原对象的属性值赋值到新对象中
            for (Field f : fs) {
                // 暴力破解
                f.setAccessible(true);
                // 获取原对象的属性值
                Object value = f.get(source);
                // 把原对象的属性值赋值到新对象的属性中
                f.set(o, value);
            }
            // 返回新对象
            return o;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw Exceptions.unchecked(e);
        }
    }

}
package ext.library.core.util;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;
import ext.library.tool.util.ClassUtil;
import ext.library.tool.util.ObjectUtil;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.util.ObjectUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Bean 工具类
 */
public final class BeanUtil {

    private BeanUtil() {
    }

    /**
     * 对象转 Map
     *
     * @param obj 对象
     * @return Map
     */
    public static Map<String, @Nullable Object> beanToMap(Object obj) {
        Map<String, @Nullable Object> map = new HashMap<>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
                String key = property.getName();
                if ("class".equalsIgnoreCase(key)) {
                    continue;
                }
                Method getter = property.getReadMethod();
                map.put(key, getter != null ? getter.invoke(obj) : null);
            }
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
        return map;
    }

    /**
     * Map 转对象
     * <p>
     * 只拷贝 map 中<b>确实存在</b>的 key，避免把 target 上已有的默认值/已预置字段清成 null。
     *
     * @param map         Map
     * @param targetClass 目标类型
     * @return 目标对象
     */
    public static <T> T mapToBean(Map<String, Object> map, Class<T> targetClass) {
        T object = ClassUtil.newInstance(targetClass);
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass());
            for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
                String name = property.getName();
                Method setter = property.getWriteMethod();
                if (setter != null && map.containsKey(name)) {
                    setter.invoke(object, map.get(name));
                }
            }
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            // IllegalArgumentException：map 中的值类型与 setter 参数类型不匹配（如 String 赋给 int 属性）
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
        return object;
    }

    /**
     * 获取 Bean 的属性，支持 propertyName 多级：test.user.name
     *
     * @param bean         bean
     * @param propertyName 属性名
     * @return 属性值
     */
    public static @Nullable Object getProperty(Object bean, String propertyName) {
        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(bean);
        return beanWrapper.getPropertyValue(propertyName);
    }

    /**
     * 设置 Bean 属性，支持 propertyName 多级：test.user.name
     *
     * @param bean         bean
     * @param propertyName 属性名
     * @param value        属性值
     */
    public static void setProperty(Object bean, String propertyName, Object value) {
        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(
                Objects.requireNonNull(bean, "Bean 不能为 null"));
        beanWrapper.setPropertyValue(propertyName, value);
    }

    /**
     * 深度拷贝
     *
     * @param source 待拷贝的对象
     * @return 拷贝之后的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepClone(T source) {
        FastByteArrayOutputStream fBos = new FastByteArrayOutputStream(1024);
        try (ObjectOutputStream oos = new ObjectOutputStream(fBos)) {
            oos.writeObject(source);
            oos.flush();
        } catch (IOException ex) {
            throw new ToolException(EmojiSymbol.TOOL, ex);
        }
        try (ObjectInputStream ois = new ObjectInputStream(fBos.getInputStream())) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            throw new ToolException(EmojiSymbol.TOOL, ex);
        }
    }

    /**
     * 将 S 类型对象，转换为 T 类型的对象并返回
     *
     * @param source     数据来源实体
     * @param targetType 目标类型
     * @return 转换后的对象
     */
    @SuppressWarnings("unchecked")
    public static <S, T> T convert(S source, Class<T> targetType) {
        if (ObjectUtils.isEmpty(source)) {
            return ClassUtil.newInstance(targetType);
        }
        if (targetType.equals(source.getClass())) {
            return (T) source;
        }
        if (targetType.isRecord()) {
            // record 无 setter 也不可变，只能逐个读源属性后走规范构造器重建
            return (T) ClassUtil.newRecord(targetType, recordArgs(source, targetType));
        }
        T target = ClassUtil.newInstance(targetType);
        copyProperties(source, target);
        return target;
    }

    /**
     * 按 record 分量顺序从 source 取值，作为规范构造器的实参
     */
    private static Object[] recordArgs(Object source, Class<?> recordClass) {
        BeanWrapper sourceWrapper = PropertyAccessorFactory.forBeanPropertyAccess(source);
        RecordComponent[] components = recordClass.getRecordComponents();
        Object[] args = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            args[i] = sourceWrapper.getPropertyValue(components[i].getName());
        }
        return args;
    }

    /**
     * 将 S 类型对象的属性拷贝到 T 类型的对象
     *
     * @param source 数据来源实体
     * @param target 目标对象，属性将被复制到此对象
     */
    public static <S, T> void convert(@Nullable S source, @Nullable T target) {
        if (source == null || target == null) {
            return;
        }
        copyProperties(source, target);
    }

    /**
     * 将 S 类型的集合，转换为 T 类型的集合并返回
     *
     * @param sourceList 数据来源实体列表
     * @param targetType 目标类型
     * @return 转换后的列表
     */
    @SuppressWarnings("unchecked")
    public static <S, T> List<T> convert(List<S> sourceList, Class<T> targetType) {
        if (ObjectUtil.isEmpty(sourceList)) {
            return Collections.emptyList();
        }
        if (targetType.equals(sourceList.getFirst().getClass())) {
            return (List<T>) sourceList;
        }
        // 返回可变列表：调用方普遍会对结果做增删，Stream.toList() 的不可变列表会抛 UnsupportedOperationException
        return sourceList.stream()
                .map(source -> convert(source, targetType))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 将 Map 转换为指定类型的对象
     *
     * @param map        数据来源
     * @param targetType 目标类型
     * @return 转换后的对象
     */
    public static <T> T convert(Map<String, Object> map, Class<T> targetType) {
        if (ObjectUtil.isEmpty(map)) {
            return ClassUtil.newInstance(targetType);
        }
        return mapToBean(map, targetType);
    }

    /**
     * 将 source 的属性拷贝到 target（浅拷贝，Date/List 等引用会共享）
     * <p>
     * 使用 Spring 的 {@link BeanUtils#copyProperties}：它是浅拷贝且对
     * 包级私有 DTO / Lombok {@code @Value} 等无无参构造的目标类都有效。
     * 曾尝试用 cglib BeanCopier 加速，但它要求源/目标类均为 public 且有 public 无参构造，
     * 且 {@code BeanCopier.create} 抛异常时无法靠 catch 回落——那条「失败回落反射」的路径是失效的。
     * <p>
     * record 没有 setter，Spring 的拷贝对其无效，所以 record 由调用方改走
     * {@link #convert(Object, Class)} 的构造器分支处理，不经过这里。
     *
     * @param source 数据来源实体
     * @param target 目标对象，属性将被复制到此对象
     */
    private static void copyProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target);
    }

}

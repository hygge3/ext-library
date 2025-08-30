package ext.library.core.util;

import com.google.common.collect.Maps;
import ext.library.tool.core.Exceptions;
import ext.library.tool.holder.Lazy;
import ext.library.tool.util.ClassUtil;
import ext.library.tool.util.ObjectUtil;
import io.github.linpeilie.Converter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.util.ObjectUtils;

import jakarta.annotation.Nonnull;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * bean 工具类
 */
@Slf4j
@UtilityClass
public class BeanUtil {

    private static final Lazy<Converter> CONVERTER = Lazy.of(() -> SpringUtil.getBean(Converter.class));

    private static final Lazy<Map<String, BeanCopier>> BEAN_COPIER_CACHE = Lazy.of(ConcurrentHashMap::new);

    /**
     * 对象转 Map
     *
     * @param obj 对象
     *
     * @return {@code Map<String, Object> }
     */
    public Map<String, Object> beanToMap(@Nonnull Object obj) {
        Map<String, Object> map = Maps.newHashMap();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();
                if (key.compareToIgnoreCase("class") == 0) {
                    continue;
                }
                Method getter = property.getReadMethod();
                Object value = getter != null ? getter.invoke(obj) : null;
                map.put(key, value);
            }
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            throw Exceptions.unchecked(e);
        }
        return map;
    }

    /**
     * map 转对象
     *
     * @param map         map
     * @param targetClass 目标类别
     *
     * @return {@code T }
     */
    public <T> T mapToBean(Map<String, Object> map, Class<T> targetClass) {
        T object = org.springframework.beans.BeanUtils.instantiateClass(targetClass);
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                Method setter = property.getWriteMethod();
                if (setter != null) {
                    setter.invoke(object, map.get(property.getName()));
                }
            }
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
            throw Exceptions.unchecked(e);
        }
        return object;
    }

    /**
     * 获取 Bean 的属性，支持 propertyName 多级：test.user.name
     *
     * @param bean         bean
     * @param propertyName 属性名
     *
     * @return 属性值
     */
    public Object getProperty(Object bean, String propertyName) {
        if (bean == null) {
            return null;
        }
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
    public void setProperty(Object bean, String propertyName, Object value) {
        BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(Objects.requireNonNull(bean, "Bean could not null"));
        beanWrapper.setPropertyValue(propertyName, value);
    }

    /**
     * 深度拷贝
     *
     * @param source 待拷贝的对象
     *
     * @return 拷贝之后的对象
     */

    @SuppressWarnings("unchecked")
    public <T> T deepClone(T source) {
        if (source == null) {
            return null;
        }
        FastByteArrayOutputStream fBos = new FastByteArrayOutputStream(1024);
        try (ObjectOutputStream oos = new ObjectOutputStream(fBos)) {
            oos.writeObject(source);
            oos.flush();
        } catch (IOException ex) {
            throw new IllegalArgumentException("未能序列化类型为" + source.getClass() + "的对象", ex);
        }
        try (ObjectInputStream ois = new ObjectInputStream(fBos.getInputStream())) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            throw new IllegalArgumentException("未能反序列化对象", ex);
        }
    }

    /**
     * 将 S 类型对象，转换为 T 类型的对象并返回
     *
     * @param source     数据来源实体
     * @param targetType 转换后的对象
     *
     * @return targetType
     */
    @SuppressWarnings("unchecked")
    public <S, T> T convert(S source, Class<T> targetType) {
        if (ObjectUtils.isEmpty(source)) {
            return ClassUtil.newInstance(targetType);
        }
        if (targetType.equals(source.getClass())) {
            return (T) source;
        }
        try {
            return CONVERTER.get().convert(source, targetType);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return copyByCopier(source, targetType);
    }

    /**
     * 将 S 类型对象，按照配置的映射字段规则，给 T 类型的对象赋值并返回 T 对象
     *
     * @param source 数据来源实体
     * @param target 转换后的对象
     */
    @SuppressWarnings("unchecked")
    public <S, T> void convert(@Nonnull S source, @Nonnull T target) {
        if (target.getClass().equals(source.getClass())) {
            target = (T) source;
        }
        try {
            target = CONVERTER.get().convert(source, target);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        copyByCopier(source, target);
    }

    /**
     * 将 T 类型的集合，转换为 desc 类型的集合并返回
     *
     * @param sourceList 数据来源实体列表
     * @param targetType 描述对象 转换后的对象
     *
     * @return targetType
     */

    @SuppressWarnings("unchecked")
    public <S, T> List<T> convert(List<S> sourceList, Class<T> targetType) {
        if (ObjectUtil.isEmpty(sourceList)) {
            return Collections.emptyList();
        }
        if (targetType.equals(sourceList.getFirst().getClass())) {
            return (List<T>) sourceList;
        }
        try {
            return CONVERTER.get().convert(sourceList, targetType);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return copyListByCopier(sourceList, targetType);
    }

    /**
     * 将 Map 转换为 beanClass 类型的集合并返回
     *
     * @param map        数据来源
     * @param targetType bean 类
     *
     * @return bean 对象
     */
    public <T> T convert(Map<String, Object> map, Class<T> targetType) {
        if (ObjectUtil.isEmpty(map)) {
            return ClassUtil.newInstance(targetType);
        }
        try {
            return CONVERTER.get().convert(map, targetType);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return mapToBean(map, targetType);
    }

    private void copyByCopier(@Nonnull Object source, @Nonnull Object target) {
        Class<?> sourceType = source.getClass();
        Class<?> targetType = target.getClass();
        String beanKey = sourceType.getName() + targetType.getName();
        BeanCopier copier;
        if (!BEAN_COPIER_CACHE.get().containsKey(beanKey)) {
            copier = BeanCopier.create(sourceType, targetType, false);
            BEAN_COPIER_CACHE.get().put(beanKey, copier);
        } else {
            copier = BEAN_COPIER_CACHE.get().get(beanKey);
        }
        try {
            copier.copy(source, target, null);
        } catch (Exception e) {
            org.springframework.beans.BeanUtils.copyProperties(source, target);
        }
    }

    private <T> T copyByCopier(Object source, @Nonnull Class<T> targetType) {
        T t;
        try {
            t = targetType.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw Exceptions.throwOut(e, "创建 {} 的新实例失败：｛｝", targetType, e.getMessage());
        }
        org.springframework.beans.BeanUtils.copyProperties(source, t);
        return t;
    }

    @Nonnull
    private <S, T> List<T> copyListByCopier(@Nonnull List<S> sourceList, Class<T> targetType) {
        List<T> resultList = new ArrayList<>(sourceList.size());
        for (Object source : sourceList) {
            T target;
            try {
                target = targetType.getDeclaredConstructor().newInstance();
                try {
                    copyByCopier(source, target);
                } catch (Exception e) {
                    org.springframework.beans.BeanUtils.copyProperties(source, target);
                }
            } catch (Exception e) {
                throw Exceptions.unchecked(e);
            }
            resultList.add(target);
        }
        return resultList;
    }

}
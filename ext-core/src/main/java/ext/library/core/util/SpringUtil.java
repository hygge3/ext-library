package ext.library.core.util;

import ext.library.tool.core.Exceptions;
import ext.library.tool.util.ObjectUtil;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.boot.autoconfigure.thread.Threading;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nonnull;
import java.util.Map;

/**
 * Spring 工具类
 */
@Component
public class SpringUtil implements BeanFactoryPostProcessor, ApplicationContextAware {

    /**
     * "@PostConstruct"注解标记的类中，由于 ApplicationContext 还未加载，导致空指针<br>
     * 因此实现 BeanFactoryPostProcessor 注入 ConfigurableListableBeanFactory 实现 bean 的操作
     */
    private static ConfigurableListableBeanFactory beanFactory;
    /**
     * Spring 应用上下文环境 -- GETTER -- 获取
     */
    private static ApplicationContext context;

    public static void setContext(ApplicationContext context) {
        SpringUtil.context = context;
    }

    /**
     * 获取{@link ListableBeanFactory}，可能为{@link ConfigurableListableBeanFactory} 或
     * {@link ApplicationContextAware}
     *
     * @return {@link ListableBeanFactory}
     */
    public static ListableBeanFactory getBeanFactory() {
        final ListableBeanFactory factory = ObjectUtil.defaultIfNull(beanFactory, context);
        if (null == factory) {
            throw Exceptions.throwOut("[🫛] 没有注入 ConfigurableListableBeanFactory 或 ApplicationContext，可能不是在 Spring 环境中？");
        }
        return factory;
    }

    /**
     * 获取{@link ConfigurableListableBeanFactory}
     *
     * @return {@link ConfigurableListableBeanFactory}
     */
    public static ConfigurableListableBeanFactory getConfigurableBeanFactory() {
        final ConfigurableListableBeanFactory factory;
        if (null != beanFactory) {
            factory = beanFactory;
        } else if (context instanceof ConfigurableApplicationContext) {
            factory = ((ConfigurableApplicationContext) context).getBeanFactory();
        } else {
            throw Exceptions.throwOut("[🫛] 上下文中没有可配置的 BeanFactory！");
        }
        return factory;
    }

    /**
     * 通过 name 获取 Bean
     *
     * @param <T>  Bean 类型
     * @param name Bean 名称
     *
     * @return Bean
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) context.getBean(name);
    }

    /**
     * 通过 name，以及 Clazz 返回指定的 Bean
     *
     * @param name Bean 名称
     * @param args 创建 bean 需要的参数属性
     *
     * @return Bean 对象
     */
    public static Object getBean(String name, Object... args) {
        return getBeanFactory().getBean(name, args);
    }

    /**
     * 通过 class 获取 Bean
     *
     * @param <T>   Bean 类型
     * @param clazz Bean 类
     *
     * @return Bean 对象
     */
    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    /**
     * 通过 class 获取 Bean
     *
     * @param <T>   Bean 类型
     * @param clazz Bean 类
     * @param args  创建 bean 需要的参数属性
     *
     * @return Bean 对象
     */
    public static <T> T getBean(Class<T> clazz, Object... args) {
        return getBeanFactory().getBean(clazz, args);
    }

    /**
     * 通过 name，以及 Clazz 返回指定的 Bean
     *
     * @param <T>   bean 类型
     * @param name  Bean 名称
     * @param clazz bean 类型
     *
     * @return Bean 对象
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return context.getBean(name, clazz);
    }

    /**
     * 获取指定类型对应的所有 Bean，包括子类
     *
     * @param <T>  Bean 类型
     * @param type 类、接口，null 表示获取所有 bean
     *
     * @return 类型对应的 bean，key 是 bean 注册的 name，value 是 Bean
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return context.getBeansOfType(type);
    }

    /**
     * 获取指定类型对应的 Bean 名称，包括子类
     *
     * @param type 类、接口，null 表示获取所有 bean 名称
     *
     * @return bean 名称
     */
    public static String[] getBeanNamesForType(Class<?> type) {
        return context.getBeanNamesForType(type);
    }

    /**
     * 获取指定类型对应的 Bean 名称，包括子类
     *
     * @param type 类、接口，null 表示获取所有 bean 名称
     *
     * @return bean 名称
     */
    public static String[] getBeanNamesForType(ResolvableType type) {
        return context.getBeanNamesForType(type);
    }

    /**
     * 获取配置文件配置项的值
     *
     * @param key 配置项 key
     *
     * @return 属性值
     */
    public static String getProperty(String key) {
        return context.getEnvironment().getProperty(key);
    }

    /**
     * 获取配置文件配置项的值
     *
     * @param key          配置项 key
     * @param defaultValue 默认值
     *
     * @return 属性值
     */
    public static String getProperty(String key, String defaultValue) {
        return context.getEnvironment().getProperty(key, defaultValue);
    }

    /**
     * 获取配置文件配置项的值
     *
     * @param <T>          属性值类型
     * @param key          配置项 key
     * @param targetType   配置项类型
     * @param defaultValue 默认值
     *
     * @return 属性值
     */
    public static <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return context.getEnvironment().getProperty(key, targetType, defaultValue);
    }

    /**
     * 获取应用程序名称
     *
     * @return 应用程序名称
     */
    public static String getApplicationName() {
        return getProperty("spring.application.name");
    }

    /**
     * 获取当前的环境配置，无配置返回 null
     *
     * @return 当前的环境配置
     */
    public static String[] getActiveProfiles() {
        return context.getEnvironment().getActiveProfiles();
    }

    /**
     * 获取环境
     */
    public static Environment getEnvironment() {
        return context.getEnvironment();
    }

    /**
     * 动态向 Spring 注册 Bean
     * <p>
     * 由{@link org.springframework.beans.factory.BeanFactory} 实现，通过工具开放 API
     * <p>
     * 更新：shadow 2021-07-29 17:20:44 增加自动注入，修复注册 bean 无法反向注入的问题
     *
     * @param <T>      Bean 类型
     * @param beanName 名称
     * @param bean     bean
     *
     * @author shadow
     * @since 5.4.2
     */
    public static <T> void registerBean(String beanName, T bean) {
        final ConfigurableListableBeanFactory factory = getConfigurableBeanFactory();
        factory.autowireBean(bean);
        factory.registerSingleton(beanName, bean);
    }

    /**
     * 注销 bean
     * <p>
     * 将 Spring 中的 bean 注销，请谨慎使用
     *
     * @param beanName bean 名称
     *
     * @author shadow
     * @since 5.7.7
     */
    public static void unregisterBean(String beanName) {
        final ConfigurableListableBeanFactory factory = getConfigurableBeanFactory();
        if (factory instanceof DefaultSingletonBeanRegistry registry) {
            registry.destroySingleton(beanName);
        } else {
            throw Exceptions.throwOut("[🫛] 无法取消注册 bean，工厂不是 DefaultSingletonBeanRegistry！");
        }
    }

    /**
     * 发布事件 Spring 4.2+ 版本事件可以不再是{@link ApplicationEvent}的子类
     *
     * @param event 待发布的事件
     */
    public static void publishEvent(Object event) {
        context.publishEvent(event);
    }

    /**
     * 获取 aop 代理对象
     *
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T getCurrentProxy() {
        return (T) AopContext.currentProxy();
    }

    public static boolean isVirtual() {
        return Threading.VIRTUAL.isActive(getBean(Environment.class));
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext context) throws BeansException {
        setContext(context);
    }

    @Override
    public void postProcessBeanFactory(@Nonnull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        SpringUtil.beanFactory = beanFactory;
    }

}
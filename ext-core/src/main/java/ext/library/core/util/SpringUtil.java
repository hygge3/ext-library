package ext.library.core.util;

import java.util.Map;

import ext.library.tool.$;
import ext.library.tool.core.Exceptions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.thread.Threading;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Spring 工具类
 */
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpringUtil implements BeanFactoryPostProcessor, ApplicationContextAware {

    /**
     * Spring 应用上下文环境 -- GETTER -- 获取
     */
    @Getter
    @Setter
    private static ApplicationContext context;

    /**
     * "@PostConstruct"注解标记的类中，由于 ApplicationContext 还未加载，导致空指针<br>
     * 因此实现 BeanFactoryPostProcessor 注入 ConfigurableListableBeanFactory 实现 bean 的操作
     */
    private static ConfigurableListableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
        setContext(context);
    }

    @Override
    public void postProcessBeanFactory(@NotNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        SpringUtil.beanFactory = beanFactory;
    }

    /**
     * 获取{@link ListableBeanFactory}，可能为{@link ConfigurableListableBeanFactory} 或
     * {@link ApplicationContextAware}
     *
     * @return {@link ListableBeanFactory}
     */
    public static ListableBeanFactory getBeanFactory() {
        final ListableBeanFactory factory = $.defaultIfNull(beanFactory, context);
        if (null == factory) {
            throw Exceptions.throwOut(
                    "没有注入 ConfigurableListableBeanFactory 或 ApplicationContext，可能不是在 Spring 环境中？");
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
            throw Exceptions.throwOut("上下文中没有可配置的 BeanFactory！");
        }
        return factory;
    }

    /**
     * 通过 name 获取 Bean
     *
     * @param <T>  Bean 类型
     * @param name Bean 名称
     * @return Bean
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) context.getBean(name);
    }

    /**
     * 通过 class 获取 Bean
     *
     * @param <T>   Bean 类型
     * @param clazz Bean 类
     * @return Bean 对象
     */
    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    /**
     * 通过 name，以及 Clazz 返回指定的 Bean
     *
     * @param <T>   bean 类型
     * @param name  Bean 名称
     * @param clazz bean 类型
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
     * @return 类型对应的 bean，key 是 bean 注册的 name，value 是 Bean
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return context.getBeansOfType(type);
    }

    /**
     * 获取指定类型对应的 Bean 名称，包括子类
     *
     * @param type 类、接口，null 表示获取所有 bean 名称
     * @return bean 名称
     */
    public static String[] getBeanNamesForType(Class<?> type) {
        return context.getBeanNamesForType(type);
    }

    /**
     * 获取指定类型对应的 Bean 名称，包括子类
     *
     * @param type 类、接口，null 表示获取所有 bean 名称
     * @return bean 名称
     */
    public static String[] getBeanNamesForType(ResolvableType type) {
        return context.getBeanNamesForType(type);
    }

    /**
     * 获取配置文件配置项的值
     *
     * @param key 配置项 key
     * @return 属性值
     */
    public static String getProperty(String key) {
        return context.getEnvironment().getProperty(key);
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

}

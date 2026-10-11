package ext.library.core.util;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;
import ext.library.tool.util.ObjectUtil;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.boot.thread.Threading;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;

import java.util.Map;

/**
 * Spring 工具类
 */
public class SpringUtil implements BeanFactoryPostProcessor, ApplicationContextAware {

    /**
     * "@PostConstruct"注解标记的类中，由于 ApplicationContext 还未加载，导致空指针<br>
     * 因此实现 BeanFactoryPostProcessor 注入 ConfigurableListableBeanFactory 实现 bean 的操作
     */
    private static ConfigurableListableBeanFactory beanFactory;

    /**
     * Spring 应用上下文环境
     */
    private static ApplicationContext context;

    /**
     * 设置 Spring 应用上下文
     *
     * @param context Spring 应用上下文
     */
    public static void setContext(ApplicationContext context) {
        SpringUtil.context = context;
    }

    /**
     * 获取 Spring 应用上下文
     *
     * @return Spring 应用上下文
     * @throws ToolException 如果上下文未初始化
     */
    public static ApplicationContext getApplicationContext() {
        return requireContext();
    }

    /**
     * 获取 {@link ListableBeanFactory}，可能为 {@link ConfigurableListableBeanFactory} 或
     * {@link ApplicationContext}
     *
     * @return {@link ListableBeanFactory}
     * @throws ToolException 如果 BeanFactory 未初始化
     */
    public static ListableBeanFactory getBeanFactory() {
        ListableBeanFactory factory = ObjectUtil.defaultIfNull(beanFactory, context);
        if (factory == null) {
            throw new ToolException(EmojiSymbol.CORE, "没有注入 ConfigurableListableBeanFactory 或 ApplicationContext，可能不是在 Spring 环境中");
        }
        return factory;
    }

    /**
     * 获取 {@link ConfigurableListableBeanFactory}
     *
     * @return {@link ConfigurableListableBeanFactory}
     * @throws ToolException 如果上下文中没有可配置的 BeanFactory
     */
    public static ConfigurableListableBeanFactory getConfigurableBeanFactory() {
        if (beanFactory != null) {
            return beanFactory;
        }
        if (context instanceof ConfigurableApplicationContext cac) {
            return cac.getBeanFactory();
        }
        throw new ToolException(EmojiSymbol.CORE, "上下文中没有可配置的 BeanFactory");
    }

    /**
     * 通过 name 获取 Bean
     *
     * @param <T>  Bean 类型
     * @param name Bean 名称
     * @return Bean 实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) getBeanFactory().getBean(name);
    }

    /**
     * 通过 name 和参数获取 Bean
     *
     * @param name Bean 名称
     * @param args 创建 bean 需要的参数
     * @return Bean 实例
     */
    public static Object getBean(String name, Object... args) {
        return getBeanFactory().getBean(name, args);
    }

    /**
     * 通过 class 获取 Bean
     *
     * @param <T>   Bean 类型
     * @param clazz Bean 类
     * @return Bean 实例
     */
    public static <T> T getBean(Class<T> clazz) {
        return getBeanFactory().getBean(clazz);
    }

    /**
     * 通过 class 和参数获取 Bean
     *
     * @param <T>   Bean 类型
     * @param clazz Bean 类
     * @param args  创建 bean 需要的参数
     * @return Bean 实例
     */
    public static <T> T getBean(Class<T> clazz, Object... args) {
        return getBeanFactory().getBean(clazz, args);
    }

    /**
     * 通过 name 和 class 获取 Bean
     *
     * @param <T>   Bean 类型
     * @param name  Bean 名称
     * @param clazz Bean 类型
     * @return Bean 实例
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getBeanFactory().getBean(name, clazz);
    }

    /**
     * 获取指定类型对应的所有 Bean，包括子类
     *
     * @param <T>  Bean 类型
     * @param type 类、接口，null 表示获取所有 bean
     * @return 类型对应的 bean，key 是 bean 注册的 name，value 是 Bean
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> type) {
        return getBeanFactory().getBeansOfType(type);
    }

    /**
     * 获取指定类型对应的 Bean 名称，包括子类
     *
     * @param type 类、接口，null 表示获取所有 bean 名称
     * @return bean 名称数组
     */
    public static String[] getBeanNamesForType(Class<?> type) {
        return getBeanFactory().getBeanNamesForType(type);
    }

    /**
     * 获取指定类型对应的 Bean 名称，包括子类
     *
     * @param type 可解析类型
     * @return bean 名称数组
     */
    public static String[] getBeanNamesForType(ResolvableType type) {
        return getBeanFactory().getBeanNamesForType(type);
    }

    /**
     * 获取配置文件配置项的值
     *
     * @param key 配置项 key
     * @return 属性值，不存在时返回 null
     */
    public static @Nullable String getProperty(String key) {
        return getEnvironment().getProperty(key);
    }

    /**
     * 获取配置文件配置项的值
     *
     * @param key          配置项 key
     * @param defaultValue 默认值
     * @return 属性值，不存在时返回默认值
     */
    public static String getProperty(String key, String defaultValue) {
        return getEnvironment().getProperty(key, defaultValue);
    }

    /**
     * 获取配置文件配置项的值
     *
     * @param <T>          属性值类型
     * @param key          配置项 key
     * @param targetType   配置项类型
     * @param defaultValue 默认值
     * @return 属性值，不存在时返回默认值
     */
    public static <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        return getEnvironment().getProperty(key, targetType, defaultValue);
    }

    /**
     * 获取应用程序名称
     *
     * @return 应用程序名称，未配置时返回 null
     */
    public static @Nullable String getApplicationName() {
        return getProperty("spring.application.name");
    }

    /**
     * 获取当前激活的环境配置
     *
     * @return 激活的 profile 数组
     */
    public static String[] getActiveProfiles() {
        return getEnvironment().getActiveProfiles();
    }

    /**
     * 获取 Spring 环境
     *
     * @return Spring 环境对象
     */
    public static Environment getEnvironment() {
        return requireContext().getEnvironment();
    }

    /**
     * 动态向 Spring 注册 Bean
     * <p>
     * 由 {@link org.springframework.beans.factory.BeanFactory} 实现，通过工具开放 API。
     * 注册的 bean 会自动进行依赖注入。
     *
     * @param <T>      Bean 类型
     * @param beanName Bean 名称
     * @param bean     Bean 实例
     */
    public static <T> void registerBean(String beanName, T bean) {
        ConfigurableListableBeanFactory factory = getConfigurableBeanFactory();
        factory.autowireBean(bean);
        factory.registerSingleton(beanName, bean);
    }

    /**
     * 注销 Bean
     * <p>
     * 将 Spring 中的 bean 注销，请谨慎使用
     *
     * @param beanName bean 名称
     * @throws ToolException 如果工厂不支持注销操作
     */
    public static void unregisterBean(String beanName) {
        ConfigurableListableBeanFactory factory = getConfigurableBeanFactory();
        if (factory instanceof DefaultSingletonBeanRegistry registry) {
            registry.destroySingleton(beanName);
        } else {
            throw new ToolException(EmojiSymbol.CORE, "无法取消注册 bean，工厂不是 DefaultSingletonBeanRegistry");
        }
    }

    /**
     * 发布事件
     * <p>
     * Spring 4.2+ 版本事件可以不再是 ApplicationEvent 的子类
     *
     * @param event 待发布的事件
     */
    public static void publishEvent(Object event) {
        requireContext().publishEvent(event);
    }

    /**
     * 获取当前 AOP 代理对象
     *
     * @param <T> 代理对象类型
     * @return 代理对象
     * @throws IllegalStateException 如果当前不在 AOP 代理上下文中
     */
    @SuppressWarnings("unchecked")
    public static <T> T getCurrentProxy() {
        return (T) AopContext.currentProxy();
    }

    /**
     * 检查是否启用了虚拟线程
     *
     * @return 如果启用了虚拟线程返回 true
     */
    public static boolean isVirtual() {
        return Threading.VIRTUAL.isActive(getEnvironment());
    }

    // ==================== 内部方法 ====================

    /**
     * 获取 ApplicationContext，如果为空则抛出异常
     */
    private static ApplicationContext requireContext() {
        if (context == null) {
            throw new ToolException(EmojiSymbol.CORE, "ApplicationContext 未初始化，可能不是在 Spring 环境中");
        }
        return context;
    }

    // ==================== Spring 回调 ====================

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        setContext(context);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        SpringUtil.beanFactory = beanFactory;
    }

}

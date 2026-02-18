package ext.library.tool.builder;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * 实例构建器
 * 用于构建和配置对象实例，支持链式调用设置属性值
 *
 * @param <T> 要构建的实例类型
 */
public class InstanceBuilder<T> {

    /** 正在构建的实例对象 */
    private final T inst;

    /**
     * 私有构造函数 - 基于现有实例创建构建器
     *
     * @param inst 已存在的实例对象
     */
    private InstanceBuilder(T inst) {
        this.inst = Objects.requireNonNull(inst, "实例不能为空");
    }

    /**
     * 私有构造函数 - 基于类创建构建器（通过反射实例化）
     *
     * @param instClass 要实例化的类
     */
    private InstanceBuilder(Class<T> instClass) {
        Objects.requireNonNull(instClass, "instClass cannot be null");
        try {
            // 使用无参构造函数创建实例
            this.inst = instClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new ExtException(EmojiSymbol.TOOL, e, "未能创建 {} 的实例", instClass.getName());
        }
    }

    /**
     * 静态工厂方法 - 基于现有实例创建构建器
     *
     * @param <T>  实例类型
     * @param inst 已存在的实例对象
     *
     * @return InstBuilder 实例
     */
    public static <T> InstanceBuilder<T> of(T inst) {
        return new InstanceBuilder<>(inst);
    }

    /**
     * 静态工厂方法 - 基于类创建构建器
     *
     * @param <T>       实例类型
     * @param instClass 要实例化的类
     *
     * @return InstBuilder 实例
     */
    public static <T> InstanceBuilder<T> of(Class<T> instClass) {
        return new InstanceBuilder<>(instClass);
    }

    /**
     * 设置实例属性值
     * 使用函数式接口方式设置实例的特定属性，支持链式调用
     *
     * @param <V>   属性值类型
     * @param fnSet 设置属性的函数式接口
     * @param v     要设置的属性值
     *
     * @return 当前构建器实例（支持链式调用）
     */
    public <V> InstanceBuilder<T> set(SetterFunction<T, V> fnSet, V v) {
        fnSet.call(inst, v);
        return this;
    }

    /**
     * 带校验的 set 方法（重载，想用时用，不想用还能用原来的）
     *
     * @param fnSet     设置属性的函数式接口
     * @param v         要设置的属性值
     * @param validator 校验器
     * @param <V>       属性值类型
     *
     * @return 当前构建器实例（支持链式调用）
     */
    public <V> InstanceBuilder<T> set(SetterFunction<T, V> fnSet, V v, Predicate<V> validator) {
        // 先校验：符合条件才设置属性，不符合就报错
        if (!validator.test(v)) {
            throw new IllegalArgumentException("属性值不对：" + v);
        }
        fnSet.call(inst, v); // 校验通过再调用你原有的设置逻辑
        return this;
    }

    /**
     * 构建并返回配置完成的实例
     *
     * @return 构建完成的实例对象
     */
    public T build() {
        return inst;
    }
}

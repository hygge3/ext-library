package ext.library.tool.builder;

/**
 * Setter 方法函数式接口
 * 承接对象的属性设置行为，为 Lambda 表达式提供目标类型
 *
 * @param <T> 目标对象类型
 * @param <P> 属性值类型
 */
@FunctionalInterface // 显式声明函数式接口（可选，但推荐）
public interface SetterFunction<T, P> {
    /**
     * 执行属性设置
     *
     * @param target 目标对象
     * @param param  要设置的属性值
     */
    void call(T target, P param);
}

package ext.library.core.util.spel;

import org.springframework.expression.EvaluationException;
import org.springframework.expression.TypeLocator;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Collections;

/**
 * SpEL 上下文加固
 * <p>
 * {@link StandardEvaluationContext} 默认开放类型引用（{@code T(java.lang.Runtime).getRuntime().exec(...)}）
 * 和构造器（{@code new java.lang.ProcessBuilder(...).start()}），一旦
 * {@code spelExpression} 来自配置中心、数据库或前端，就是一条完整的远程代码执行路径。
 * <p>
 * 这里关掉这两项能力：表达式仍可访问 root 对象、方法参数（{@code #p0} / 参数名）和 bean，
 * 但不能凭空引用或构造类型。业务确实需要完整能力时，请自行使用
 * {@link StandardEvaluationContext} 并自行承担风险，不要走本工具类。
 */
final class SpelLock {

    /**
     * 拒绝一切类型加载的定位器
     */
    private static final TypeLocator NO_CLASS_LOADING = new TypeLocator() {

        @Override
        public Class<?> findType(String typeName) throws EvaluationException {
            throw new EvaluationException("不允许在表达式中引用类型：" + typeName);
        }

        @Override
        public String toString() {
            return "NO_CLASS_LOADING";
        }
    };

    private SpelLock() {
    }

    /**
     * 关闭类型引用与构造器解析
     *
     * @param context 原始上下文
     * @return 同一个上下文（保留 {@code #p0} 等参数绑定能力）
     */
    static <C extends StandardEvaluationContext> C lock(C context) {
        context.setTypeLocator(NO_CLASS_LOADING);
        context.setConstructorResolvers(Collections.emptyList());
        return context;
    }

}

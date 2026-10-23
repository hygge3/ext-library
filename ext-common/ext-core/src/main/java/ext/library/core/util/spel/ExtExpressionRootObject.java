package ext.library.core.util.spel;

import java.lang.reflect.Method;

/**
 * ExpressionRootObject
 * <p>
 * 额外提供 {@code getXxx()} 形式的 bean 属性访问器：record 的 {@code target()} 是无前缀访问器，
 * 只有 SpEL 的 {@code #root.target} 能命中；依赖它意味着隐式契约，
 * 加上 bean 风格方法后 {@code #root.target} 同样成立，不再依赖 Spring 内部对无前缀访问器的反射兼容规则。
 */
public record ExtExpressionRootObject(Method method, Object[] args, Object target, Class<?> targetClass,
                                      Method targetMethod) {

    public Object getTarget() {
        return target;
    }

    public Object[] getArgs() {
        return args;
    }

    public Object getMethod() {
        return method;
    }

    public Object getTargetClass() {
        return targetClass;
    }

    public Object getTargetMethod() {
        return targetMethod;
    }
}

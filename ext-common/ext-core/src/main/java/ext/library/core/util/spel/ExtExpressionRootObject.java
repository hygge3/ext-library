package ext.library.core.util.spel;

import java.lang.reflect.Method;

/**
 * ExpressionRootObject
 */
public record ExtExpressionRootObject(Method method, Object[] args, Object target, Class<?> targetClass,
                                      Method targetMethod) {}

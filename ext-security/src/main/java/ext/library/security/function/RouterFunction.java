package ext.library.security.function;

/**
 * <p>
 * 路由接口
 * </p>
 */
@FunctionalInterface
public interface RouterFunction<T> {

	boolean run(T t);

}

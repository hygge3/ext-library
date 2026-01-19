package ext.library.tool.holder.function;

import java.io.Serializable;

/**
 * 受检的 function
 */
@FunctionalInterface
public interface CheckedFunction<T, R> extends Serializable {

	/**
	 * Run the Function
	 * @param t T
	 * @return R R
	 * @throws Throwable CheckedException
	 */
	R apply( T t) throws Throwable;

}

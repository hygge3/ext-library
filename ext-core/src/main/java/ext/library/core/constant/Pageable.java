package ext.library.core.constant;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 分页常量
 */
public interface Pageable {

	/**
	 * 排序的 Field 部分的正则
	 */
	String SORT_FILED_REGEX = "(([A-Za-z0-9_]{1,10}\\.)?[A-Za-z0-9_]{1,64})";

	/**
	 * 排序的 order 部分的正则
	 */
	String SORT_FILED_ORDER = "(desc|asc)";

	/**
	 * 完整的排序规则正则
	 */
	String SORT_REGEX = "^" + Pageable.SORT_FILED_REGEX + "(," + Pageable.SORT_FILED_ORDER + ")*$";

	/**
	 * 默认的当前页数的参数名
	 */
	String DEFAULT_PAGE_PARAMETER = "page";

	/**
	 * 默认的单页条数的参数名
	 */
	String DEFAULT_SIZE_PARAMETER = "size";

	/**
	 * 默认的排序参数的参数名
	 */
	String DEFAULT_SORT_PARAMETER = "sort";

	/**
	 * 当前记录起始索引 默认值
	 */
	int DEFAULT_PAGE_NUM = 1;

	/**
	 * 每页显示记录数 默认值 默认查全部
	 */
	int DEFAULT_PAGE_SIZE = 10;

	/**
	 * 默认的最大单页条数
	 */
	int DEFAULT_MAX_PAGE_SIZE = 100;

	/**
	 * 升序关键字
	 */
	String ASC = "asc";

	/**
	 * SQL 关键字
	 */
	Set<String> SQL_KEYWORDS = new HashSet<>(Arrays.asList("master", "truncate", "insert", "select", "delete", "update",
			"declare", "alter", "drop", "sleep"));

}

package ext.library.log.event;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 操作日志事件
 */

@Data
public class OperLogEvent implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 日志主键
	 */
	private Long id;

	/**
	 * 操作模块
	 */
	private String title;

	/**
	 * 业务类型（0 其它 1 新增 2 修改 3 删除）
	 */
	private Integer businessType;

	/**
	 * 业务类型数组
	 */
	private Integer[] businessTypes;

	/**
	 * 请求方法
	 */
	private String method;

	/**
	 * 请求方式
	 */
	private String requestMethod;

	/**
	 * 操作类别（0 其它 1 后台用户 2 手机端用户）
	 */
	private Integer operatorType;

	/** 操作人员 */
	private String operId;

	/**
	 * 请求 url
	 */
	private String operUrl;

	/**
	 * 操作地址
	 */
	private String operIp;

	/**
	 * 操作地点
	 */
	private String operLocation;

	/**
	 * 请求参数
	 */
	private String operParam;

	/**
	 * 返回参数
	 */
	private String jsonResult;

	/**
	 * 操作状态（0 正常 1 异常）
	 */
	private Integer status;

	/**
	 * 错误消息
	 */
	private String errorMsg;

	/**
	 * 操作时间
	 */
	private Date operTime;

	/**
	 * 消耗时间
	 */
	private Long costTime;

}

package ext.library.core.response;


/**
 * 响应码
 */
public interface ResponseCode {

    /**
     * 获取业务码
     *
     * @return 业务码
     */
    int getCode();

    /**
     * 获取信息
     *
     * @return 返回结构体中的信息
     */
    String getMsg();

}

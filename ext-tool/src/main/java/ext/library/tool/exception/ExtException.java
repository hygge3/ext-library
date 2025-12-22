package ext.library.tool.exception;


import ext.library.tool.util.StringUtil;

/**
 * 框架内部异常
 */
public class ExtException extends BizException {

    public ExtException(String module, String message, Object... args) {
        super("[" + module + "] " + StringUtil.format(message, args));
    }

    public ExtException(String module, Throwable cause) {
        super("[" + module + "] " + cause.getMessage(), cause);
    }

}

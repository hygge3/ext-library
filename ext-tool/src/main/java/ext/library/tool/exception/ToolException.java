package ext.library.tool.exception;


import ext.library.tool.util.StringUtil;

/**
 * 工具异常
 */
public class ToolException extends BizException {


    public ToolException(String module, String message, Object... args) {
        super("[" + module + "] " + StringUtil.format(message, args));
    }

    public ToolException(String module, Throwable cause) {
        super("[" + module + "] " + cause.getMessage(), cause);
    }

}

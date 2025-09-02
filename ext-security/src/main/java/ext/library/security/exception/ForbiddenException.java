package ext.library.security.exception;

import ext.library.tool.biz.exception.BizCode;
import ext.library.tool.biz.exception.BizException;

/**
 * 无权限
 */
public class ForbiddenException extends BizException {

    public ForbiddenException() {
        super(BizCode.FORBIDDEN);
    }

    public ForbiddenException(String msg) {
        super(BizCode.FORBIDDEN, msg);
    }

}
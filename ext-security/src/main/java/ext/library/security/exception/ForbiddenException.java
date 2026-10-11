package ext.library.security.exception;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;

/**
 * 无权限
 */
public class ForbiddenException extends ExtException {

    public ForbiddenException() {
        super(EmojiSymbol.SECURITY, "身份已验证但权限不足");
    }

    public ForbiddenException(String msg) {
        super(EmojiSymbol.SECURITY, msg);
    }

}

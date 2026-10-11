package ext.library.security.exception;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;

/**
 * 未经授权
 */
public class UnauthorizedException extends ExtException {

    public UnauthorizedException() {
        super(EmojiSymbol.SECURITY, "身份未验证");
    }

    public UnauthorizedException(String msg) {
        super(EmojiSymbol.SECURITY, msg);
    }

}

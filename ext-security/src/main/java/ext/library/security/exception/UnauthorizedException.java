package ext.library.security.exception;

import ext.library.core.exception.BizCode;
import ext.library.core.exception.BizException;

/**
 * 未经授权
 */
public class UnauthorizedException extends BizException {

	public UnauthorizedException() {
		super(BizCode.UNAUTHORIZED);
	}

	public UnauthorizedException(String msg) {
		super(BizCode.UNAUTHORIZED, msg);
	}

}

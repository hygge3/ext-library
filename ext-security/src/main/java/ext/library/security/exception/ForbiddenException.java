package ext.library.security.exception;

/**
 * 无权限
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException() {
        super("身份已验证但权限不足");
    }

    public ForbiddenException(String msg) {
        super(msg);
    }

}
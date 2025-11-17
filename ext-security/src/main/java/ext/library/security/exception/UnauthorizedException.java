package ext.library.security.exception;

/**
 * 未经授权
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("身份未验证");
    }

    public UnauthorizedException(String msg) {
        super(msg);
    }

}
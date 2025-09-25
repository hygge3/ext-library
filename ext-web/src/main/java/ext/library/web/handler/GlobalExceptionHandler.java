package ext.library.web.handler;

import ext.library.tool.biz.exception.BizCode;
import ext.library.tool.biz.exception.BizException;
import ext.library.tool.constant.Symbol;
import ext.library.tool.util.StreamUtil;
import ext.library.tool.util.StringUtil;
import ext.library.web.response.R;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentConversionNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;

/**
 * 全局异常处理器
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Order(1)
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 打印日志
     *
     * @param request 请求
     * @param message 消息
     * @param e       e
     */
    private static void printLog(@Nonnull HttpServletRequest request, String message, Exception e) {
        log.error("[🌐] URI:{},{}", request.getRequestURI(), message, e);
    }

    /**
     * 处理 BindingResult
     *
     * @param result BindingResult
     *
     * @return R
     */
    private static R<Void> handleBindingResult(@Nonnull BindingResult result) {
        FieldError error = result.getFieldError();
        String message = Symbol.EMPTY;
        if (error != null) {
            message = StringUtil.format("{}:{}", error.getField(), error.getDefaultMessage());
        } else {
            ObjectError globalError = result.getGlobalError();
            if (globalError != null) {
                message = globalError.getDefaultMessage();
            }
        }
        return R.failed(BizCode.BAD_REQUEST, message);
    }

    /**
     * 处理 ConstraintViolation
     *
     * @param violations 校验结果
     *
     * @return R
     */
    private static R<Void> handleConstraintViolation(@Nonnull Set<ConstraintViolation<?>> violations) {
        ConstraintViolation<?> violation = violations.iterator().next();
        String path = ((PathImpl) violation.getPropertyPath()).getLeafNode().getName();
        String message = StringUtil.format("{}:{}", path, violation.getMessage());
        return R.failed(BizCode.BAD_REQUEST, message);
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(BizException.class)
    public R<Void> bizException(@Nonnull BizException e, @Nonnull HttpServletRequest request) {
        log.error("[⚠️] URI:{},{}", request.getRequestURI(), e.getMessage());
        return R.failed(e.getCode(), e.getMessage());
    }

    /**
     * 应用内部异常
     */
    @ExceptionHandler(RuntimeException.class)
    public R<Void> otherException(RuntimeException e, HttpServletRequest request) {
        printLog(request, "未知错误", e);
        return R.failed(BizCode.SERVER_ERROR, BizCode.SERVER_ERROR.getMsg());
    }

    /**
     * 内部参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public R<Void> illegalArgumentException(@Nonnull IllegalArgumentException e, HttpServletRequest request) {
        String message = StringUtil.format("参数异常:{}", e.getMessage());
        printLog(request, message, e);
        return R.failed(BizCode.ILLEGAL_ARGUMENT, e.getMessage());
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Void> httpRequestMethodNotSupportedException(@Nonnull HttpRequestMethodNotSupportedException e,
                                                          HttpServletRequest request) {
        String message = StringUtil.format("({}) 未支持", e.getMethod());
        printLog(request, message, e);
        return R.failed(BizCode.METHOD_NOT_ALLOWED, message);
    }

    /**
     * 请求路径中缺少必需的路径变量
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public R<Void> missingPathVariableException(@Nonnull MissingPathVariableException e, HttpServletRequest request) {
        String message = StringUtil.format("缺少 path 变量：{}", e.getVariableName());
        printLog(request, message, e);
        return R.failed(BizCode.BAD_REQUEST, message);
    }

    /**
     * 请求参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<Void> methodArgumentTypeMismatchException(@Nonnull MethodArgumentTypeMismatchException e,
                                                       HttpServletRequest request) {
        String message = StringUtil.format("方法参数类型不匹配：{}", e.getMessage());
        printLog(request, message, e);
        return R.failed(BizCode.BAD_REQUEST, StringUtil.format("方法参数类型不匹配，参数 [{}] 要求类型为：'{}'，但输入值为：'{}'",
                e.getName(), e.getRequiredType().getName(), e.getValue()));
    }

    /**
     * 参数效验未通过统一处理。
     *
     * @param e 参数校验未通过异常
     *
     * @return 结果
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<Void> missingServletRequestParameterException(@Nonnull MissingServletRequestParameterException e, HttpServletRequest request) {
        String message = StringUtil.format("缺少请求参数：{}", e.getParameterName());
        printLog(request, message, e);
        return R.failed(BizCode.BAD_REQUEST, message);
    }

    /**
     * 433 参数效验未通过统一处理。
     *
     * @param e 参数校验未通过异常
     *
     * @return 结果
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public R<Void> handlerMethodValidationException(@Nonnull HandlerMethodValidationException e,
                                                    HttpServletRequest request) {
        String param = StreamUtil.join(e.getParameterValidationResults(), (allValidationResult) -> {
            String parameterName = allValidationResult.getMethodParameter().getParameterName();
            String defaultMessage = allValidationResult.getResolvableErrors().getFirst().getDefaultMessage();
            return parameterName + Symbol.COLON + defaultMessage;
        }, Symbol.SEMICOLON);

        String message = StringUtil.format("参数校验未通过：{}", param);
        printLog(request, message, e);
        return R.failed(BizCode.BAD_REQUEST, message);
    }

    /**
     * 404 找不到路由
     *
     * @param e       e
     * @param request 请求
     *
     * @return {@code R<Void> }
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public R<Void> noHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        printLog(request, e.getMessage(), e);
        return R.failed(BizCode.NOT_FOUND, e.getMessage());
    }

    /**
     * 处理 HTTP 消息不可读异常
     *
     * @param e       e
     * @param request 请求
     *
     * @return {@code R<Object> }
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Object> httpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        printLog(request, e.getMessage(), e);
        return R.failed(BizCode.BAD_REQUEST, e.getMessage());
    }

    /**
     * 处理 HTTP 消息转换异常
     *
     * @param e       e
     * @param request 请求
     *
     * @return {@code R<Object> }
     */
    @ExceptionHandler(HttpMessageConversionException.class)
    public R<Object> httpMessageConversionException(HttpMessageConversionException e, HttpServletRequest request) {
        printLog(request, e.getMessage(), e);
        return R.failed(BizCode.BAD_REQUEST, e.getMessage());
    }

    /**
     * 不支持方法参数转换异常
     *
     * @param e       e
     * @param request 请求
     *
     * @return {@code R<Object> }
     */
    @ExceptionHandler(MethodArgumentConversionNotSupportedException.class)
    public R<Object> methodArgumentConversionNotSupportedException(
            MethodArgumentConversionNotSupportedException e, HttpServletRequest request) {
        printLog(request, e.getMessage(), e);
        return R.failed(BizCode.BAD_REQUEST, e.getMessage());
    }

    /**
     * 处理不支持 HTTP 媒体类型异常
     *
     * @param e       e
     * @param request 请求
     *
     * @return {@code R<Object> }
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public R<Object> httpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        printLog(request, e.getMessage(), e);
        return R.failed(BizCode.BAD_REQUEST, e.getMessage());
    }

    /**
     * HTTP 媒体类型不可接受异常
     *
     * @param e       e
     * @param request 请求
     *
     * @return {@code R<Object> }
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public R<Object> httpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException e, HttpServletRequest request) {
        printLog(request, e.getMessage(), e);
        String message = e.getMessage() + Symbol.EMPTY + StringUtil.join(e.getSupportedMediaTypes());
        return R.failed(BizCode.BAD_REQUEST, message);
    }

    /**
     * 处理@Valid 参数校验失败异常
     *
     * @param e       e
     * @param request 请求
     *
     * @return {@code R<Void> }
     */
    @ExceptionHandler(BindException.class)
    public R<Void> bindException(BindException e, HttpServletRequest request) {
        // String message = StreamUtils.join(e.getAllErrors(),
        // DefaultMessageSourceResolvable::getDefaultMessage, ", ");
        printLog(request, e.getMessage(), e);
        return handleBindingResult(e.getBindingResult());
    }

    /**
     * 自定义验证异常
     *
     * @param e       e
     * @param request 请求
     *
     * @return {@code R<Void> }
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> constraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        printLog(request, e.getMessage(), e);
        // String message = StreamUtils.join(e.getConstraintViolations(),
        // ConstraintViolation::getMessage, ", ");
        return handleConstraintViolation(e.getConstraintViolations());
    }

    /**
     * 处理@Validated 参数校验失败异常
     *
     * @param e       e
     * @param request 请求
     *
     * @return {@code R<Void> }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> methodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        printLog(request, e.getMessage(), e);
        return handleBindingResult(e.getBindingResult());
    }

    /**
     * 超出最大上传大小异常
     *
     * @param e       参数校验未通过异常
     * @param request 请求
     *
     * @return 结果
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public R<Void> maxUploadSizeExceededException(@Nonnull MaxUploadSizeExceededException e, HttpServletRequest request) {
        long maxUploadSize = e.getMaxUploadSize();
        String message = StringUtil.format("超出最大上传大小，最大：{}", maxUploadSize);
        printLog(request, message, e);
        return R.failed(BizCode.BAD_REQUEST, message);
    }

}
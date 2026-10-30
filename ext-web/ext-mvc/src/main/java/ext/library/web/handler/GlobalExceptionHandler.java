package ext.library.web.handler;

import ext.library.servlet.exception.ErrorEnvelope;
import ext.library.servlet.exception.ErrorLog;
import ext.library.servlet.response.R;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.BizCode;
import ext.library.tool.exception.BizException;
import ext.library.tool.util.StreamUtil;
import ext.library.tool.util.StringUtil;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;

/**
 * 全局异常处理器
 * <p>
 * 只负责两件事：某类异常归谁管、它对外要说什么。HTTP 状态与响应体的配对交给
 * {@link ErrorEnvelope}，日志口径交给 {@link ErrorLog}。
 */
@Order(1)
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 BindingResult
     *
     * @param result BindingResult
     *
     * @return 校验失败的响应体
     */
    private static R<Void> handleBindingResult(BindingResult result) {
        FieldError error = result.getFieldError();
        String message = "";
        if (error != null) {
            message = StringUtil.format("{}:{}", error.getField(), error.getDefaultMessage());
        } else {
            ObjectError globalError = result.getGlobalError();
            if (globalError != null) {
                message = globalError.getDefaultMessage();
            }
        }
        return R.failed(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * 处理 ConstraintViolation
     *
     * @param violations 校验结果
     *
     * @return 校验失败的响应体
     */
    private static R<Void> handleConstraintViolation(Set<ConstraintViolation<?>> violations) {
        ConstraintViolation<?> violation = violations.iterator().next();
        String propertyPath = violation.getPropertyPath().toString();
        int lastDot = propertyPath.lastIndexOf('.');
        String path = lastDot < 0 ? propertyPath : propertyPath.substring(lastDot + 1);
        String message = StringUtil.format("{}:{}", path, violation.getMessage());
        return R.failed(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * 主动业务异常
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<R<Void>> bizException(BizException e, HttpServletRequest request) {
        ErrorLog.error(EmojiSymbol.WEB, request, e.getMessage());
        return ErrorEnvelope.ofCode(e.getCode(), e.getMessage()).toResponseEntity();
    }

    /**
     * 未知的应用内部异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<R<Void>> otherException(RuntimeException e, HttpServletRequest request) {
        ErrorLog.error(EmojiSymbol.WEB, request, "未知错误", e);
        return ErrorEnvelope
                .of(HttpStatus.INTERNAL_SERVER_ERROR, BizCode.UNKNOWN.getCode(), BizCode.UNKNOWN.getMsg())
                .toResponseEntity();
    }

    /**
     * 内部参数校验异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<R<Void>> illegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        String message = StringUtil.format("参数异常:{}", e.getMessage());
        ErrorLog.error(EmojiSymbol.WEB, request, message, e);
        return ErrorEnvelope
                .of(HttpStatus.INTERNAL_SERVER_ERROR, BizCode.ILLEGAL_ARGUMENT.getCode(), BizCode.ILLEGAL_ARGUMENT.getMsg())
                .toResponseEntity();
    }

    /**
     * 请求使用的方法不被允许
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<R<Void>> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e,
                                                                          HttpServletRequest request) {
        String message = StringUtil.format("({}) 未支持", e.getMethod());
        ErrorLog.error(EmojiSymbol.WEB, request, message, e);
        return ErrorEnvelope.of(HttpStatus.METHOD_NOT_ALLOWED, message).toResponseEntity();
    }

    /**
     * 请求路径中缺少必需的路径变量
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<R<Void>> missingPathVariableException(MissingPathVariableException e,
                                                                HttpServletRequest request) {
        String message = StringUtil.format("缺少 Path 变量：{}", e.getVariableName());
        ErrorLog.error(EmojiSymbol.WEB, request, message, e);
        return ErrorEnvelope.of(HttpStatus.BAD_REQUEST, message).toResponseEntity();
    }

    /**
     * 请求参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<R<Void>> methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e,
                                                                      HttpServletRequest request) {
        String message = StringUtil.format("参数 [{}] 类型不匹配，要求：'{}'，实际：'{}'",
                e.getName(), e.getRequiredType(), e.getValue());
        ErrorLog.error(EmojiSymbol.WEB, request, message, e);
        return ErrorEnvelope.of(HttpStatus.BAD_REQUEST, StringUtil.format("参数 [{}] 类型不匹配", e.getName()))
                .toResponseEntity();
    }

    /**
     * 缺少必需的请求参数
     *
     * @param e       参数校验未通过异常
     * @param request 请求
     *
     * @return 结果
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<R<Void>> missingServletRequestParameterException(MissingServletRequestParameterException e,
                                                                           HttpServletRequest request) {
        String message = StringUtil.format("缺少请求参数：{}", e.getParameterName());
        ErrorLog.error(EmojiSymbol.WEB, request, message, e);
        return ErrorEnvelope.of(HttpStatus.BAD_REQUEST, message).toResponseEntity();
    }

    /**
     * 方法参数校验未通过统一处理
     *
     * @param e       参数校验未通过异常
     * @param request 请求
     *
     * @return 结果
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<R<Void>> handlerMethodValidationException(HandlerMethodValidationException e,
                                                                    HttpServletRequest request) {
        String param = StreamUtil.join(e.getParameterValidationResults(), (allValidationResult) -> {
            String parameterName = allValidationResult.getMethodParameter().getParameterName();
            String defaultMessage = allValidationResult.getResolvableErrors().getFirst().getDefaultMessage();
            return parameterName + ":" + defaultMessage;
        }, ";");

        String message = StringUtil.format("参数校验未通过：{}", param);
        ErrorLog.error(EmojiSymbol.WEB, request, message, e);
        return ErrorEnvelope.of(HttpStatus.BAD_REQUEST, message).toResponseEntity();
    }

    /**
     * 404 找不到路由
     *
     * @param e       e
     * @param request 请求
     *
     * @return 结果
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<R<Void>> noHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        ErrorLog.error(EmojiSymbol.WEB, request, e.getMessage(), e);
        return ErrorEnvelope.of(HttpStatus.NOT_FOUND, "请求的资源不存在").toResponseEntity();
    }

    /**
     * 处理 HTTP 消息不可读异常
     *
     * @param e       e
     * @param request 请求
     *
     * @return 结果
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<R<Void>> httpMessageNotReadableException(HttpMessageNotReadableException e,
                                                                   HttpServletRequest request) {
        ErrorLog.error(EmojiSymbol.WEB, request, e.getMessage(), e);
        return ErrorEnvelope.of(HttpStatus.BAD_REQUEST, "请求体格式错误或无法解析").toResponseEntity();
    }

    /**
     * 处理 HTTP 消息转换异常
     *
     * @param e       e
     * @param request 请求
     *
     * @return 结果
     */
    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<R<Void>> httpMessageConversionException(HttpMessageConversionException e,
                                                                  HttpServletRequest request) {
        ErrorLog.error(EmojiSymbol.WEB, request, e.getMessage(), e);
        return ErrorEnvelope.of(HttpStatus.BAD_REQUEST, "请求数据转换失败").toResponseEntity();
    }

    /**
     * 不支持方法参数转换异常
     *
     * @param e       e
     * @param request 请求
     *
     * @return 结果
     */
    @ExceptionHandler(MethodArgumentConversionNotSupportedException.class)
    public ResponseEntity<R<Void>> methodArgumentConversionNotSupportedException(
            MethodArgumentConversionNotSupportedException e, HttpServletRequest request) {
        ErrorLog.error(EmojiSymbol.WEB, request, e.getMessage(), e);
        return ErrorEnvelope.of(HttpStatus.BAD_REQUEST, "参数类型转换不支持").toResponseEntity();
    }

    /**
     * 请求使用的媒体类型不被支持
     *
     * @param e       e
     * @param request 请求
     *
     * @return 结果
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<R<Void>> httpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e,
                                                                      HttpServletRequest request) {
        ErrorLog.error(EmojiSymbol.WEB, request, e.getMessage(), e);
        return ErrorEnvelope.of(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "不支持的媒体类型").toResponseEntity();
    }

    /**
     * 请求的媒体类型不可接受
     *
     * @param e       e
     * @param request 请求
     *
     * @return 结果
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<R<Void>> httpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException e,
                                                                       HttpServletRequest request) {
        String message = e.getMessage() + "，支持的媒体类型：" + StringUtil.join(e.getSupportedMediaTypes());
        ErrorLog.error(EmojiSymbol.WEB, request, message, e);
        return ErrorEnvelope.of(HttpStatus.NOT_ACCEPTABLE, "请求的媒体类型不可接受").toResponseEntity();
    }

    /**
     * 处理 @Valid 参数校验失败异常
     *
     * @param e       e
     * @param request 请求
     *
     * @return 结果
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<R<Void>> bindException(BindException e, HttpServletRequest request) {
        R<Void> result = handleBindingResult(e.getBindingResult());
        ErrorLog.error(EmojiSymbol.WEB, request, result.getMsg(), e);
        return ErrorEnvelope.of(HttpStatus.BAD_REQUEST, result.getMsg()).toResponseEntity();
    }

    /**
     * 自定义验证异常
     *
     * @param e       e
     * @param request 请求
     *
     * @return 结果
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<R<Void>> constraintViolationException(ConstraintViolationException e,
                                                                HttpServletRequest request) {
        R<Void> result = handleConstraintViolation(e.getConstraintViolations());
        ErrorLog.error(EmojiSymbol.WEB, request, result.getMsg(), e);
        return ErrorEnvelope.of(HttpStatus.BAD_REQUEST, result.getMsg()).toResponseEntity();
    }

    /**
     * 处理 @Validated 参数校验失败异常
     *
     * @param e       e
     * @param request 请求
     *
     * @return 结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<Void>> methodArgumentNotValidException(MethodArgumentNotValidException e,
                                                                   HttpServletRequest request) {
        R<Void> result = handleBindingResult(e.getBindingResult());
        ErrorLog.error(EmojiSymbol.WEB, request, result.getMsg(), e);
        return ErrorEnvelope.of(HttpStatus.BAD_REQUEST, result.getMsg()).toResponseEntity();
    }

    /**
     * 超出最大上传大小异常
     *
     * @param e       超出最大上传大小异常
     * @param request 请求
     *
     * @return 结果
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<R<Void>> maxUploadSizeExceededException(MaxUploadSizeExceededException e,
                                                                  HttpServletRequest request) {
        String message = StringUtil.format("超出最大上传大小，最大：{}", e.getMaxUploadSize());
        ErrorLog.error(EmojiSymbol.WEB, request, message, e);
        return ErrorEnvelope.of(HttpStatus.CONTENT_TOO_LARGE, message).toResponseEntity();
    }
}

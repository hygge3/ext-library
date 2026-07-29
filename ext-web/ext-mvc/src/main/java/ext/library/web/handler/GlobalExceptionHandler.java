package ext.library.web.handler;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.BizCode;
import ext.library.tool.exception.BizException;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.StreamUtil;
import ext.library.tool.util.StringUtil;
import ext.library.web.response.R;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.ResponseStatus;
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
 */
@Order(1)
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 打印日志
     *
     * @param request 请求
     * @param message 消息
     * @param e       e
     */
    private static void printLog(HttpServletRequest request, String message, Exception e) {
        Logs.error(EmojiSymbol.WARN, e, "URI:{},{}", request.getRequestURI(), message);
    }

    /**
     * 处理 BindingResult
     *
     * @param result BindingResult
     * @return R
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
     * @return R
     */
    private static R<Void> handleConstraintViolation(Set<ConstraintViolation<?>> violations) {
        ConstraintViolation<?> violation = violations.iterator().next();
        String path = violation.getPropertyPath().toString();
        String message = StringUtil.format("{}:{}", path, violation.getMessage());
        return R.failed(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * 主动业务异常
     */
    @ExceptionHandler(BizException.class)
    public R<Void> bizException(BizException e, HttpServletRequest request) {
        Logs.error(EmojiSymbol.WEB, "URI:{},{}", request.getRequestURI(), e.getMessage());
        return R.failed(e.getCode(), e.getMessage());
    }

    /**
     * 未知的应用内部异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> otherException(RuntimeException e, HttpServletRequest request) {
        printLog(request, "未知错误", e);
        return R.failed(BizCode.UNKNOWN, BizCode.UNKNOWN.getMsg());
    }

    /**
     * 内部参数校验异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> illegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        String message = StringUtil.format("参数异常:{}", e.getMessage());
        printLog(request, message, e);
        return R.failed(BizCode.ILLEGAL_ARGUMENT, BizCode.ILLEGAL_ARGUMENT.getMsg());
    }

    /**
     * 请求使用的方法不被允许
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public R<Void> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String message = StringUtil.format("({}) 未支持", e.getMethod());
        printLog(request, message, e);
        return R.failed(HttpStatus.METHOD_NOT_ALLOWED, message);
    }

    /**
     * 请求路径中缺少必需的路径变量
     */
    @ExceptionHandler(MissingPathVariableException.class)
    @ResponseStatus(HttpStatus.PRECONDITION_REQUIRED)
    public R<Void> missingPathVariableException(MissingPathVariableException e, HttpServletRequest request) {
        String message = StringUtil.format("缺少 Path 变量：{}", e.getVariableName());
        printLog(request, message, e);
        return R.failed(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * 请求参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    public R<Void> methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e,
                                                       HttpServletRequest request) {
        String message = StringUtil.format("参数 [{}] 类型不匹配，要求：'{}'，实际：'{}'",
                e.getName(), e.getRequiredType().getName(), e.getValue());
        printLog(request, message, e);
        return R.failed(HttpStatus.BAD_REQUEST, StringUtil.format("参数 [{}] 类型不匹配", e.getName()));
    }

    /**
     * 参数效验未通过统一处理
     *
     * @param e       参数校验未通过异常
     * @param request 请求
     * @return 结果
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.PRECONDITION_REQUIRED)
    public R<Void> missingServletRequestParameterException(MissingServletRequestParameterException e,
                                                           HttpServletRequest request) {
        String message = StringUtil.format("缺少请求参数：{}", e.getParameterName());
        printLog(request, message, e);
        return R.failed(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * 参数效验未通过统一处理
     *
     * @param e       参数校验未通过异常
     * @param request 请求
     * @return 结果
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.PRECONDITION_REQUIRED)
    public R<Void> handlerMethodValidationException(HandlerMethodValidationException e,
                                                    HttpServletRequest request) {
        String param = StreamUtil.join(e.getParameterValidationResults(), (allValidationResult) -> {
            String parameterName = allValidationResult.getMethodParameter().getParameterName();
            String defaultMessage = allValidationResult.getResolvableErrors().getFirst().getDefaultMessage();
            return parameterName + ":" + defaultMessage;
        }, ";");

        String message = StringUtil.format("参数校验未通过：{}", param);
        printLog(request, message, e);
        return R.failed(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * 404 找不到路由
     *
     * @param e       e
     * @param request 请求
     * @return {@code R<Void> }
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public R<Void> noHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        printLog(request, e.getMessage(), e);
        return R.failed(HttpStatus.NOT_FOUND, "请求的资源不存在");
    }

    /**
     * 处理 HTTP 消息不可读异常
     *
     * @param e       e
     * @param request 请求
     * @return {@code R<Object> }
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Object> httpMessageNotReadableException(HttpMessageNotReadableException e,
                                                     HttpServletRequest request) {
        printLog(request, e.getMessage(), e);
        return R.failed(HttpStatus.BAD_REQUEST, "请求体格式错误或无法解析");
    }

    /**
     * 处理 HTTP 消息转换异常
     *
     * @param e       e
     * @param request 请求
     * @return {@code R<Object> }
     */
    @ExceptionHandler(HttpMessageConversionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Object> httpMessageConversionException(HttpMessageConversionException e,
                                                    HttpServletRequest request) {
        printLog(request, e.getMessage(), e);
        return R.failed(HttpStatus.BAD_REQUEST, "请求数据转换失败");
    }

    /**
     * 不支持方法参数转换异常
     *
     * @param e       e
     * @param request 请求
     * @return {@code R<Object> }
     */
    @ExceptionHandler(MethodArgumentConversionNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Object> methodArgumentConversionNotSupportedException(MethodArgumentConversionNotSupportedException e,
                                                                   HttpServletRequest request) {
        printLog(request, e.getMessage(), e);
        return R.failed(HttpStatus.BAD_REQUEST, "参数类型转换不支持");
    }

    /**
     * 请求使用的媒体类型不被支持
     *
     * @param e       e
     * @param request 请求
     * @return {@code R<Object> }
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public R<Object> httpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e,
                                                        HttpServletRequest request) {
        printLog(request, e.getMessage(), e);
        return R.failed(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "不支持的媒体类型");
    }

    /**
     * 请求的媒体类型不可接受
     *
     * @param e       e
     * @param request 请求
     * @return {@code R<Object> }
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public R<Object> httpMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException e,
                                                         HttpServletRequest request) {
        String message = e.getMessage() + "，支持的媒体类型：" + StringUtil.join(e.getSupportedMediaTypes());
        printLog(request, message, e);
        return R.failed(HttpStatus.NOT_ACCEPTABLE, "请求的媒体类型不可接受");
    }

    /**
     * 处理 @Valid 参数校验失败异常
     *
     * @param e       e
     * @param request 请求
     * @return {@code R<Void> }
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.PRECONDITION_REQUIRED)
    public R<Void> bindException(BindException e, HttpServletRequest request) {
        R<Void> result = handleBindingResult(e.getBindingResult());
        printLog(request, result.getMsg(), e);
        return result;
    }

    /**
     * 自定义验证异常
     *
     * @param e       e
     * @param request 请求
     * @return {@code R<Void> }
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.PRECONDITION_REQUIRED)
    public R<Void> constraintViolationException(ConstraintViolationException e,
                                                HttpServletRequest request) {
        R<Void> result = handleConstraintViolation(e.getConstraintViolations());
        printLog(request, result.getMsg(), e);
        return result;
    }

    /**
     * 处理 @Validated 参数校验失败异常
     *
     * @param e       e
     * @param request 请求
     * @return {@code R<Void> }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> methodArgumentNotValidException(MethodArgumentNotValidException e,
                                                   HttpServletRequest request) {
        R<Void> result = handleBindingResult(e.getBindingResult());
        printLog(request, result.getMsg(), e);
        return result;
    }

    /**
     * 超出最大上传大小异常
     *
     * @param e       超出最大上传大小异常
     * @param request 请求
     * @return 结果
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
    public R<Void> maxUploadSizeExceededException(MaxUploadSizeExceededException e,
                                                  HttpServletRequest request) {
        long maxUploadSize = e.getMaxUploadSize();
        String message = StringUtil.format("超出最大上传大小，最大：{}", maxUploadSize);
        printLog(request, message, e);
        return R.failed(HttpStatus.CONTENT_TOO_LARGE, message);
    }
}
package ext.library.log.aspect;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import ext.library.core.util.ServletUtil;
import ext.library.core.util.SpringUtil;
import ext.library.json.util.JsonUtil;
import ext.library.log.annotation.LogRecord;
import ext.library.log.enums.BusinessStatus;
import ext.library.log.event.OperLogEvent;
import ext.library.security.domain.SecurityToken;
import ext.library.security.util.SecurityUtil;
import ext.library.tool.$;
import ext.library.tool.util.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.util.StopWatch;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * 操作日志记录处理
 */
@Slf4j
@Aspect
@AutoConfiguration
public class LogAspect {

    /**
     * 排除敏感属性字段
     */
    public static final String[] EXCLUDE_PROPERTIES = {"password", "oldPassword", "newPassword", "confirmPassword"};

    /**
     * 计时 key
     */
    private static final ThreadLocal<StopWatch> KEY_CACHE = new ThreadLocal<>();

    /**
     * 处理请求前执行
     */
    @Before(value = "@annotation(controllerLog)")
    public void doBefore(JoinPoint joinPoint, LogRecord controllerLog) {
        StopWatch stopWatch = new StopWatch();
        KEY_CACHE.set(stopWatch);
        stopWatch.start();
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, LogRecord controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, LogRecord controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, LogRecord controllerLog, final Exception e, Object jsonResult) {
        try {
            HttpServletRequest request = ServletUtil.getRequest();
            // *========数据库日志=========*//
            OperLogEvent operLog = new OperLogEvent();
            operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
            // 请求的地址
            String ip = ServletUtil.getIpAddr(request);
            operLog.setOperIp(ip);
            operLog.setOperUrl(request.getRequestURI().substring(0, 255));

            SecurityToken currentToken = SecurityUtil.getCurrentToken();
            operLog.setOperId(currentToken.getLoginId());

            if (e != null) {
                operLog.setStatus(BusinessStatus.FAIL.ordinal());
                operLog.setErrorMsg(e.getMessage().substring(0, 2000));
            }
            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");
            // 设置请求方式
            operLog.setRequestMethod(request.getMethod());
            // 处理设置注解上的参数
            getControllerMethodDescription(joinPoint, controllerLog, operLog, jsonResult);
            // 设置消耗时间
            StopWatch stopWatch = KEY_CACHE.get();
            stopWatch.stop();
            operLog.setCostTime(stopWatch.getTotalTimeMillis());
            // 发布事件保存数据库
            SpringUtil.getContext().publishEvent(operLog);
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("Exception:{}", exp.getMessage(), e);
        } finally {
            KEY_CACHE.remove();
        }
    }

    /**
     * 获取注解中对方法的描述信息 用于 Controller 层注解
     *
     * @param joinPoint  连接点
     * @param log        日志
     * @param operLog    操作日志
     * @param jsonResult json 结果
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, LogRecord log, OperLogEvent operLog, Object jsonResult) {
        // 设置 action 动作
        operLog.setBusinessType(log.businessType().ordinal());
        // 设置标题
        operLog.setTitle(log.title());
        // 设置操作人类别
        operLog.setOperatorType(log.operatorType().ordinal());
        // 是否需要保存 request，参数和值
        if (log.isSaveRequestData()) {
            // 获取参数的信息，传入到数据库中。
            setRequestValue(joinPoint, operLog, log.excludeParamNames());
        }
        // 是否需要保存 response，参数和值
        if (log.isSaveResponseData() && Objects.nonNull(jsonResult)) {
            operLog.setJsonResult(JsonUtil.toJson(jsonResult).substring(0, 2000));
        }
    }

    /**
     * 获取请求的参数，放到 log 中
     *
     * @param operLog 操作日志
     */
    private void setRequestValue(JoinPoint joinPoint, OperLogEvent operLog, String[] excludeParamNames) {
        Map<String, String> paramsMap = ServletUtil.getParamMap(ServletUtil.getRequest());
        String requestMethod = operLog.getRequestMethod();
        if ($.isEmpty(paramsMap) && HttpMethod.PUT.name().equals(requestMethod)
            || HttpMethod.POST.name().equals(requestMethod)) {
            String params = argsArrayToString(joinPoint.getArgs(), excludeParamNames);
            operLog.setOperParam(params.substring(0, 2000));
        } else {
            MapUtil.removeAny(paramsMap, EXCLUDE_PROPERTIES);
            MapUtil.removeAny(paramsMap, excludeParamNames);
            operLog.setOperParam(JsonUtil.toJson(paramsMap).substring(0, 2000));
        }
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray, String[] excludeParamNames) {
        StringJoiner params = new StringJoiner(" ");
        if ($.isEmpty(paramsArray)) {
            return params.toString();
        }
        for (Object o : paramsArray) {
            if (Objects.nonNull(o) && !isFilterObject(o)) {
                String str = JsonUtil.toJson(o);
                Map<String, Object> dict = JsonUtil.readMap(str);
                if ($.isNotEmpty(dict)) {
                    MapUtil.removeAny(dict, EXCLUDE_PROPERTIES);
                    MapUtil.removeAny(dict, excludeParamNames);
                    str = JsonUtil.toJson(dict);
                }
                params.add(str);
            }
        }
        return params.toString();
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回 true；否则返回 false。
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return MultipartFile.class.isAssignableFrom(clazz.getComponentType());
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.values()) {
                return value instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
               || o instanceof BindingResult;
    }

}

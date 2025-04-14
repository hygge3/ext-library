package ext.library.mybatis.handler;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;

import com.mybatisflex.core.exception.MybatisFlexException;
import ext.library.core.exception.BizCode;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Mybatis 异常处理器
 */
@Slf4j
@Order(0)
@AutoConfiguration
@RestControllerAdvice
public class MybatisExceptionHandler {

    /**
     * 打印日志
     *
     * @param request 请求
     * @param message 消息
     * @param e       e
     */
    private static void printLog(@Nonnull HttpServletRequest request, String message, Exception e) {
        log.error("[⚠️] URI:{},{}", request.getRequestURI(), message, e);
    }

    /**
     * 主键或 UNIQUE 索引，数据重复异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Map<String, Object> duplicateKeyException(DuplicateKeyException e, HttpServletRequest request) {
        printLog(request, "数据库中已存在记录", e);
        return Map.of("code", BizCode.DATABASE_ERROR.getCode(), "msg", "数据库中已存在该记录。请联系管理员确认");
    }

    /**
     * SQL 语法错误异常
     */
    @ExceptionHandler(SQLSyntaxErrorException.class)
    public Map<String, Object> sqlSyntaxErrorException(SQLSyntaxErrorException e, HttpServletRequest request) {
        printLog(request, "SQl error", e);
        return Map.of("code", BizCode.DATABASE_ERROR.getCode(), "msg", "SQl 执行错误，请联系管理员");
    }

    /**
     * SQL 语法错误异常
     */
    @ExceptionHandler(BadSqlGrammarException.class)
    public Map<String, Object> badSqlGrammarException(BadSqlGrammarException e, HttpServletRequest request) {
        printLog(request, "SQl error", e);
        return Map.of("code", BizCode.DATABASE_ERROR.getCode(), "msg", "SQl 执行错误，请联系管理员");
    }

    /**
     * Mybatis 系统异常 通用处理
     */
    @ExceptionHandler(MyBatisSystemException.class)
    public Map<String, Object> myBatisSystemException(@Nonnull MyBatisSystemException e, HttpServletRequest request) {
        String message = e.getMessage();
        if ("CannotFindDataSourceException".contains(message)) {
            printLog(request, "未找到数据源", e);
            return Map.of("code", BizCode.DATABASE_ERROR.getCode(), "msg", "找不到数据源，请联系管理员确认");
        }
        printLog(request, "Mybatis exception", e);
        return Map.of("code", BizCode.DATABASE_ERROR.getCode(), "msg", "Mybatis exception");
    }

    /**
     * Mybatis Flex 系统异常 通用处理
     */
    @ExceptionHandler(MybatisFlexException.class)
    public Map<String, Object> mybatisFlexException(@Nonnull MybatisFlexException e, HttpServletRequest request) {
        printLog(request, "Mybatis Flex exception", e);
        return Map.of("code", BizCode.DATABASE_ERROR.getCode(), "msg", "Mybatis Flex exception");
    }

    /**
     * SQL 系统异常 通用处理
     */
    @ExceptionHandler(SQLException.class)
    public Map<String, Object> sQLException(@Nonnull SQLException e, HttpServletRequest request) {
        printLog(request, "Mybatis Flex exception", e);
        return Map.of("code", BizCode.DATABASE_ERROR.getCode(), "msg", "Mybatis Flex exception");
    }

    /**
     * 数据访问异常 通用处理
     */
    @ExceptionHandler(DataAccessException.class)
    public Map<String, Object> dataAccessException(DataAccessException e, HttpServletRequest request) {
        printLog(request, "Data access exception", e);
        return Map.of("code", BizCode.DATABASE_ERROR.getCode(), "msg", "Data access exception");
    }

}

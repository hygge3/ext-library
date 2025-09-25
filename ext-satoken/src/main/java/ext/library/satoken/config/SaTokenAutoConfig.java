package ext.library.satoken.config;

import ext.library.satoken.handler.SaTokenExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * sa-token 配置
 */
@AutoConfiguration
public class SaTokenAutoConfig {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 异常处理器
     */
    @Bean
    public SaTokenExceptionHandler saTokenExceptionHandler() {
        log.info("[🛡️] SaToken 模块载入成功");
        return new SaTokenExceptionHandler();
    }

}
package ext.library.satoken.config;

import ext.library.satoken.handler.SaTokenExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * sa-token 配置
 */
@Slf4j
@AutoConfiguration
public class SaTokenAutoConfig {

    /**
     * 异常处理器
     */
    @Bean
    public SaTokenExceptionHandler saTokenExceptionHandler() {
        log.info("[🛡️] SaToken 模块载入成功");
        return new SaTokenExceptionHandler();
    }

}
package ext.library.satoken.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.stp.StpInterface;
import ext.library.satoken.dao.PlusSaTokenDao;
import ext.library.satoken.handler.SaTokenExceptionHandler;
import ext.library.satoken.service.SaPermissionImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * sa-token 配置
 */
@AutoConfiguration
public class SaTokenAutoConfig {

	/**
	 * 权限接口实现 (使用 bean 注入方便用户替换)
	 */
	@Bean
	public StpInterface stpInterface() {
		return new SaPermissionImpl();
	}

	/**
	 * 自定义 dao 层存储
	 */
	@Bean
	public SaTokenDao saTokenDao() {
		return new PlusSaTokenDao();
	}

	/**
	 * 异常处理器
	 */
	@Bean
	public SaTokenExceptionHandler saTokenExceptionHandler() {
		return new SaTokenExceptionHandler();
	}

}

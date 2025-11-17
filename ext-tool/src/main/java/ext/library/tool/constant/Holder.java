package ext.library.tool.constant;

import ext.library.tool.domain.SnowflakeId;
import ext.library.tool.holder.retry.SimpleRetry;

import java.security.SecureRandom;
import java.util.Random;

/**
 * 一些常用的单例对象
 */
public interface Holder {

    /**
     * cpu 核心数
     */
    int CPU_CORE_NUM = Runtime.getRuntime().availableProcessors();

    /**
     * RANDOM
     */
    Random RANDOM = new Random();

    /**
     * SECURE_RANDOM
     */
    SecureRandom SECURE_RANDOM = new SecureRandom();

    /** 简单重试 */
    SimpleRetry SIMPLE_RETRY = new SimpleRetry();

    /** Twitter 的 Snowflake 算法实现 */
    SnowflakeId SNOWFLAKE_ID = new SnowflakeId(0, 0);

}
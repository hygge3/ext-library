package ext.library.tool.constant;

import java.security.SecureRandom;
import java.util.Random;

import ext.library.tool.domain.ObjectId;
import ext.library.tool.domain.SnowflakeId;
import ext.library.tool.domain.ULID;

/**
 * 一些常用的单例对象
 */
public interface Holder {

    /**
     * RANDOM
     */
    Random RANDOM = new Random();

    /**
     * SECURE_RANDOM
     */
    SecureRandom SECURE_RANDOM = new SecureRandom();

    /** 通用唯一按字典排序的标识符 */
    ULID ULID = new ULID();

    /** MongoDB 的 ObjectId */
    ObjectId OBJECT_ID = new ObjectId();

    /** Twitter 的 Snowflake 算法实现 */
    SnowflakeId SNOWFLAKE_ID = new SnowflakeId(0, 0);

}

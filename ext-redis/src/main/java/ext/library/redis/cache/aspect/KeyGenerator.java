package ext.library.redis.cache.aspect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import ext.library.core.util.spel.SpelUtil;
import ext.library.redis.config.properties.RedisPropertiesHolder;
import ext.library.tool.$;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 缓存 key 的生成工具类，主要用于解析 spel, 进行拼接 key 的生成
 *
 * @author Hccake 2019/9/3 9:58
 */
public class KeyGenerator {

    /**
     * SpEL 上下文
     */
    StandardEvaluationContext spelContext;

    public KeyGenerator(Object target, Method method, Object[] arguments) {
        this.spelContext = SpelUtil.getSpelContext(target, method, arguments);
    }

    /**
     * 根据 keyPrefix 和 keyJoint 获取完整的 key 信息
     *
     * @param keyPrefix key 前缀
     * @param keyJoint  key 拼接元素，值为 spel 表达式，可为空
     * @return 拼接完成的 key
     */
    public String getKey(String keyPrefix, String keyJoint) {
        // 根据 keyJoint 判断是否需要拼接
        if ($.isBlank(keyJoint)) {
            return keyPrefix;
        }
        // 获取所有需要拼接的元素，组装进集合中
        String joint = SpelUtil.parseValueToString(this.spelContext, keyJoint);
        Assert.notNull(joint, "keyJoint 不能为空");

        if (!StringUtils.hasText(keyPrefix)) {
            return joint;
        }
        // 拼接后返回
        return jointKey(keyPrefix, joint);
    }

    public List<String> getKeys(String keyPrefix, String keyJoint) {
        // keyJoint 必须有值
        Assert.hasText(keyJoint, "[getKeys] keyJoint 不能为空");

        // 获取所有需要拼接的元素，组装进集合中
        List<String> joints = SpelUtil.parseValueToStringList(this.spelContext, keyJoint);
        Assert.notEmpty(joints, "[getKeys] keyJoint 必须解析为非空集合");

        if (!StringUtils.hasText(keyPrefix)) {
            return joints;
        }
        // 拼接后返回
        return joints.stream().map(x -> jointKey(keyPrefix, x)).collect(Collectors.toList());
    }

    /**
     * 拼接 key, 默认使用：作为分隔符
     *
     * @param keyItems 用于拼接 key 的元素列表
     * @return 拼接完成的 key
     */
    public String jointKey(List<String> keyItems) {
        return String.join(RedisPropertiesHolder.delimiter(), keyItems);
    }

    /**
     * 拼接 key, 默认使用：作为分隔符
     *
     * @param keyItems 用于拼接 key 的元素列表
     * @return 拼接完成的 key
     */
    public String jointKey(String... keyItems) {
        return jointKey(Arrays.asList(keyItems));
    }

}

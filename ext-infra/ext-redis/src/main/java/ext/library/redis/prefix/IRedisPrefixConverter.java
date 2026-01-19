package ext.library.redis.prefix;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.core.Logs;
import ext.library.tool.util.ObjectUtil;
import ext.library.tool.util.StringUtil;

import java.nio.charset.StandardCharsets;

/**
 * redis key 前缀生成器
 */
public interface IRedisPrefixConverter {

    /**
     * 生成前缀
     *
     * @return 前缀
     */
    String prefix();

    /**
     * 前置匹配，是否走添加前缀规则
     *
     * @return 是否匹配
     */
    boolean match();

    /**
     * 去除 key 前缀
     *
     * @param bytes key 字节数组
     *
     * @return 原始 key
     */
    default byte[] unwrap(byte[] bytes) {
        if (!match() || ObjectUtil.isEmpty(bytes)) {
            return bytes;
        }
        String prefix = prefix();
        if (StringUtil.isBlank(prefix)) {
            Logs.warn(EmojiSymbol.REDIS, "前缀转换器已启用，但 getPrefix 方法返回空白结果，请检查您的实现");
            return bytes;
        }
        byte[] prefixBytes = prefix.getBytes(StandardCharsets.UTF_8);
        int prefixLen = prefixBytes.length;
        int wrapLen = bytes.length;
        int originLen = wrapLen - prefixLen;
        byte[] originBytes = new byte[originLen];
        System.arraycopy(bytes, prefixLen, originBytes, 0, originLen);
        return originBytes;
    }

    /**
     * 给 key 加上固定前缀
     *
     * @param bytes 原始 key 字节数组
     *
     * @return 加前缀之后的 key
     */
    default byte[] wrap(byte[] bytes) {
        if (!match() || bytes == null || bytes.length == 0) {
            return bytes;
        }
        String prefix = prefix();
        if (StringUtil.isBlank(prefix)) {
            Logs.warn(EmojiSymbol.REDIS, "前缀转换器已启用，但 getPrefix 方法返回空白结果，请检查您的实现");
            return bytes;
        }
        byte[] prefixBytes = prefix.getBytes(StandardCharsets.UTF_8);
        int prefixLen = prefixBytes.length;
        int originLen = bytes.length;
        byte[] wrapBytes = new byte[prefixLen + originLen];
        System.arraycopy(prefixBytes, 0, wrapBytes, 0, prefixLen);
        System.arraycopy(bytes, 0, wrapBytes, prefixLen, originLen);
        return wrapBytes;
    }

}

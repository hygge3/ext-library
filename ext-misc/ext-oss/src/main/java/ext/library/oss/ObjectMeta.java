package ext.library.oss;

/**
 * 对象元信息
 * <p>
 * {@code stat} 的结果：只有大小、内容类型与修改时间，<b>不含</b>对象内容——想拿内容用
 * {@link ObjectStorage#get(String)}，别为了看一眼大小去下载整个对象。
 *
 * @param bucket       所属存储桶（没有桶概念的实现填配置里的桶名）
 * @param key          对象键
 * @param size         字节数
 * @param contentType  内容类型
 * @param lastModified 最后修改时间（ISO-8601 字符串，后端拿不到时为空串）
 * @since 4.1.0
 */
public record ObjectMeta(String bucket, String key, long size, String contentType, String lastModified) {
}

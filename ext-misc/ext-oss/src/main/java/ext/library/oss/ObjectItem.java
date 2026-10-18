package ext.library.oss;

/**
 * 对象条目
 *
 * @param key          对象键
 * @param size         字节数
 * @param lastModified 最后修改时间
 * @since 4.1.0
 */
public record ObjectItem(String key, long size, String lastModified) {
}

package ext.library.oss;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * 对象存储中的对象
 * <p>
 * 实现了 {@link AutoCloseable}，调用方必须关闭流，否则底层连接不会释放。
 *
 * @param bucket      所属存储桶
 * @param key         对象键
 * @param contentType 内容类型
 * @param size        字节数，未知时为 -1
 * @param content     内容流
 * @since 4.1.0
 */
public record OssObject(String bucket, String key, String contentType, long size, InputStream content)
        implements AutoCloseable {

    /**
     * 关闭内容流
     */
    @Override
    public void close() {
        try {
            content.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}

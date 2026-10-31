package ext.library.servlet.util;

import org.jspecify.annotations.Nullable;
import org.springframework.util.StreamUtils;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 请求体可重复读包装
 * <p>
 * servlet 的请求体是一条只能读一次的数据流：谁先读，谁之后的调用方就只能拿到空 body。
 * 解密、审计记入参、幂等指纹计算这类横切需求因此会互相踩。本包装在第一次访问时把 body
 * 缓存到内存，之后 {@link #getInputStream()} / {@link #getReader()} 每次都从头读。
 * <p>
 * 代价是整段 body 驻留内存，只应在确实有多个读取方的请求上使用
 * （见 {@code ext.servlet.repeatable-request.enabled}）。
 */
public class RepeatableRequestWrapper extends HttpServletRequestWrapper {

    /** 缓存的请求体 */
    private final byte[] body;

    /**
     * 包装请求并缓存请求体
     *
     * @param request 原始请求
     *
     * @throws IOException 读取请求体失败
     */
    public RepeatableRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.body = StreamUtils.copyToByteArray(request.getInputStream());
    }

    /**
     * 缓存的请求体
     *
     * @return 请求体字节
     */
    public byte[] getBody() {
        return body.clone();
    }

    /**
     * 以字符串形式获取请求体
     *
     * @return 请求体字符串
     */
    public String getBodyAsString() {
        return new String(body, charset());
    }

    @Override
    public ServletInputStream getInputStream() {
        return new ByteArrayServletInputStream(body);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), charset()));
    }

    @Override
    public int getContentLength() {
        return body.length;
    }

    @Override
    public long getContentLengthLong() {
        return body.length;
    }

    private Charset charset() {
        String encoding = getCharacterEncoding();
        return encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding);
    }

    /**
     * 基于字节数组的输入流
     */
    private static final class ByteArrayServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream delegate;

        private ByteArrayServletInputStream(byte[] body) {
            this.delegate = new ByteArrayInputStream(body);
        }

        @Override
        public int read() {
            return delegate.read();
        }

        @Override
        public int read(byte @Nullable [] b, int off, int len) {
            return delegate.read(b, off, len);
        }

        @Override
        public boolean isFinished() {
            return delegate.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(@Nullable ReadListener readListener) {
            throw new UnsupportedOperationException("同步读取模式下不支持 ReadListener");
        }
    }
}

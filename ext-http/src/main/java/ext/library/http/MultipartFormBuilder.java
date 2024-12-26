package ext.library.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import ext.library.tool.core.Exceptions;
import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;

/**
 * 表单构造器
 *
 */
public class MultipartFormBuilder {

    private final HttpRequest request;

    private final MultipartBody.Builder formBuilder;

    MultipartFormBuilder(HttpRequest request) {
        this.request = request;
        this.formBuilder = new MultipartBody.Builder();
    }

    public MultipartFormBuilder add(String name, Object value) {
        this.formBuilder.addFormDataPart(name, HttpRequest.handleValue(value));
        return this;
    }

    public MultipartFormBuilder addMap(Map<String, Object> formMap) {
        if (formMap != null && !formMap.isEmpty()) {
            formMap.forEach(this::add);
        }
        return this;
    }

    public MultipartFormBuilder add(String name, @NotNull File file) {
        String fileName = file.getName();
        return add(name, fileName, file);
    }

    public MultipartFormBuilder add(String name, String filename, File file) {
        RequestBody fileBody = RequestBody.create(file, null);
        return add(name, filename, fileBody);
    }

    public MultipartFormBuilder add(String name, String filename, byte[] bytes) {
        RequestBody fileBody = RequestBody.create(bytes, null);
        return add(name, filename, fileBody);
    }

    public MultipartFormBuilder add(String name, String filename, InputStream stream) {
        try (Buffer buffer = new Buffer()) {
            buffer.readFrom(stream);
            return add(name, filename, buffer.readByteArray());
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        } finally {
            Util.closeQuietly(stream);
        }
    }

    public MultipartFormBuilder add(String name, String filename, RequestBody fileBody) {
        this.formBuilder.addFormDataPart(name, filename, fileBody);
        return this;
    }

    public MultipartFormBuilder add(RequestBody body) {
        this.formBuilder.addPart(body);
        return this;
    }

    public MultipartFormBuilder add(Headers headers, RequestBody body) {
        this.formBuilder.addPart(headers, body);
        return this;
    }

    public MultipartFormBuilder add(MultipartBody.Part part) {
        this.formBuilder.addPart(part);
        return this;
    }

    public MultipartFormBuilder addList(@NotNull List<MultipartBody.Part> partList) {
        for (MultipartBody.Part part : partList) {
            this.formBuilder.addPart(part);
        }
        return this;
    }

    public MultipartFormBuilder add(@NotNull Consumer<MultipartBody.Builder> consumer) {
        consumer.accept(this.formBuilder);
        return this;
    }

    public HttpRequest build() {
        return this.request.multipartForm(this.formBuilder.setType(MultipartBody.FORM).build());
    }

    public Exchange execute() {
        return this.build().execute();
    }

    public AsyncExchange async() {
        return this.build().async();
    }

}

package ext.library.http;

import java.util.Map;
import java.util.function.Consumer;

import okhttp3.FormBody;

/**
 * 表单构造器
 */
public class FormBuilder {

    private final HttpRequest request;

    private final FormBody.Builder formBuilder;

    FormBuilder(HttpRequest request) {
        this.request = request;
        this.formBuilder = new FormBody.Builder();
    }

    public FormBuilder add(String name, Object value) {
        this.formBuilder.add(name, HttpRequest.handleValue(value));
        return this;
    }

    public FormBuilder addMap(Map<String, Object> formMap) {
        if (formMap != null && !formMap.isEmpty()) {
            formMap.forEach(this::add);
        }
        return this;
    }

    public FormBuilder addEncoded(String name, Object encodedValue) {
        this.formBuilder.addEncoded(name, HttpRequest.handleValue(encodedValue));
        return this;
    }

    public FormBuilder add(Consumer<FormBody.Builder> consumer) {
        consumer.accept(this.formBuilder);
        return this;
    }

    public HttpRequest build() {
        return this.request.form(this.formBuilder.build());
    }

    public Exchange execute() {
        return this.build().execute();
    }

    public AsyncExchange async() {
        return this.build().async();
    }

}

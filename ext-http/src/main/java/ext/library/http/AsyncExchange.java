package ext.library.http;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

import okhttp3.Call;
import okhttp3.Request;
import org.jetbrains.annotations.Contract;

/**
 * 异步执行器
 */
@ParametersAreNonnullByDefault
public class AsyncExchange {

	private final Call call;

	private Consumer<ResponseSpec> successConsumer;

	private Consumer<ResponseSpec> responseConsumer;

	private BiConsumer<Request, HttpException> failedBiConsumer;

	@Contract(pure = true)
	AsyncExchange(Call call) {
		this.call = call;
		this.successConsumer = null;
		this.responseConsumer = null;
		this.failedBiConsumer = null;
	}

	public void onSuccessful(Consumer<ResponseSpec> consumer) {
		this.successConsumer = consumer;
		this.execute();
	}

	public void onResponse(Consumer<ResponseSpec> consumer) {
		this.responseConsumer = consumer;
		this.execute();
	}

	public AsyncExchange onFailed(BiConsumer<Request, HttpException> biConsumer) {
		this.failedBiConsumer = biConsumer;
		return this;
	}

	private void execute() {
		call.enqueue(new AsyncCallback(this));
	}

	protected void onResponse(HttpResponse response) {
		if (responseConsumer != null) {
			responseConsumer.accept(response);
		}
	}

	protected void onSuccessful(HttpResponse response) {
		if (successConsumer != null) {
			successConsumer.accept(response);
		}
	}

	protected void onFailure(Request request, IOException e) {
		if (failedBiConsumer != null) {
			failedBiConsumer.accept(request, new HttpException(request, e));
		}
	}

	protected void onFailure(HttpResponse response) {
		if (failedBiConsumer != null) {
			failedBiConsumer.accept(response.rawRequest(), new HttpException(response));
		}
	}

}

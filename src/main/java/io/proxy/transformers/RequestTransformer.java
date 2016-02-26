package io.proxy.transformers;

import io.netty.buffer.ByteBuf;
import rx.Observable;
import rx.Observable.Transformer;

public class RequestTransformer implements Transformer<ByteBuf, String> {

	public RequestTransformer() {
	}

	@Override
	public Observable<String> call(Observable<ByteBuf> t) {
		return t.map(t1 -> {
			// compose object from bytebuf
			return "{\"a\":1 \"b\":\"2\"}";
		});
	}
}

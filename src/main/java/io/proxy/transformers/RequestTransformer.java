package io.proxy.transformers;

import io.netty.buffer.ByteBuf;
import rx.Observable;
import rx.Observable.Transformer;
import rx.functions.Func1;

public class RequestTransformer implements Transformer<ByteBuf, String> {

	public RequestTransformer() {
	}

	@Override
	public Observable<String> call(Observable<ByteBuf> t) {
		return t.map(new Func1<ByteBuf, String>() {
			@Override
			public String call(ByteBuf t) {
				// compose object from bytebuf
				return "{\"a\":1 \"b\":\"2\"}";
			}
		});
	}
}

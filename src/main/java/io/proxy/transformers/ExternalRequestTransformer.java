package io.proxy.transformers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Observable.Transformer;
import rx.functions.Func1;

public class ExternalRequestTransformer implements Transformer<String, String> {

	private static final Logger	LOG	= LoggerFactory.getLogger(ExternalRequestTransformer.class);

	@Override
	public Observable<String> call(Observable<String> t) {
		return t.map(new Func1<String, String>() {
			@Override
			public String call(String t) {
				// StringBuffer sb = new StringBuffer("/v1/api?source=proxy");
				// append some values to string according to data extracted from t
				// sb.append("&country=").append("US");
				// sb.append("&ip=").append("8.8.8.8");
				// ....
				// return sb.toString();
				/**
				 * for test, call a static json file
				 */
				return "/static.json";
			}
		});
	}
}

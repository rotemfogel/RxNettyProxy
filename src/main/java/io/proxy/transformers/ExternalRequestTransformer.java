package io.proxy.transformers;

import rx.Observable;
import rx.Observable.Transformer;

public class ExternalRequestTransformer implements Transformer<String, String> {

	@Override
	public Observable<String> call(Observable<String> t) {
		return t.map(t1 -> {
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
		});
	}
}

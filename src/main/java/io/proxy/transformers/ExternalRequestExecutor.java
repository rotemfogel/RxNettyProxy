package io.proxy.transformers;

import static io.proxy.utils.StringUtils.isEmpty;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.proxy.model.ResponseData;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;

import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Observable.Transformer;
import rx.functions.Func1;

public class ExternalRequestExecutor implements Transformer<String, ResponseData> {

	private final static Logger					LOG					= LoggerFactory
																			.getLogger(ExternalRequestExecutor.class);
	private final HttpClient<ByteBuf, ByteBuf>	client;
	
	public ExternalRequestExecutor(final HttpClient<ByteBuf, ByteBuf> client) {
		this.client = client;
	}

	@Override
	public Observable<ResponseData> call(Observable<String> t) {
		return t.map(new Func1<String, ResponseData>() {
			@Override
			public ResponseData call(String req) {
				StringBuilder sResp = new StringBuilder();
				ResponseData rData = null;
				long start = 0, end = 0;
				final AtomicInteger counter = new AtomicInteger(0);
				try {
					start = System.currentTimeMillis();
					final HttpClientRequest<ByteBuf> request = HttpClientRequest.create(
							HttpVersion.HTTP_1_1, HttpMethod.GET, req);
					request.getHeaders().set(HttpHeaders.Names.CONNECTION,
							HttpHeaders.Values.KEEP_ALIVE);
					client.submit(request)
							.timeout(50, TimeUnit.MILLISECONDS)
							.flatMap(HttpClientResponse::getContent)
							.map(buf -> buf.toString(Charset.defaultCharset()))
							.doOnError((e) -> { if (e instanceof java.util.concurrent.TimeoutException) counter.incrementAndGet(); })
							.forEach(s -> sResp.append(s));
					if (counter.get() > 0) throw new TimeoutException();
					end = System.currentTimeMillis() - start;
					if (isEmpty(sResp)) {
						throw new NullPointerException();
					}
					else {
						rData = ResponseData.parse(sResp.toString());
						if (!rData.hasP()) throw new NullPointerException();
					}
					LOG.debug("external server response {}", rData);
					return rData;
				} catch (TimeoutException to) {
					LOG.warn("timeout calling to external service");
				} catch (NullPointerException | NoSuchElementException nse) {
					LOG.warn("empty response after {}ms", end > 0 ? end : System.currentTimeMillis() - start);
				} catch (Exception e) {
					LOG.error("error calling external server");
				}
				return null;
			}
		});
	}
}

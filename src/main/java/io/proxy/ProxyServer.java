package io.proxy;

import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerBuilder;
import rx.Observable;
import rx.Observable.Transformer;
import rx.functions.Func1;

class ProxyServer {
	private final static Logger LOG = LoggerFactory.getLogger(ProxyServer.class);

	private boolean isEmpty(CharSequence c) {
		return c == null || c.length() == 0;
	}

	class RequestExecutor implements Transformer<ByteBuf, String> {
		private final HttpClient<String, ByteBuf> client;

		public RequestExecutor() {
			client = RxNetty.<String, ByteBuf> createHttpClient("127.0.0.1", 80, null);
			LOG.info("created HttpClient");
		}

		@Override
		public Observable<String> call(Observable<ByteBuf> t) {
			return t.map(new Func1<ByteBuf, String>() {
				@Override
				public String call(ByteBuf t) {
					String resp;
					long start = 0, end = 0;
					final HttpClientRequest<String> request = HttpClientRequest.create(HttpVersion.HTTP_1_1,
							HttpMethod.GET, "/static.json");
					request.getHeaders().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
					try {
						start = System.currentTimeMillis();
						resp = client.submit(request).flatMap(response -> {
							return response.getContent().<String> map(content -> {
								return content.toString(Charset.defaultCharset());
							});
						}).toBlocking().toFuture().get(10, TimeUnit.MILLISECONDS);
						end = System.currentTimeMillis() - start;
						// support both empty bidderResponse and one containing only userId
						if (isEmpty(resp)) {
							throw new NullPointerException();
						}
						LOG.info("success");
						return resp;
					} catch (java.util.concurrent.TimeoutException to) {
						LOG.warn("timeout calling to static file");
					} catch (NullPointerException | NoSuchElementException nse) {
						LOG.warn("empty response after {} ms", end > 0 ? end : System.currentTimeMillis() - start);
					} catch (Exception e) {
						LOG.error("error executing", e);
					}
					return null;
				}
			});
		}

	}

	private HttpServer<ByteBuf, ByteBuf> createServer(final int listenPort) {
		HttpServer<ByteBuf, ByteBuf> server = new HttpServerBuilder<ByteBuf, ByteBuf>(listenPort,
				(request, response) -> {
					return request.getContent().compose(new RequestExecutor()).flatMap(r -> {
						if (isEmpty(r)) response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
						else {
							response.setStatus(HttpResponseStatus.OK);
							response.writeString(r);
						}
						return response.close();
					});
				}).build();
		LOG.info("Proxy Server started on port 9000");
		return server;
	}

	public static void main(final String[] args) {
		new ProxyServer().createServer(9000).startAndWait();
	}
}

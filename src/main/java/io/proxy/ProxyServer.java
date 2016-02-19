package io.proxy;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerBuilder;
import rx.Observable;
import rx.Observable.Transformer;

class ProxyServer {
    private final static Logger LOG = LoggerFactory.getLogger(ProxyServer.class);

    private boolean isEmpty(CharSequence c) {
        return c == null || c.length() == 0;
    }

    class RequestExecutor implements Transformer<ByteBuf, String> {
        private final HttpClient<ByteBuf, ByteBuf> client;

        public RequestExecutor(HttpClient<ByteBuf, ByteBuf> client) {
            this.client = client;
        }

        @Override
        public Observable<String> call(Observable<ByteBuf> t) {
            return t.map(buf -> buf.toString(Charset.defaultCharset()))
                    .reduce((s1, s2) -> s1 + s2)
                    .flatMap(payload -> {
                                final HttpClientRequest<ByteBuf> request =
                                        HttpClientRequest.<ByteBuf>create(HttpMethod.GET, "/static.json").withContent(payload);
                                return client.submit(request)
                                        .timeout(10, TimeUnit.MILLISECONDS)
                                        .flatMap(HttpClientResponse::getContent)
                                        .map(buf -> buf.toString(Charset.defaultCharset()))
                                        .reduce((s1, s2) -> s1 + s2);
                            });
        }

    }

    private HttpServer<ByteBuf, ByteBuf> createServer(final int listenPort) {
        HttpClient<ByteBuf, ByteBuf> client = RxNetty.<ByteBuf, ByteBuf> createHttpClient("127.0.0.1", 54321);
        HttpServer<ByteBuf, ByteBuf> server = new HttpServerBuilder<ByteBuf, ByteBuf>(listenPort,
                (request, response) -> {
                    return request.getContent().compose(new RequestExecutor(client)).flatMap(r -> {
                        if (isEmpty(r)) {
                            response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                        }
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
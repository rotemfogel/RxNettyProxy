package io.proxy;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.proxy.transformers.ExternalRequestExecutor;
import io.proxy.transformers.ExternalRequestTransformer;
import io.proxy.transformers.RequestTransformer;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProxyServer {

	private final static Logger					LOG	= LoggerFactory.getLogger(ProxyServer.class);
	private final HttpClient<ByteBuf, ByteBuf>	client;

	private ProxyServer(final String bidderUrl, final int bidderPort) {
		client = RxNetty.createHttpClient(bidderUrl, bidderPort);
	}

	public static void main(final String[] args) {
		new ProxyServer("localhost", 9001).createServer(9000).startAndWait();
	}

	private HttpServer<ByteBuf, ByteBuf> createServer(final int listenPort) {
		HttpServer<ByteBuf, ByteBuf> server = new HttpServerBuilder<ByteBuf, ByteBuf>(listenPort, (
				request, response) -> request
				.getContent()
				.compose(new RequestTransformer())
				.filter(bidreq -> bidreq != null)
				.compose(new ExternalRequestTransformer())
				.compose(new ExternalRequestExecutor(client))
				.flatMap(
						resp -> {
							response.setStatus(resp == null ? HttpResponseStatus.NO_CONTENT
									: HttpResponseStatus.OK);
							response.writeString(resp == null ? "" : resp.toString());
							return response.close();
						})).build();
		LOG.info("Gateway started...");
		return server;
	}
}

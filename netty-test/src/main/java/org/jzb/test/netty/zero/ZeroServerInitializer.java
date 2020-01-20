package org.jzb.test.netty.zero;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @author jzb 2019-12-15
 */
public class ZeroServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;

    public ZeroServerInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }
//        pipeline.addLast("logging", new LoggingHandler(LogLevel.INFO));
        pipeline.addLast("httpDecoder", new HttpRequestDecoder());
        pipeline.addLast("httpEncoder", new HttpResponseEncoder());
        pipeline.addLast("inflater", new HttpContentDecompressor(false));
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        pipeline.addLast("idle", new IdleStateHandler(0, 0, 0, TimeUnit.SECONDS));
        pipeline.addLast(new HttpServerExpectContinueHandler());
        pipeline.addLast(new ZeroServerHandler());
    }
}

package org.jzb.test.adv.time;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;

/**
 * @author jzb 2019-12-15
 */
public class TimeClientImpl implements TimeClient {
    private final VertxInternal vertxInternal;
    private final ContextInternal contextInternal;
    private final Bootstrap bootstrap;

    public TimeClientImpl(Vertx vertx) {
        vertxInternal = (VertxInternal) vertx;
        contextInternal = vertxInternal.getOrCreateContext();
        final EventLoop eventLoop = contextInternal.nettyEventLoop();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoop);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
    }

    @Override
    public void getTime(int port, String host, Handler<AsyncResult<Long>> resultHandler) {
        bootstrap.handler(new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel ch) {
                final ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new TimeClientHandler(contextInternal, resultHandler));
            }
        });
        final ChannelFuture connectFuture = bootstrap.connect(host, port);
        connectFuture.addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                contextInternal.executeFromIO(v -> {
                    resultHandler.handle(Future.failedFuture(future.cause()));
                });
            }
        });
    }
}

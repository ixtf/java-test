package org.jzb.test.adv.time;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.vertx.core.*;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;

/**
 * @author jzb 2019-12-15
 */
public class TimeServerImpl implements TimeServer {
    private final VertxInternal vertxInternal;
    private final ContextInternal contextInternal;
    private final ServerBootstrap serverBootstrap;

    public TimeServerImpl(Vertx vertx) {
        vertxInternal = (VertxInternal) vertx;
        contextInternal = vertxInternal.getOrCreateContext();
        serverBootstrap = new ServerBootstrap();
    }

    @Override
    public TimeServer requestHandler(Handler<Promise<Long>> requestHandler) {
        final EventLoopGroup acceptorEventLoopGroup = vertxInternal.getAcceptorEventLoopGroup();
        final EventLoop eventLoop = contextInternal.nettyEventLoop();
        serverBootstrap.group(acceptorEventLoopGroup, eventLoop)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        final ChannelPipeline pipeline = ch.pipeline();
                        final TimeServerHandler handler = new TimeServerHandler(contextInternal, requestHandler);
                        pipeline.addLast(handler);
                    }
                });
        return this;
    }

    @Override
    public void listen(int port, String host, Handler<AsyncResult<Void>> listenHandler) {
        final ChannelFuture bindFuture = serverBootstrap.bind(host, port);
        bindFuture.addListener((ChannelFutureListener) future -> {
            final Channel channel = future.channel();
            contextInternal.executeFromIO(v -> {
                if (future.isSuccess()) {
                    listenHandler.handle(Future.succeededFuture(null));
                } else {
                    listenHandler.handle(Future.failedFuture(future.cause()));
                }
            });
        });
    }

    @Override
    public void close() {
    }
}

package org.jzb.test.adv.time;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;

/**
 * @author jzb 2019-12-15
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter {
    private final ContextInternal contextInternal;
    private final Handler<Promise<Long>> requestHandler;

    public TimeServerHandler(ContextInternal contextInternal, Handler<Promise<Long>> requestHandler) {
        this.contextInternal = contextInternal;
        this.requestHandler = requestHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("channelRead");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final Promise<Long> result = Promise.promise();
        contextInternal.executeFromIO(result, requestHandler);
        result.future().setHandler(ar -> {
            if (ar.succeeded()) {
                final ByteBuf time = ctx.alloc().buffer(4);
                time.writeInt((int) (ar.result() / 1000L + 2208988800L));
                final ChannelFuture f = ctx.writeAndFlush(time);
//                f.addListener((ChannelFutureListener) channelFuture -> ctx.close());
                f.addListener(ChannelFutureListener.CLOSE);
            } else {
                ctx.close();
            }
        });
    }
}

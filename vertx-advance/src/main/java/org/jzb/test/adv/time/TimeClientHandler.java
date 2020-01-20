package org.jzb.test.adv.time;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.ContextInternal;

/**
 * @author jzb 2019-12-15
 */
public class TimeClientHandler extends ChannelInboundHandlerAdapter {
    private final ContextInternal contextInternal;
    private final Handler<AsyncResult<Long>> resultHandler;

    public TimeClientHandler(ContextInternal contextInternal, Handler<AsyncResult<Long>> resultHandler) {
        this.contextInternal = contextInternal;
        this.resultHandler = resultHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        final ByteBuf byteBuf = (ByteBuf) msg;
        try {
            final long currentTimeMillis = (byteBuf.readUnsignedInt() - 2208988800L) * 1000L;
            contextInternal.executeFromIO(Future.succeededFuture(currentTimeMillis), resultHandler);
            ctx.close();
        } finally {
            byteBuf.release();
        }
    }
}

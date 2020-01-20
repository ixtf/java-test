package org.jzb.test.netty.zero;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * @author jzb 2019-12-15
 */
@Slf4j
public class ZeroServerHandler extends ChannelDuplexHandler {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            final HttpRequest request = (HttpRequest) msg;
            if (request.decoderResult() != DecoderResult.SUCCESS) {
                handleError(ctx, request);
                return;
            }

            final QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
            final Map<String, List<String>> uriAttributes = queryStringDecoder.parameters();

            final boolean keepAlive = HttpUtil.isKeepAlive(request);
            final FullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), OK,
                    Unpooled.copiedBuffer("zero".getBytes()));
            response.headers()
                    .set(CONTENT_TYPE, TEXT_PLAIN)
                    .setInt(CONTENT_LENGTH, response.content().readableBytes());

            if (keepAlive) {
                if (!request.protocolVersion().isKeepAliveDefault()) {
                    response.headers().set(CONNECTION, KEEP_ALIVE);
                }
            } else {
                // Tell the client we're going to close the connection.
                response.headers().set(CONNECTION, CLOSE);
            }

            final ChannelFuture f = ctx.write(response);
            if (!keepAlive) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    private void handleError(ChannelHandlerContext ctx, HttpRequest obj) {
        final DecoderResult result = obj.decoderResult();
        final Throwable cause = result.cause();
        if (cause instanceof TooLongFrameException) {
            final String causeMsg = cause.getMessage();
            final HttpResponseStatus status = causeMsg.startsWith("An HTTP line is larger than") ? HttpResponseStatus.REQUEST_URI_TOO_LONG : HttpResponseStatus.BAD_REQUEST;
            final DefaultFullHttpResponse resp = new DefaultFullHttpResponse(obj.protocolVersion(), status);
            final ChannelPromise promise = ctx.newPromise();
            ctx.write(resp, promise);
            promise.addListener(res -> {
                ctx.fireExceptionCaught(result.cause());
            });
        } else {
            ctx.fireExceptionCaught(result.cause());
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelReadComplete");
        ctx.flush();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelWritabilityChanged");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerAdded");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        System.out.println("handlerRemoved");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println("userEventTriggered");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, final Throwable t) throws Exception {
        log.error("", t);
        ctx.close();
    }
}

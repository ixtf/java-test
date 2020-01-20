package org.jzb.test.netty.test0005;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author jzb 2019-12-15
 */
public class TelnetClient {
    static final String HOST = "127.0.0.1";
    static final int PORT = 8023;

    public static void main(String[] args) throws Exception {
        final EventLoopGroup group = new NioEventLoopGroup();
        try {
            final Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new TelnetClientInitializer());

            final Channel ch = b.connect(HOST, PORT).sync().channel();

            ChannelFuture lastWriteFuture = null;
            final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            for (; ; ) {
                final String line = in.readLine();
                if (line == null) {
                    break;
                }

                lastWriteFuture = ch.writeAndFlush(line + "\r\n");

                if ("bye".equals(line.toLowerCase())) {
                    ch.closeFuture().sync();
                    break;
                }
            }

            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
        } finally {
            group.shutdownGracefully();
        }

    }
}

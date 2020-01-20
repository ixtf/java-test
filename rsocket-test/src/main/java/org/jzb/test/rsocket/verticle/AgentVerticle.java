package org.jzb.test.rsocket.verticle;

import com.googlecode.protobuf.format.JsonFormat;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;
import org.jzb.test.rsocket.proto.HelloRequest;
import org.jzb.test.rsocket.proto.HelloServiceClient;
import org.jzb.test.rsocket.proto.SimpleServiceClient;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.jzb.test.rsocket.GuiceModule.getInstance;

/**
 * @author jzb 2019-12-13
 */
@Slf4j
public class AgentVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        final Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.get("/grpc/hello/:name").handler(rc -> {
            final HelloServiceClient client = getInstance(HelloServiceClient.class);
            final String name = rc.pathParam("name");
            final HelloRequest request = HelloRequest.newBuilder().setName(name).build();
            final ByteBuf byteBuf = Unpooled.copiedBuffer("principal", StandardCharsets.UTF_8);
            final Context context = vertx.getOrCreateContext();

            rc.fileUploads();

            client.sayHello(request, byteBuf).map(new JsonFormat()::printToString).subscribe(it -> {
                log.info(Thread.currentThread().toString());
                context.put("test", "test");
                rc.response().end(it);
            }, rc::fail);
            System.out.println(context);
        });

        router.get("/grpc/test").handler(rc -> {
            final Context context = vertx.getOrCreateContext();
            CompletableFuture.supplyAsync(() -> {
                System.out.println(Thread.currentThread());
                final String s = context.get("test");
                System.out.println(s);
                try {
                    Thread.sleep(5_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return "test";
            }).thenAccept(rc.response()::end);
//            Executors.newCachedThreadPool().submit(() -> {
//                Thread.sleep(5_000);
//                return "test";
//            });

//            final Context context = vertx.getOrCreateContext();
//            System.out.println(context);
//            rc.response().end("test");
        });

        router.get("/grpc/simple/:message").handler(rc -> {
            final SimpleServiceClient client = getInstance(SimpleServiceClient.class);
            rc.response().end("test");
        });

        final HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setDecompressionSupported(true)
                .setCompressionSupported(true)
                .setWebsocketSubProtocols("graphql-ws");
        Future.<HttpServer>future(promise -> vertx.createHttpServer(httpServerOptions)
                .requestHandler(router)
                .listen(8080, promise))
                .<Void>mapEmpty()
                .setHandler(startFuture);
    }
}

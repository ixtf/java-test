package org.jzb.test.vertx.grpc.verticle;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jzb.test.grpc.proto.GreeterGrpc;
import org.jzb.test.grpc.proto.HelloReply;
import org.jzb.test.grpc.proto.HelloRequest;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;

/**
 * @author jzb 2019-12-13
 */
public class AgentVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        final Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());


        final ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9999)
                .usePlaintext()
                .build();
        final GreeterGrpc.GreeterFutureStub futureStub = GreeterGrpc.newFutureStub(channel);

        Schedulers.elastic().createWorker();

        router.get("/grpc/greeter/:name").handler(rc -> {
            final String name = rc.pathParam("name");
            final HelloRequest request = HelloRequest.newBuilder().setName(name).build();
            Mono.create(sink -> {
                final ListenableFuture<HelloReply> future = futureStub.sayHello(request);
                Futures.addCallback(future, new FutureCallback<>() {
                    @Override
                    public void onSuccess(@Nullable HelloReply reply) {
                        final String message = reply.getMessage();
                        System.out.println(Thread.currentThread());
                        rc.response().end(message);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        rc.fail(t);
                    }
                }, Executors.newSingleThreadExecutor());

//                Futures.addCallback(future, new FutureCallback<>() {
//                    @Override
//                    public void onSuccess(@Nullable HelloReply result) {
//                        sink.success(result);
//                    }
//
//                    @Override
//                    public void onFailure(Throwable t) {
//                        sink.error(t);
//                    }
//                });

                sink.success();
            });
        });

        router.get("/grpc/test").handler(rc -> {
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

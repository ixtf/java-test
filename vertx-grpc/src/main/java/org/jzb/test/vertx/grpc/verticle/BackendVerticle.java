package org.jzb.test.vertx.grpc.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;
import org.jzb.test.grpc.proto.GreeterGrpc;
import org.jzb.test.grpc.proto.HelloReply;
import org.jzb.test.grpc.proto.HelloRequest;

/**
 * @author jzb 2019-12-13
 */
public class BackendVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        final VertxServer server = VertxServerBuilder.forAddress(vertx, "0.0.0.0", 9999)
                .addService(new GreeterGrpc.GreeterVertxImplBase() {
                    @Override
                    public void sayHello(HelloRequest request, Promise<HelloReply> future) {
                        System.out.println(Thread.currentThread());
                        final HelloReply reply = HelloReply.newBuilder().setMessage(request.getName()).build();
                        future.complete(reply);
                    }
                })
                .build();
        server.start();
//        Future.<Void>future(p -> server.start(p)).handle(startFuture);
    }
}

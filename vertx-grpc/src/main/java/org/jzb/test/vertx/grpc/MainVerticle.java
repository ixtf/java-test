package org.jzb.test.vertx.grpc;

import io.vertx.core.*;
import org.jzb.test.vertx.grpc.verticle.AgentVerticle;
import org.jzb.test.vertx.grpc.verticle.BackendVerticle;

/**
 * @author jzb 2019-12-13
 */
public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        deployBackend().compose(it -> deployAgent())
                .<Void>mapEmpty().setHandler(startFuture);
    }

    private Future<String> deployAgent() {
        return Future.future(p -> {
            final DeploymentOptions options = new DeploymentOptions()
                    .setInstances(20);
            vertx.deployVerticle(AgentVerticle.class, options, p);
        });
    }

    private Future<String> deployBackend() {
        return Future.future(p -> {
            final DeploymentOptions options = new DeploymentOptions().setInstances(20);
            vertx.deployVerticle(BackendVerticle.class, options, p);
        });
    }

    public static void main(String[] args) {
        final VertxOptions options = new VertxOptions()
                .setPreferNativeTransport(true);
        Vertx.vertx(options).deployVerticle(new MainVerticle(), ar -> {
            if (ar.succeeded()) {
                System.out.println("deploy success");
            }
        });
    }
}

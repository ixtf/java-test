package org.jzb.test.rsocket;

import io.vertx.core.*;
import org.jzb.test.rsocket.verticle.AgentVerticle;
import org.jzb.test.rsocket.verticle.BackendVerticle;

/**
 * @author jzb 2019-12-13
 */
public class MainVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        GuiceModule.init(vertx);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        start();
        deployBackend().compose(it -> deployAgent())
                .<Void>mapEmpty().setHandler(startFuture);
    }

    private Future<String> deployAgent() {
        return Future.future(p -> {
            final DeploymentOptions options = new DeploymentOptions();
            vertx.deployVerticle(AgentVerticle.class, options, p);
        });
    }

    private Future<String> deployBackend() {
        return Future.future(p -> {
            final DeploymentOptions options = new DeploymentOptions();
//                    .setInstances(2);
            vertx.deployVerticle(BackendVerticle.class, options, p);
        });
    }

    public static void main(String[] args) {
        final VertxOptions options = new VertxOptions()
                .setWorkerPoolSize(1)
                .setPreferNativeTransport(true);
        Vertx.vertx(options).deployVerticle(new MainVerticle(), ar -> {
            if (ar.succeeded()) {
                System.out.println("deploy success");
            }
        });
    }
}

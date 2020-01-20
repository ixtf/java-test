package org.jzb.test.rsocket;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.jzb.test.rsocket.verticle.BackendVerticle;

/**
 * @author jzb 2019-12-13
 */
public class Backend {
    public static void main(String[] args) {
        final VertxOptions vertxOptions = new VertxOptions()
                .setPreferNativeTransport(true);
        final Vertx vertx = Vertx.vertx(vertxOptions);
        GuiceModule.init(vertx);
        vertx.deployVerticle(new BackendVerticle());
//        Thread.currentThread().join();
    }
}

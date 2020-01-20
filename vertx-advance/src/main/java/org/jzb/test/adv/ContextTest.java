package org.jzb.test.adv;

import io.vertx.core.Context;
import io.vertx.core.Vertx;

/**
 * @author jzb 2019-12-15
 */
public class ContextTest {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        Context context = vertx.getOrCreateContext();
        System.out.println(context);
        context.runOnContext(v -> {
            System.out.println("Current context is " + Vertx.currentContext());
        });
    }
}

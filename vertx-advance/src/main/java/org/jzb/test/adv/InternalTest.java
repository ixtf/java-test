package org.jzb.test.adv;

import io.vertx.core.Vertx;
import org.jzb.test.adv.time.TimeClient;

import java.util.Date;

/**
 * @author jzb 2019-12-15
 */
public class InternalTest {
    public static void main(String[] args) {

        final Vertx vertx = Vertx.vertx();
// Create the time client
        final TimeClient server = TimeClient.create(vertx);
// Fetch the time
        server.getTime(8037, "localhost", ar -> {
            if (ar.succeeded()) {
                System.out.println("Time is " + new Date(ar.result()));
            } else {
                ar.cause().printStackTrace();
            }
        });
    }


}

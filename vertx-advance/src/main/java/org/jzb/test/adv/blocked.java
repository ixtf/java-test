package org.jzb.test.adv;

import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.jzb.test.adv.time.TimeServer;

/**
 * @author jzb 2019-12-14
 */
public class blocked {
    public static void main(String[] args) {
        TimeServer.start();

        Vertx vertx = Vertx.vertx();
//        vertx.runOnContext(v -> {
//            System.out.println(Thread.currentThread());
//
//            Handler<Promise<String>> blockingCodeHandler = future -> {
//                System.out.println(Thread.currentThread());
//                throw new RuntimeException();
//            };
//
//            Handler<AsyncResult<String>> resultHandler = result -> {
//                System.out.println(Thread.currentThread());
//                if (result.succeeded()) {
//                    System.out.println("Got result");
//                } else {
//                    System.out.println("Blocking code failed");
////                    result.cause().printStackTrace(System.out);
//                }
//            };
//
//            vertx.executeBlocking(blockingCodeHandler, resultHandler);
//        });

        vertx.runOnContext(v -> {

            // On the event loop
            System.out.println("Calling blocking block from " + Thread.currentThread());

            Handler<Promise<String>> blockingCodeHandler = future -> {
                // Non event loop
                System.out.println("Computing with " + Thread.currentThread());

                // Running on context from the worker
                vertx.runOnContext(v2 -> {
                    System.out.println("Running on context from the worker " + Thread.currentThread());
                });
            };

            // Execute the blocking code handler and the associated result handler
            vertx.executeBlocking(blockingCodeHandler, result -> {});
        });


//        System.out.println(Thread.currentThread());
//        for (int i = 0; i < 20; i++) {
//            int index = i;
//            vertx.setTimer(1, timerID -> {
//                System.out.println(Thread.currentThread().getClass() + ":" + Thread.currentThread());
//            });
//        }
//        vertx.periodicStream(1);
//        vertx.periodicStream(1).handler(l->{
//            System.out.println(Thread.currentThread());
//        });
//        vertx.runOnContext(it -> {
//            vertx.setTimer(1, id -> {
//                System.out.println(Thread.currentThread());
//                // Blocking the Vert.x event loop
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            });
//        });
    }
}

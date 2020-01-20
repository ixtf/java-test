package org.jzb.test.adv.time;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

/**
 * @author jzb 2019-12-15
 */
public interface TimeServer {

    /**
     * @return a new time server
     */
    static TimeServer create(Vertx vertx) {
        return new TimeServerImpl(vertx);
    }

    /**
     * Set the handler to be called when a time request happens. The handler should complete
     * the future with the time value.
     *
     * @param handler the handler to be called
     * @return this object
     */
    TimeServer requestHandler(Handler<Promise<Long>> handler);

    /**
     * Start and bind the time server.
     *
     * @param port          the server port
     * @param host          the server host
     * @param listenHandler the listen result handler
     */
    void listen(int port, String host, Handler<AsyncResult<Void>> listenHandler);

    /**
     * Close the time server.
     */
    void close();

    static void start() {
        final Vertx vertx = Vertx.vertx();
// Create the time server
        final TimeServer server = TimeServer.create(vertx);
        server.requestHandler(time -> {
            time.complete(System.currentTimeMillis());
        });
// Start the server
        server.listen(8037, "0.0.0.0", ar -> {
            if (ar.succeeded()) {
                System.out.println("Server started");
            } else {
                ar.cause().printStackTrace();
            }
        });
    }
}

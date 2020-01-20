package org.jzb.test.adv.time;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * @author jzb 2019-12-15
 */
public interface TimeClient {

    /**
     * @return a new time client
     */
    static TimeClient create(Vertx vertx) {
        return new TimeClientImpl(vertx);
    }

    /**
     * Fetch the current time from a server.
     *
     * @param port the server port
     * @param host the server host name
     * @param resultHandler the asynchronous time result
     */
    void getTime(int port, String host, Handler<AsyncResult<Long>> resultHandler);

}

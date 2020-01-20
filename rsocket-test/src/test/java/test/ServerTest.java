package test;

import com.google.common.primitives.Ints;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.jzb.test.rsocket.GuiceModule;
import org.jzb.test.rsocket.verticle.AgentVerticle;

/**
 * @author jzb 2019-12-13
 */
public class ServerTest {
    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        GuiceModule.init(vertx);
//        IntStream.range(0, 2).forEach(it -> BackendVerticle.deployHelloService());
        vertx.deployVerticle(AgentVerticle.class, new DeploymentOptions().setInstances(20), ar -> {
            if (ar.succeeded()) {
                System.out.println("success");
            }
        });

        final byte[] bytes = Ints.toByteArray(197);
        System.out.println(bytes);
    }
}

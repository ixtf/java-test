package greeter;

import io.envoyproxy.pgv.ReflectiveValidatorIndex;
import io.envoyproxy.pgv.ValidatorIndex;
import io.envoyproxy.pgv.grpc.ValidatingClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.jzb.test.grpc.proto.GreeterGrpc;
import org.jzb.test.grpc.proto.HelloReply;
import org.jzb.test.grpc.proto.HelloRequest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author jzb 2019-12-13
 */
@Slf4j
public class GreeterTest {
    private final ManagedChannel channel;
    //    private final GreeterGrpc.GreeterBlockingStub blockingStub;
    private final GreeterGrpc.GreeterFutureStub futureStub;

    /**
     * Construct client connecting to HelloWorld server at {@code host:port}.
     */
    public GreeterTest(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build());
    }

    /**
     * Construct client for accessing HelloWorld server using the existing channel.
     */
    GreeterTest(ManagedChannel channel) {
        this.channel = channel;
//        blockingStub = GreeterGrpc.newBlockingStub(channel);
        final ValidatorIndex validatorIndex = new ReflectiveValidatorIndex();
        futureStub = GreeterGrpc.newFutureStub(channel)
                .withInterceptors(new ValidatingClientInterceptor(validatorIndex));
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Say hello to server.
     */
    public void greet(String name) throws ExecutionException, InterruptedException {
        log.info("Will try to greet " + name + " ...");
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloReply response;
        try {
//            response = blockingStub.sayHello(request);
            response = futureStub.sayHello(request).get();
        } catch (StatusRuntimeException e) {
            log.warn("RPC failed: {0}", e.getStatus());
            return;
        }
        log.info("Greeting: " + response.getMessage());
    }

    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting.
     */
    public static void main(String[] args) throws Exception {
        // Access a service running on the local machine on port 50051
        GreeterTest client = new GreeterTest("localhost", 8081);
        try {
            String user = "world";
            // Use the arg as the name to greet if provided
            if (args.length > 0) {
                user = args[0];
            }
            client.greet(user);
        } finally {
            client.shutdown();
        }
    }

}

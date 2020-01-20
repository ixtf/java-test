package org.jzb.test.grpc;

import io.envoyproxy.pgv.ReflectiveValidatorIndex;
import io.envoyproxy.pgv.ValidatorIndex;
import io.envoyproxy.pgv.grpc.ValidatingServerInterceptor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.jzb.test.grpc.proto.GreeterGrpc;
import org.jzb.test.grpc.proto.HelloReply;
import org.jzb.test.grpc.proto.HelloRequest;

import java.io.IOException;

/**
 * @author jzb 2019-12-13
 */
@Slf4j
public class GreeterServer {
    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 8081;
        final GreeterImpl service = new GreeterImpl();
        final ValidatorIndex validatorIndex = new ReflectiveValidatorIndex();
        final ValidatingServerInterceptor validatingInterceptor = new ValidatingServerInterceptor(validatorIndex);
        final ServerServiceDefinition serviceDefinition = ServerInterceptors.intercept(service, validatingInterceptor);
        server = ServerBuilder.forPort(port)
                .addService(serviceDefinition)
                .build()
                .start();
        log.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                GreeterServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final GreeterServer server = new GreeterServer();
        server.start();
        server.blockUntilShutdown();
    }

    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            final HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}

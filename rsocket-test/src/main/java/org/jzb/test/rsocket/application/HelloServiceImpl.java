package org.jzb.test.rsocket.application;

import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jzb.test.rsocket.proto.HelloRequest;
import org.jzb.test.rsocket.proto.HelloResponse;
import org.jzb.test.rsocket.proto.HelloService;
import reactor.core.publisher.Mono;

/**
 * @author jzb 2019-12-13
 */
@Slf4j
public class HelloServiceImpl implements HelloService {
    private final String serviceName;

    public HelloServiceImpl(final String serviceName) {
        this.serviceName = serviceName;
    }

    @SneakyThrows
    @Override
    public Mono<HelloResponse> sayHello(HelloRequest message, ByteBuf metadata) {
        log.info("received a message -> {}", message.getName());
//        if (metadata.hasArray()) {
//            byte[] array = metadata.array();
//            int offset = metadata.arrayOffset() + metadata.readerIndex();
//            int length = metadata.readableBytes();
//            System.out.println("metadata=" + Arrays.toString(array));
//            System.out.println(offset);
//            System.out.println(length);
//        }
        Thread.sleep(40_000);
        return Mono.fromCallable(() -> HelloResponse.newBuilder()
                .setMessage("Hello, " + message.getName() + "! from " + serviceName)
                .build());
    }
}

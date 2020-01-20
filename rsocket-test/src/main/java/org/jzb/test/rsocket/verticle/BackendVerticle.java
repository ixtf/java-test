package org.jzb.test.rsocket.verticle;

import com.netifi.broker.BrokerClient;
import io.vertx.core.AbstractVerticle;
import org.jzb.test.rsocket.GuiceModule;
import org.jzb.test.rsocket.application.HelloServiceImpl;
import org.jzb.test.rsocket.application.SimpleServiceImpl;
import org.jzb.test.rsocket.proto.HelloService;
import org.jzb.test.rsocket.proto.HelloServiceServer;
import org.jzb.test.rsocket.proto.SimpleService;
import org.jzb.test.rsocket.proto.SimpleServiceServer;

import java.util.Optional;
import java.util.UUID;

/**
 * @author jzb 2019-12-13
 */
public class BackendVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        deployHelloService();
        deploySimpleService();
    }

    public static void deploySimpleService() {
        final String serviceName = SimpleService.class.getSimpleName() + "-" + UUID.randomUUID().toString();
        final String groupName = "java-test:" + SimpleService.SERVICE;
        // Add Service to Respond to Requests
        final SimpleService simpleService = new SimpleServiceImpl(serviceName);
        final SimpleServiceServer server = new SimpleServiceServer(simpleService, Optional.empty(), Optional.empty());


        final BrokerClient.TcpBuilder tcpBuilder = GuiceModule.getInstance(BrokerClient.TcpBuilder.class);
        final BrokerClient netifi = tcpBuilder.group(groupName).destination(serviceName).build();
        netifi.addService(server);
        // Connect to Netifi Platform
//        netifi.groupServiceSocket(groupName, Tags.empty());
    }

    public static void deployHelloService() {
        final String serviceName = HelloService.class.getSimpleName() + "-" + UUID.randomUUID().toString();
        final String groupName = "java-test:" + HelloService.SERVICE;
        // Add Service to Respond to Requests
        final HelloServiceImpl helloService = new HelloServiceImpl(serviceName);
        final HelloServiceServer server = new HelloServiceServer(helloService, Optional.empty(), Optional.empty());

        final BrokerClient.TcpBuilder tcpBuilder = GuiceModule.getInstance(BrokerClient.TcpBuilder.class);
        final BrokerClient netifi = tcpBuilder.group(groupName).destination(serviceName).build();
        netifi.addService(server);
        // Connect to Netifi Platform
//        netifi.groupServiceSocket(groupName, Tags.empty());
    }
}

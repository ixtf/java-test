package org.jzb.test.rsocket;

import com.google.inject.*;
import com.google.inject.name.Named;
import com.netifi.broker.BrokerClient;
import com.netifi.broker.rsocket.BrokerSocket;
import com.netifi.common.tags.Tags;
import io.jaegertracing.Configuration;
import io.jaegertracing.Configuration.ReporterConfiguration;
import io.jaegertracing.Configuration.SamplerConfiguration;
import io.jaegertracing.Configuration.SenderConfiguration;
import io.opentracing.Tracer;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.SneakyThrows;
import org.jzb.test.rsocket.proto.HelloService;
import org.jzb.test.rsocket.proto.HelloServiceClient;
import org.jzb.test.rsocket.proto.SimpleService;
import org.jzb.test.rsocket.proto.SimpleServiceClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static com.github.ixtf.japp.core.Constant.YAML_MAPPER;

/**
 * @author jzb 2019-12-13
 */
public class GuiceModule extends AbstractModule {
    private static Injector INJECTOR;
    private final Vertx vertx;

    private GuiceModule(Vertx vertx) {
        this.vertx = vertx;
    }

    synchronized public static void init(Vertx vertx) {
        if (INJECTOR == null) {
            INJECTOR = Guice.createInjector(new GuiceModule(vertx));
        }
    }

    public static <T> T getInstance(Class<T> clazz) {
        return INJECTOR.getInstance(clazz);
    }

    public static <T> T getInstance(Key<T> key) {
        return INJECTOR.getInstance(key);
    }

    public static void injectMembers(Object o) {
        INJECTOR.injectMembers(o);
    }

    @Override
    protected void configure() {
        bind(Vertx.class).toInstance(vertx);
    }

    @Provides
    @Singleton
    @Named("rootPath")
    private Path rootPath() {
        return Path.of(System.getProperty("rsocket-test.path", "/home/java-test/rsocket-test"));
    }

    @SneakyThrows(IOException.class)
    @Provides
    @Singleton
    @Named("vertxConfig")
    private JsonObject vertxConfig(@Named("rootPath") Path rootPath) {
        final File ymlFile = rootPath.resolve("config.yml").toFile();
        if (ymlFile.exists()) {
            final Map map = YAML_MAPPER.readValue(ymlFile, Map.class);
            return new JsonObject(map);
        }
        return new JsonObject();
    }

    @Provides
    @Singleton
    private HelloServiceClient HelloServiceClient(BrokerClient.TcpBuilder tcpBuilder) {
        final BrokerClient netifi = tcpBuilder.group("java-test:clients").destination("client1").build();
        final String groupName = "java-test:" + HelloService.SERVICE;
        final BrokerSocket conn = netifi.groupServiceSocket(groupName, Tags.empty());
        // Create Client to Communicate with the HelloService (included example service)
        return new HelloServiceClient(conn);
    }

    @Provides
    @Singleton
    private SimpleServiceClient SimpleServiceClient(BrokerClient.TcpBuilder tcpBuilder) {
        final BrokerClient netifi = tcpBuilder.group("java-test:clients").destination("client1").build();
        final String groupName = "java-test:" + SimpleService.SERVICE;
        final BrokerSocket conn = netifi.groupServiceSocket(groupName, Tags.empty());
        // Create Client to Communicate with the HelloService (included example service)
        return new SimpleServiceClient(conn);
    }

    @Provides
    private BrokerClient.TcpBuilder TcpBuilder(@Named("vertxConfig") JsonObject vertxConfig) {
        final JsonObject netifiConfig = vertxConfig.getJsonObject("netifi", new JsonObject());
//        final String host = netifiConfig.getString("host", "localhost");
        final String host = netifiConfig.getString("host", "192.168.0.38");
        final int port = netifiConfig.getInteger("port", 8001);
        final long accessKey = netifiConfig.getLong("accessKey", 9007199254740991L);
        final String accessToken = netifiConfig.getString("accessToken", "kTBDVtfRBO4tHOnZzSyY5ym2kfY=");
        // Build Netifi Broker Connection
        return BrokerClient.tcp()
                .host(host) // Netifi Broker Host
                .port(port) // Netifi Broker Port
                .accessKey(accessKey)
                .accessToken(accessToken)
                .disableSsl(); // Disabled for parity with Javascript Tutorial
    }

    @SneakyThrows
    @Provides
    @Singleton
    private Tracer Tracer(@javax.inject.Named("vertxConfig") JsonObject vertxConfig) {
        final JsonObject apm = vertxConfig.getJsonObject("apm", new JsonObject());
        final String serviceName = apm.getString("serviceName", "rsocket-test");
        final String agentHost = apm.getString("agentHost", "192.168.0.38");
        final SamplerConfiguration samplerConfig = SamplerConfiguration.fromEnv().withType("const").withParam(1);
        final SenderConfiguration senderConfiguration = new SenderConfiguration().withEndpoint("http://" + agentHost + ":14268/api/traces");
        final ReporterConfiguration reporterConfig = ReporterConfiguration.fromEnv().withSender(senderConfiguration).withLogSpans(true);
        final Configuration config = new Configuration(serviceName).withSampler(samplerConfig).withReporter(reporterConfig);
        return config.getTracer();
    }
}

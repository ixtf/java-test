package org.jzb.test.helidon;

import io.helidon.config.Config;
import io.helidon.health.HealthSupport;
import io.helidon.health.checks.HealthChecks;
import io.helidon.media.jackson.server.JacksonSupport;
import io.helidon.media.jsonp.server.JsonSupport;
import io.helidon.metrics.MetricsSupport;
import io.helidon.security.integration.webserver.WebSecurity;
import io.helidon.tracing.TracerBuilder;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerConfiguration;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebTracingConfig;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * @author jzb 2019-12-16
 */
public class Helidon {
    public static void main(String[] args) {
        startServer();
    }

    private static WebServer startServer() {
        // load logging configuration
        setupLogging();

        // By default this will pick up application.yaml from the classpath
        final Config config = Config.create();

        // Get webserver config from the "server" section of application.yaml
        final ServerConfiguration serverConfig = ServerConfiguration.builder(config.get("server"))
                .tracer(TracerBuilder.create(config.get("tracing"))
                        .collectorHost("192.168.0.38")
                        .registerGlobal(true))
                .build();

        final WebServer server = WebServer.create(serverConfig, createRouting(config));

        // Try to start the server. If successful, print some info and arrange to
        // print a message at shutdown. If unsuccessful, print the exception.
        server.start()
                .thenAccept(ws -> {
                    System.out.println("WEB server is up! http://localhost:" + ws.port() + "/greet");
                    ws.whenShutdown().thenRun(() -> System.out.println("WEB server is DOWN. Good bye!"));
                })
                .exceptionally(t -> {
                    System.err.println("Startup failed: " + t.getMessage());
                    t.printStackTrace(System.err);
                    return null;
                });

        // Server threads are not daemon. No need to block. Just react.
        return server;
    }

    private static Routing createRouting(Config config) {
        final MetricsSupport metrics = MetricsSupport.create();
        final HealthSupport health = HealthSupport.builder()
                .addLiveness(HealthChecks.healthChecks())   // Adds a convenient set of checks
                .build();
        final GreetService greetService = new GreetService(config);

        return Routing.builder()
                .register(WebTracingConfig.create(config.get("tracing")))
                .register(WebSecurity.create(config.get("security")))
                .register(JacksonSupport.create())
                .register(JsonSupport.create())
                .register(health)                   // Health at "/health"
                .register(metrics)                  // Metrics at "/metrics"
                .register("/greet", greetService)
                .build();
    }

    /**
     * Configure logging from logging.properties file.
     */
    @SneakyThrows(IOException.class)
    private static void setupLogging() {
        try (InputStream is = Helidon.class.getResourceAsStream("/logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        }
    }
}

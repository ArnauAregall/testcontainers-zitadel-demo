package tech.aregall.testcontainers.zitadel.test;

import org.slf4j.Logger;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;
import static org.testcontainers.shaded.org.apache.commons.io.FileUtils.getFile;
import static org.testcontainers.shaded.org.apache.commons.io.FileUtils.readFileToString;

class ZitadelComposeContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log = getLogger(ZitadelComposeContainerInitializer.class);

    @Container
    public static ComposeContainer container = new ComposeContainer(new File("src/test/resources/compose/compose-test.yml"))
            .withExposedService("zitadel-db", 5432)
            .withExposedService("zitadel", 8080,
                    Wait.forHttp("/healthz").forStatusCode(200).withStartupTimeout(Duration.ofSeconds(5)))
            .withLogConsumer("zitadel", new Slf4jLogConsumer(getLogger("zitadel-container")));

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        log.debug("Starting Zitadel compose container");
        container.start();

        final String zitadelHost = container.getServiceHost("zitadel", 8080);
        final Integer zitadelPort = container.getServicePort("zitadel", 8080);
        final String adminPat = readZitadelAdminPat();

        final Map<String, String> properties = Map.of(
                "testcontainers.zitadel.host", zitadelHost,
                "testcontainers.zitadel.port", String.valueOf(zitadelPort),
                "testcontainers.zitadel.admin-pat", adminPat
        );

        log.debug("Zitadel properties: {}", properties);

        TestPropertyValues.of(properties).applyTo(applicationContext.getEnvironment());
    }

    private static String readZitadelAdminPat() {
        try {
            return readFileToString(getPatFile(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error reading Zitadel PAT file", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * See {@code zitadel-init-steps.yaml}
     * @return the File containing the PAT.
     */
    private static File getPatFile() {
        return getFile("/tmp/zitadel-init/zitadel-admin-sa.pat");
    }

}
package tech.aregall.testcontainers.zitadel.test;

import org.slf4j.Logger;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.testcontainers.shaded.org.apache.commons.io.FileUtils.getFile;
import static org.testcontainers.shaded.org.apache.commons.io.FileUtils.readFileToString;

class ZitadelComposeContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String ZITADEL_IMAGE_LATEST = "ghcr.io/zitadel/zitadel:latest";
    private static final String ENV_ZITADEL_IMAGE = "ZITADEL_IMAGE";
    private static final String ENV_ZITADEL_ADMIN_PAT_DIR = "ZITADEL_ADMIN_PAT_DIR";

    private static final Logger log = getLogger(ZitadelComposeContainerInitializer.class);

    @Container
    private static final ComposeContainer container = new ComposeContainer(new File("src/test/resources/compose/compose-test.yml"))
            .withExposedService("zitadel-db", 5432)
            .withExposedService("zitadel", 8080,
                    Wait.forHttp("/debug/healthz").forStatusCode(200).withStartupTimeout(Duration.ofSeconds(10)))
            .withLogConsumer("zitadel", new Slf4jLogConsumer(getLogger("zitadel-container")));

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        final ConfigurableEnvironment env = applicationContext.getEnvironment();

        final String zitadelImage = Optional.ofNullable(env.getProperty("testcontainers.zitadel.image")).orElse(ZITADEL_IMAGE_LATEST);
        final String zitadelAdminPatDir = getZitadelAdminPatDirPath();

        container.withEnv(Map.of(
                ENV_ZITADEL_IMAGE, zitadelImage,
                ENV_ZITADEL_ADMIN_PAT_DIR, zitadelAdminPatDir
        ));

        log.debug("Starting Zitadel compose container with image '{}', admin PAT directory: {}", zitadelImage, zitadelAdminPatDir);
        container.start();

        final String zitadelHost = container.getServiceHost("zitadel", 8080);
        final Integer zitadelPort = container.getServicePort("zitadel", 8080);
        final String adminPat = readZitadelAdminPat(zitadelAdminPatDir);

        final Map<String, String> properties = Map.of(
                "testcontainers.zitadel.host", zitadelHost,
                "testcontainers.zitadel.port", String.valueOf(zitadelPort),
                "testcontainers.zitadel.admin-pat", adminPat
        );

        log.debug("Zitadel properties: {}", properties);

        TestPropertyValues.of(properties).applyTo(env);
    }

    /**
     * Get the directory where the admin PAT file is stored.
     * If the environment variable {@code ZITADEL_ADMIN_PAT_DIR} is set, it will be used.
     * Otherwise, it will be created.
     * @return the directory where the admin PAT file is stored.
     */
    private static String getZitadelAdminPatDirPath() {
        return Optional.ofNullable(System.getenv(ENV_ZITADEL_ADMIN_PAT_DIR)).orElse(createZitadelAdminPatDir());
    }

    /**
     * Create a directory for the admin PAT file.
     * <br>
     * The directory is created under the execution directory of the generated test class (for Maven typically {@code target} dir)
     * with write permissions.
     * <br>
     * Zitadel will create the admin PAT file in this directory.
     * <br>
     * This directory is mounted in the container as a volume. See {@code compose-test.yml}.
     * @return the directory where the admin PAT file is stored.
     */
    private static String createZitadelAdminPatDir() {
        try {
            final Class<ZitadelComposeContainerInitializer> initializerClass = ZitadelComposeContainerInitializer.class;

            final String targetPath = Paths.get(requireNonNull(initializerClass.getResource("/")).toURI())
                    .getParent()
                    .toAbsolutePath()
                    .toString();
            final String wrapperFolder = initializerClass.getName().replace(".", "_");
            final String patDir = String.format("%s/%s/%s-zitadel-admin-pat-dir", targetPath, wrapperFolder, System.currentTimeMillis());

            final File dir = new File(patDir);
            dir.mkdirs();
            dir.setWritable(true, false);
            return patDir;
        } catch (URISyntaxException e) {
            log.error("Failed to read default Zitadel admin PAT directory", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * See {@code zitadel-init-steps.yaml}.
     * @param zitadelAdminPatDir the directory where the admin PAT file is stored.
     * @return the PAT file content.
     */
    private static String readZitadelAdminPat(final String zitadelAdminPatDir) {
        try {
            return readFileToString(getFile("/%s/zitadel-admin-sa.pat".formatted(zitadelAdminPatDir)), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            log.error("Error reading Zitadel PAT file", e);
            throw new RuntimeException("Failed to read Zitadel PAT file", e);
        }
    }

}
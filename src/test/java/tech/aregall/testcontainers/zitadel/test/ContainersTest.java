package tech.aregall.testcontainers.zitadel.test;

import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Testcontainers
@ContextConfiguration(initializers = {
        ZitadelComposeContainerInitializer.class
})
public @interface ContainersTest {
}


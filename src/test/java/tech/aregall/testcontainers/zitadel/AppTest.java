package tech.aregall.testcontainers.zitadel;

import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.RestAssured;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import tech.aregall.testcontainers.zitadel.infrastructure.grpc.ZitadelGrpcClient;
import tech.aregall.testcontainers.zitadel.test.ContainersTest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.slf4j.LoggerFactory.getLogger;

@SpringBootTest
@ActiveProfiles("test")
@ContainersTest
class AppTest {

    private static final Logger log = getLogger(AppTest.class);

    @Value("${app.zitadel.base-url}")
    private String zitadelBaseUrl;

    @Value("${app.zitadel.authorization-header}")
    private String zitadelAuthorizationHeader;

    @Nested
    @TestInstance(PER_CLASS)
    class WithRestAssured {

        @BeforeAll
        void setupRestAssured() {
            RestAssured.baseURI = zitadelBaseUrl;
        }

        @Test
        void shouldGetZitadelInstanceInfo() {
            given()
					.header("Authorization", zitadelAuthorizationHeader)
                    .when()
                    .get("/admin/v1/instances/me")
                    .then()
                    .log().body(true)
                    .statusCode(200)
                    .body(
                            "instance.id", notNullValue(),
                            "instance.name", equalTo("Testcontainers ZITADEL")
                    );
        }

    }

    @Nested
    @TestInstance(PER_CLASS)
    class WithRestClient {

        private RestClient restClient;

        @BeforeAll
        void setupRestClient() {
            restClient = RestClient.builder()
                    .baseUrl(zitadelBaseUrl)
                    .messageConverters(converters -> converters.add(new MappingJackson2XmlHttpMessageConverter()))
                    .defaultHeader("Authorization", zitadelAuthorizationHeader)
                    .build();
        }

        @Test
        void shouldGetZitadelInstanceInfo() {
            final ResponseEntity<JsonNode> getInstanceResponse = restClient.get().uri("/admin/v1/instances/me")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            (request, response) -> log.error("Failed to get Zitadel instance info: {}", response.getStatusCode()))
                    .toEntity(JsonNode.class);

            assertThat(getInstanceResponse.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(getInstanceResponse.getBody())
                    .isNotNull()
                    .asInstanceOf(InstanceOfAssertFactories.type(JsonNode.class))
                    .satisfies(
                            body -> assertThat(body.get("instance").get("id").asText()).isNotBlank(),
                            body -> assertThat(body.get("instance").get("name").asText()).isEqualTo("Testcontainers ZITADEL")
                    );
        }
    }

    @Nested
    class WithGrpc {

        @Autowired
        ZitadelGrpcClient zitadelGrpcClient;

        @Test
        void test() {
            final String instanceName = zitadelGrpcClient.getInstanceName();
            assertThat(instanceName).isEqualTo("Testcontainers ZITADEL");
        }

    }

}

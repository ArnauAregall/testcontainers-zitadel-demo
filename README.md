# testcontainers-zitadel-demo

[![Build Maven project](https://github.com/ArnauAregall/testcontainers-zitadel-demo/actions/workflows/build-mvn-project.yml/badge.svg)](https://github.com/ArnauAregall/testcontainers-zitadel-demo/actions/workflows/build-mvn-project.yml)

Demo project to showcase how to run a [ZITADEL](https://zitadel.com/) instance on [Spring Boot](https://spring.io/projects/spring-boot) integration tests using [Testcontainers](https://testcontainers.com/).

Tech stack:

- Java
- Maven (Wrapper)
- Spring Boot
- gRPC and Protobuf
- [gRPC Spring Boot Client Starter](https://github.com/grpc-ecosystem/grpc-spring)
- Testcontainers
- Docker
- Docker Compose
- ZITADEL

The Maven build clones the ZITADEL repository to fetch the latest version of the gRPC proto files, and generates the Java classes using the Maven plugin `protobuf-maven-plugin`.

----
## Requirements

The application requires **JDK 21**.

Is recommended to use [SDKMAN!](https://sdkman.io/) to install the JDK.

````shell
curl -s "https://get.sdkman.io" | bash;
sdk install java 21.0.2-graalce;
sdk use java 21.0.2-graalce;
````
----

## Running the integration tests

Run the following Maven command to run the application tests, which behind the scenes will start a ZITADEL instance using Testcontainers:

````shell
./mvnw clean verify
````

----
## Running the application locally

As of now, the application only does a gRPC healthcheck against the configured ZITADEL instance. 
It should be a good starting point to scaffold Spring Boot applications with a gRPC client for ZITADEL.

Run the following command to run the application, using your own ZITADEL instance:

````shell
export APP_ZITADEL_HOST=localhost \
       APP_ZITADEL_PORT=8080 \
       APP_ZITADEL_ADMIN_PAT=your_admin_service_account_pat && \
./mvnw spring-boot:run
````

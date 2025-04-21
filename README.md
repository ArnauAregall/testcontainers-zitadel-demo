# testcontainers-zitadel-demo

[![Build Maven project](https://github.com/ArnauAregall/testcontainers-zitadel-demo/actions/workflows/build-mvn-project.yml/badge.svg)](https://github.com/ArnauAregall/testcontainers-zitadel-demo/actions/workflows/build-mvn-project.yml)

Demo project to showcase how to run a [ZITADEL](https://zitadel.com/) instance on [Spring Boot](https://spring.io/projects/spring-boot) integration tests using [Testcontainers](https://testcontainers.com/).

Tech stack:

- Java
- Maven (Wrapper)
- Spring Boot
- Testcontainers
- Docker
- Docker Compose
- ZITADEL

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

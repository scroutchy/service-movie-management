# Service Movie Management

This project is a microservice application designed to manage movies and their main characteristics within a cinema ecosystem. It is built using Kotlin and Spring Boot, leveraging reactive programming with WebFlux and MongoDB for data storage. The service integrates with other microservices (such as actor management) via Retrofit and supports secure communication using OAuth2 and Keycloak.

## Features
- Create, retrieve, and list movies with support for pagination and filtering by release dates.
- Integration with an external Actor service to enrich movie data with actor details.
- Reactive, non-blocking architecture using Project Reactor and Spring WebFlux.
- Kafka integration for event-driven communication and messaging.
- Secure endpoints with OAuth2 resource server and Keycloak.
- Container-ready with Docker and Kubernetes deployment manifests (Helm charts).
- Code quality and security checks integrated with GitLab CI/CD, SonarQube, and SAST/Container scanning.

## Tech Stack
- **Kotlin** (JVM 17)
- **Spring Boot** (WebFlux, Data MongoDB, Security, Validation)
- **Retrofit** (for HTTP client integration)
- **Project Reactor** (reactive programming)
- **Kafka** (messaging)
- **MongoDB** (reactive database)
- **Keycloak** (OAuth2 authentication)
- **Docker** & **Kubernetes** (deployment)
- **Gradle** (build tool)

## Getting Started
1. **Build the project:**
   ```sh
   ./gradlew build
   ```
2. **Run locally:**
   ```sh
   ./gradlew bootRun
   ```
3. **Build Docker image:**
   ```sh
   docker build -t service-movie-management .
   ```
4. **Run with Docker:**
   ```sh
   docker run -p 8080:8080 service-movie-management
   ```

## Configuration
Configuration is managed via `application.yaml` and environment variables for external services (MongoDB, Kafka, Keycloak, etc.). See the `ops/springboot-app` directory for Kubernetes/Helm deployment templates.

## Testing
Run tests and generate coverage reports with:
```sh
./gradlew test
```

## License
This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for details.

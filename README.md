# Nexora Backend

Nexora is a backend application consisting of multiple Spring Boot microservices:
- `userService`: Manages users and authentication. (Runs on port 9010)
- `postService`: Manages posts and media. (Runs on port 9020)

## Prerequisites
- Java 21
- Maven (or using the included Maven wrapper `./mvnw` in the service directories)

## Setup and Security
Database credentials and API secrets are externalized to profile-specific configuration files to keep the codebase secure.

1. **For `userService`**:
   - Open [userService/src/main/resources/application-local.properties](file:///Users/adityadebnath/Projects/nexora-backend/userService/src/main/resources/application-local.properties) and fill in your rotated Neon database password.

2. **For `postService`**:
   - Open [postService/src/main/resources/application-local.properties](file:///Users/adityadebnath/Projects/nexora-backend/postService/src/main/resources/application-local.properties) and fill in your rotated Neon database password and Cloudinary API credentials.

*(Note: `application-local.properties` is globally ignored in `.gitignore` so it will never be committed to Git).*

## Running the Services

By default, both services are configured with `spring.profiles.active=local` in their `application.properties`, which means they will automatically load their respective `application-local.properties` configurations.

To run each service:

```bash
# Run userService
cd userService
./mvnw spring-boot:run

# Run postService (in another terminal tab)
cd postService
./mvnw spring-boot:run
```

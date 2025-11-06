# ======== Stage 1: Build the application ========
FROM gradle:8.8-jdk17 AS build
WORKDIR /home/gradle/project

COPY gradlew .
COPY gradle gradle
RUN chmod +x gradlew

COPY build.gradle settings.gradle ./
COPY src src

RUN ./gradlew clean build -x test --no-daemon

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

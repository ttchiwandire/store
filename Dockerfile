FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -e -B -U -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -e -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache tini

RUN addgroup -S app && adduser -S app -G app
USER app

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
ENTRYPOINT ["/sbin/tini","--"]
CMD ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]

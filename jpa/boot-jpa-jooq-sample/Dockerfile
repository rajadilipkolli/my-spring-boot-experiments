FROM eclipse-temurin:25-jre-alpine as builder
WORKDIR /app
ARG JAR_FILE=target/boot-jpa-jooq-0.0.1-SNAPSHOT.jar 
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# the second stage of our build will copy the extracted layers
FROM eclipse-temurin:25-jre-alpine
LABEL maintainer="rajadilipkolli" \
      description="Spring Boot application with JPA and jOOQ integration" \
      version="0.0.1-SNAPSHOT"
WORKDIR /app
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ARG MAX_RAM_PERCENTAGE=75.0
ARG INITIAL_RAM_PERCENTAGE=50.0
ENV JAVA_OPTS="-XX:MaxRAMPercentage=${MAX_RAM_PERCENTAGE} -XX:InitialRAMPercentage=${INITIAL_RAM_PERCENTAGE}"
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} org.springframework.boot.loader.launch.JarLauncher"]

HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

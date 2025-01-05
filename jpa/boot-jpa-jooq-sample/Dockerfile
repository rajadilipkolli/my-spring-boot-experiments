FROM eclipse-temurin:21.0.5_11-jre-alpine as builder
WORKDIR application
ARG JAR_FILE=target/boot-jpa-jooq-0.0.1-SNAPSHOT.jar 
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# the second stage of our build will copy the extracted layers
FROM eclipse-temurin:21.0.5_11-jre-alpine
LABEL maintainer="rajadilipkolli" \
      description="Spring Boot application with JPA and jOOQ integration" \
      version="0.0.1-SNAPSHOT"
WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} org.springframework.boot.loader.launch.JarLauncher"]

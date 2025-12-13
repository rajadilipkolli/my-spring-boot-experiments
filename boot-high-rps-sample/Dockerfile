FROM eclipse-temurin:21-jdk-jammy
ARG JAR_FILE=target/boot-high-rps-sample-0.1.0-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]

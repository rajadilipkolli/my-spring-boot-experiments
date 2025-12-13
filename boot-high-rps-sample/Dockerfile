FROM eclipse-temurin:25-jre-jammy
ARG JAR_FILE=target/boot-high-rps-sample-0.1.0-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Run as non-root user
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

EXPOSE 8080
ENTRYPOINT ["java", \
    "-XX:+UseZGC", \
    "-Xms1g", \
    "-Xmx2g", \
    "-XX:+UseCompressedOops", \
    "-XX:MaxDirectMemorySize=512m", \
    "-jar", \
    "/app.jar"]

# Health check for container orchestration
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

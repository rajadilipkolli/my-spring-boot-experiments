plugins {
	java
	id("org.springframework.boot") version "3.0.6"
	id("io.spring.dependency-management") version "1.1.0"
	id("org.graalvm.buildtools.native") version "0.9.21"
}

group = "com.example.strategy.plugin"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
    implementation("org.springframework.plugin:spring-plugin-core:3.0.0")
    implementation("org.springframework.plugin:spring-plugin-metadata:3.0.0")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	
    //Swagger
    implementation ("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
    // For Tracing
    // OpenTelemetry version
    implementation ("io.micrometer:micrometer-tracing-bridge-otel")
    //For Latency Visualization
    implementation ("io.opentelemetry:opentelemetry-exporter-zipkin")
    // For pushing logs out
    runtimeOnly ("com.github.loki4j:loki-logback-appender:1.4.0")
	runtimeOnly("io.micrometer:micrometer-registry-prometheus")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
  useJUnitPlatform()
}

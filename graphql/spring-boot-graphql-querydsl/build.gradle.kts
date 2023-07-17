import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import java.io.Reader
import java.util.*

plugins {
	java
	id("org.springframework.boot") version "3.1.1"
	id("io.spring.dependency-management") version "1.1.2"
	id("com.diffplug.spotless") version "6.19.0"
	id("com.gorylenko.gradle-git-properties") version "2.4.1"
	id("org.owasp.dependencycheck") version "8.3.1"
	jacoco
	id("org.sonarqube") version "4.3.0.3225"
}

group = "com.example.graphql"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.liquibase:liquibase-core")
    // Printing Queries effectively
    implementation("net.ttddyy:datasource-proxy:1.9")

    // QueryDSL for JPA
    compileOnly("com.querydsl:querydsl-jpa-codegen:5.0.0:jakarta")
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")

    // Jakarta EE 10 as SB is using Jakarta EE 
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")

    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.1.0")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api:2.1.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.springframework.graphql:spring-graphql-test")
    testImplementation("org.projectlombok:lombok")
    testImplementation("org.springframework:spring-webflux")
}

sourceSets {
    named("main") {
        java {
            srcDirs("src/main/java", "build/generated/sources/annotationProcessor/java/main")
        }
    }
}

tasks.named("compileJava") {
    dependsOn("processResources")
}

tasks.withType<Test> {
	useJUnitPlatform()

	testLogging {
		events = setOf(PASSED, FAILED, SKIPPED)
		showStandardStreams = true
		exceptionFormat = FULL
	}
	finalizedBy(tasks.jacocoTestReport, tasks.jacocoTestCoverageVerification)
}

jacoco {
	toolVersion = "0.8.10"
	//reportsDirectory.set(layout.buildDirectory.dir("customJacocoReportDir"))
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(false)
		csv.required.set(false)
		html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco"))
	}
}

tasks.jacocoTestCoverageVerification {
	violationRules {
		rule {
			element = "BUNDLE"
			//includes = listOf("com.example.graphql.*")

			limit {
				counter = "LINE"
				value = "COVEREDRATIO"
				minimum = "0.47".toBigDecimal()
			}
		}
	}
}

gitProperties {
	failOnNoGitDirectory = false
	keys = listOf("git.branch",
		"git.commit.id.abbrev",
		"git.commit.user.name",
		"git.commit.message.full")
}

spotless {
	java {
		importOrder()
		removeUnusedImports()
		palantirJavaFormat("2.30.0")
		formatAnnotations()
	}
}

// Reference doc : https://jeremylong.github.io/DependencyCheck/dependency-check-gradle/configuration.html
dependencyCheck {
	// the default artifact types that will be analyzed.
	analyzedTypes = listOf("jar")
	// CI-tools usually needs XML-reports, but humans needs HTML.
	formats = listOf("HTML", "JUNIT")
	// Specifies if the build should be failed if a CVSS score equal to or above a specified level is identified.
	// failBuildOnCVSS = 8.toFloat()
	// Output directory where the report should be generated
	outputDirectory = "$buildDir/reports/dependency-vulnerabilities"
	// specify a list of known issues which contain false-positives to be suppressed
	//suppressionFiles = ["$projectDir/config/dependencycheck/dependency-check-suppression.xml"]
	// Sets the number of hours to wait before checking for new updates from the NVD, defaults to 4.
	cveValidForHours = 24
}

sonarqube {
	properties {
		property("sonar.sourceEncoding", "UTF-8")
		property("sonar.projectKey", "rajadilipkolli_mfscreener")
		property("sonar.organization", "rajadilipkolli")
		property("sonar.host.url", "https://sonarcloud.io")
		property("sonar.sources", "src/main/java")
		property("sonar.tests", "src/test/java")
		property("sonar.exclusions", "src/main/java/**/config/*.*,src/main/java/**/entities/*.*,src/main/java/**/models/*.*,src/main/java/**/exceptions/*.*,src/main/java/**/utils/*.*,src/main/java/**/*Application.*")
		property("sonar.test.inclusions", "**/*Test.java,**/*IntegrationTest.java,**/*IT.java")
		property("sonar.java.codeCoveragePlugin", "jacoco")
		property("sonar.coverage.jacoco.xmlReportPaths", "$buildDir/jacoco/test/jacoco.xml")
		property("sonar.junit.reportPaths", "$buildDir/test-results/test")
	}
}

import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import java.io.Reader
import java.util.*

plugins {
	java
	id("org.springframework.boot") version "3.1.2"
	id("io.spring.dependency-management") version "1.1.3"
	id("com.diffplug.spotless") version "6.20.0"
	id("com.gorylenko.gradle-git-properties") version "2.4.1"
	id("org.owasp.dependencycheck") version "8.3.1"
	jacoco
	id("org.sonarqube") version "4.3.0.3225"
//	id("org.graalvm.buildtools.native") version "0.9.24"
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
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    // Needed to run liquibase
    implementation("org.springframework:spring-jdbc")
    // Needed for pointcut
    implementation("org.springframework.boot:spring-boot-starter-aop")

    runtimeOnly ("org.postgresql:r2dbc-postgresql")
    runtimeOnly ("org.postgresql:postgresql")
    implementation("org.liquibase:liquibase-core")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.2.0")
    implementation("org.apache.commons:commons-lang3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.projectlombok:lombok")
    testImplementation("org.awaitility:awaitility")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:r2dbc")
    testImplementation("org.springframework.graphql:spring-graphql-test")
}

defaultTasks "bootRun"

springBoot {
    buildInfo()
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
			//includes = listOf("com.sivalabs.*")

			limit {
				counter = "LINE"
				value = "COVEREDRATIO"
				minimum = "0.59".toBigDecimal()
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
	outputDirectory = "${layout.buildDirectory}/reports/dependency-vulnerabilities"
	// specify a list of known issues which contain false-positives to be suppressed
	//suppressionFiles = ["$projectDir/config/dependencycheck/dependency-check-suppression.xml"]
	// Sets the number of hours to wait before checking for new updates from the NVD, defaults to 4.
	cveValidForHours = 24
}

sonarqube {
	properties {
		property("sonar.sourceEncoding", "UTF-8")
		property("sonar.projectKey", "rajadilipkolli_boot-graphql-webflux")
		property("sonar.organization", "rajadilipkolli")
		property("sonar.host.url", "https://sonarcloud.io")
		property("sonar.sources", "src/main/java")
		property("sonar.tests", "src/test/java")
		property("sonar.exclusions", "src/main/java/**/config/*.*,src/main/java/**/entities/*.*,src/main/java/**/models/*.*,src/main/java/**/exceptions/*.*,src/main/java/**/utils/*.*,src/main/java/**/*Application.*")
		property("sonar.test.inclusions", "**/*Test.java,**/*IntegrationTest.java,**/*IT.java")
		property("sonar.java.codeCoveragePlugin", "jacoco")
		property("sonar.coverage.jacoco.xmlReportPaths", "${layout.buildDirectory.dir("/jacoco/test/jacoco.xml")}")
		property("sonar.junit.reportPaths", "${layout.buildDirectory.dir("/test-results/test")}")
	}
}
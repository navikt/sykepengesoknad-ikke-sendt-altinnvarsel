import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.jlleitschuh.gradle.ktlint") version "12.0.2"
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"
}

group = "no.nav.helse.flex"
version = "1.0.0"
description = "sykepengesoknad-ikke-sendt-altinnvarsel"
java.sourceCompatibility = JavaVersion.VERSION_17

ext["okhttp3.version"] = "4.9.3" // Token-support tester trenger Mockwebserver.

repositories {
    mavenCentral()
    maven(url = "https://packages.confluent.io/maven/")
    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
}

val sykepengesoknadKafkaVersion = "2023.12.05-10.16-3ffa06f7"
val logstashLogbackEncoderVersion = "7.4"
val tjenestespesifikasjonerVersion = "2610.9b6de22"
val testContainersVersion = "1.19.3"
val kluentVersion = "1.73"
val tokenSupportVersion = "3.2.0"
val smCommonVersion = "1.1e5e122"
val gcsVersion = "2.17.2"
val gcsNioVersion = "0.126.3"
val commonsTextVersion = "1.11.0"
val jakartaActivationVersion = "2.0.1"
val cxfVersion = "3.5.5"
val bindApiVersion = "2.3.3"
val jaxbRuntimeVersion = "2.4.0-b180830.0438"
val jwsApiVersion = "2.1.0"
val jaxWsApiVersion = "2.3.1"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.hibernate.validator:hibernate-validator")
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.slf4j:slf4j-api")
    implementation("org.aspectj:aspectjrt")
    implementation("org.aspectj:aspectjweaver")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("no.nav.security:token-client-spring:$tokenSupportVersion")
    implementation("no.nav.security:token-validation-spring:$tokenSupportVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashLogbackEncoderVersion")
    implementation("no.nav.helse.flex:sykepengesoknad-kafka:$sykepengesoknadKafkaVersion")
    implementation("org.apache.commons:commons-text:$commonsTextVersion")
    implementation("com.sun.activation:jakarta.activation:$jakartaActivationVersion")
    implementation("org.apache.cxf:cxf-spring-boot-starter-jaxws:$cxfVersion")
    implementation("no.nav.tjenestespesifikasjoner:behandle-altinnmelding-v1-tjenestespesifikasjon:$tjenestespesifikasjonerVersion")

    // saaj-impl-2.0.0 uses the jakarta namespace instead of javax.
    implementation("com.sun.xml.messaging.saaj:saaj-impl:1.5.3")
    // https://stackoverflow.com/questions/71095913/what-is-the-difference-between-jaxb-impl-and-jaxb-runtime
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:$bindApiVersion")
    implementation("org.glassfish.jaxb:jaxb-runtime:$jaxbRuntimeVersion")
    implementation("jakarta.jws:jakarta.jws-api:$jwsApiVersion")
    implementation("javax.xml.ws:jaxws-api:$jaxWsApiVersion")

    testImplementation(platform("org.testcontainers:testcontainers-bom:$testContainersVersion"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.awaitility:awaitility")
    testImplementation("no.nav.security:token-validation-spring-test:$tokenSupportVersion")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("app.jar")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
        if (System.getenv("CI") == "true") {
            kotlinOptions.allWarningsAsErrors = true
        }
    }
}
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("STANDARD_OUT", "STARTED", "PASSED", "FAILED", "SKIPPED")
        exceptionFormat = TestExceptionFormat.FULL
    }
}

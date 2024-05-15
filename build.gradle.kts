import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.5"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.23"
}

group = "no.nav.helse.flex"
version = "1.0.0"
description = "sykepengesoknad-ikke-sendt-altinnvarsel"
java.sourceCompatibility = JavaVersion.VERSION_21

ext["okhttp3.version"] = "4.12" // Token-support tester trenger MockWebServer.

repositories {
    mavenCentral()
    maven(url = "https://packages.confluent.io/maven/")
    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
}

val sykepengesoknadKafkaVersion = "2024.05.04-08.31-672172ee"
val logstashLogbackEncoderVersion = "7.4"
val tjenestespesifikasjonerVersion = "2633.1685ed5"
val testContainersVersion = "1.19.7"
val kluentVersion = "1.73"
val tokenSupportVersion = "4.1.4"
val smCommonVersion = "1.1e5e122"
val gcsVersion = "2.17.2"
val gcsNioVersion = "0.126.3"
val commonsTextVersion = "1.12.0"
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
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.hibernate.validator:hibernate-validator")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashLogbackEncoderVersion")
    implementation("no.nav.security:token-validation-spring:$tokenSupportVersion")
    implementation("no.nav.security:token-client-spring:$tokenSupportVersion")
    implementation("no.nav.helse.flex:sykepengesoknad-kafka:$sykepengesoknadKafkaVersion")
    implementation("org.aspectj:aspectjrt")
    implementation("org.aspectj:aspectjweaver")
    implementation("org.apache.commons:commons-text:$commonsTextVersion")
    implementation("org.apache.cxf:cxf-spring-boot-starter-jaxws:$cxfVersion")
    implementation("com.sun.activation:jakarta.activation:$jakartaActivationVersion")
    implementation("no.nav.tjenestespesifikasjoner:behandle-altinnmelding-v1-tjenestespesifikasjon:$tjenestespesifikasjonerVersion")

    // saaj-impl-2.0.0 uses the jakarta namespace instead of javax.
    implementation("com.sun.xml.messaging.saaj:saaj-impl:1.5.3")
    // https://stackoverflow.com/questions/71095913/what-is-the-difference-between-jaxb-impl-and-jaxb-runtime
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:$bindApiVersion")
    implementation("jakarta.jws:jakarta.jws-api:$jwsApiVersion")
    implementation("javax.xml.ws:jaxws-api:$jaxWsApiVersion")
    implementation("org.glassfish.jaxb:jaxb-runtime:$jaxbRuntimeVersion")

    testImplementation(platform("org.testcontainers:testcontainers-bom:$testContainersVersion"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.awaitility:awaitility")
    testImplementation("no.nav.security:token-validation-spring-test:$tokenSupportVersion")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xjsr305=strict")
        if (System.getenv("CI") == "true") {
            allWarningsAsErrors.set(true)
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
        jvmArgs("-XX:+EnableDynamicAgentLoading")
        testLogging {
            events("PASSED", "FAILED", "SKIPPED")
            exceptionFormat = TestExceptionFormat.FULL
        }
        failFast = false
    }
}

tasks {
    bootJar {
        archiveFileName = "app.jar"
    }
}

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.5.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.5.31"
}

group = "no.nav.helse.flex"
version = "1.0.0"
description = "sykepengesoknad-ikke-sendt-altinnvarsel"
java.sourceCompatibility = JavaVersion.VERSION_16

buildscript {
    repositories {
        maven("https://plugins.gradle.org/m2/")
    }
    dependencies {
        classpath("org.jlleitschuh.gradle:ktlint-gradle:10.2.0")
    }
}

ext["okhttp3.version"] = "4.9.0" // For at token support testen kjører

apply(plugin = "org.jlleitschuh.gradle.ktlint")

val githubUser: String by project
val githubPassword: String by project

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://packages.confluent.io/maven/")
    maven {
        url = uri("https://maven.pkg.github.com/navikt/maven-release")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
}

val testContainersVersion = "1.16.0"
val tokenSupportVersion = "1.3.9"
val logstashLogbackEncoderVersion = "6.6"
val kluentVersion = "1.68"
val syfoKafkaVersion = "2021.07.20-09.39-6be2c52c"
val tjenestespesifikasjonerVersion = "1.2020.01.20-15.44-063ae9f84815"
val cxfVersion = "3.4.5"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("no.nav.security:token-client-spring:$tokenSupportVersion")
    implementation("no.nav.security:token-validation-spring:$tokenSupportVersion")
    implementation("org.slf4j:slf4j-api")
    implementation("org.flywaydb:flyway-core")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.postgresql:postgresql")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashLogbackEncoderVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("no.nav.syfo.kafka:felles:$syfoKafkaVersion")
    implementation("org.aspectj:aspectjrt")
    implementation("org.aspectj:aspectjweaver")
    implementation("org.hibernate.validator:hibernate-validator")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.apache.commons:commons-text:1.9")

    implementation("com.sun.activation:jakarta.activation:2.0.1")
    implementation("com.sun.xml.bind:jaxb-impl:3.0.2")
    implementation("com.sun.xml.bind:jaxb-core:3.0.2")
    implementation("com.sun.xml.messaging.saaj:saaj-impl:1.5.1")
    implementation("javax.xml.ws:jaxws-api:2.3.1")
    implementation("javax.jws:javax.jws-api:1.1")
    implementation("no.nav.tjenestespesifikasjoner:behandle-altinnmelding-v1-tjenestespesifikasjon:$tjenestespesifikasjonerVersion")
    implementation("org.apache.cxf:cxf-spring-boot-starter-jaxws:$cxfVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:kafka:$testContainersVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersVersion")
    testImplementation("no.nav.security:token-validation-spring-test:$tokenSupportVersion")
    testImplementation("org.awaitility:awaitility")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("app.jar")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "16"
        if (System.getenv("CI") == "true") {
            kotlinOptions.allWarningsAsErrors = true
        }
    }
}
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("STANDARD_OUT", "STARTED", "PASSED", "FAILED", "SKIPPED")
    }
}

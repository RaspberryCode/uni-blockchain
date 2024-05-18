plugins {
    kotlin("jvm") version "1.9.23" apply false
    kotlin("plugin.spring") version "1.9.23" apply false
    id("org.springframework.boot") version "3.2.5" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

allprojects {
    group = "pl.softwarekitchen.uni.blockchain"
    version = "1.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

//    dependencies {
//        implementation("org.springframework.boot:spring-boot-starter")
//        implementation("org.springframework.boot:spring-boot-starter-web")
//        implementation("org.springframework.boot:spring-boot-starter-actuator")
//        implementation("org.springdoc:springdoc-openapi-ui:1.8.0")
//        implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")
//    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
            jvmTarget = "21"
        }
    }
}

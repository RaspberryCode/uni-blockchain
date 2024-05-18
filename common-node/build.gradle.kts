object Versions {
    const val OPEN_API = "2.2.0"
    const val RESILIENCE4J = "2.2.0"
    const val JJWT = "0.11.5"
}

dependencies {
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("org.springframework.boot:spring-boot-starter-aop")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui:${Versions.OPEN_API}")
    api("io.github.resilience4j:resilience4j-spring-boot3:${Versions.RESILIENCE4J}")
    api("io.jsonwebtoken:jjwt-api:${Versions.JJWT}")

    runtimeOnly("io.jsonwebtoken:jjwt-impl:${Versions.JJWT}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${Versions.JJWT}")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
    archiveBaseName.set("common-node")
}

tasks.register<Jar>("sharedJar") {
    archiveBaseName.set("common")
    from(sourceSets.main.get().output)
}

artifacts {
    add("archives", tasks.named("sharedJar"))
}
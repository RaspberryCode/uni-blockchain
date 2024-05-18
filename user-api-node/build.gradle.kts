dependencies {
    implementation(project(":common-node"))
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}

tasks.named<Jar>("jar") {
    enabled = false
}
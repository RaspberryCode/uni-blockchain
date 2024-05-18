dependencies {
    implementation(project(":common-node"))
}

tasks.named<Jar>("jar") {
    enabled = false
}
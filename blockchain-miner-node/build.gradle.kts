plugins {
    kotlin("jvm") version "1.9.23"
    application
}

// Define the main class for the application
application {
    mainClass.set("miner.MainKt")
}

group = "pl.softwarekitchen.uni.blockchain"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "miner.MainKt")
    }
    // Configure the JAR to include all dependencies by collecting and adding them from the runtime classpath
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
}

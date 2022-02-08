plugins {
    kotlin("jvm") // Standalone: Add version
    id("com.github.johnrengelman.shadow") version "7.1.0"
    application
}

group = "com.github.doip-sim-ecu" // Change this to your organization
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))

    // You should use the latest released stable version
    implementation("com.github.doip-sim-ecu:doip-sim-ecu-dsl:0.5.0-beta")
//    implementation("com.github.doip-sim-ecu:doip-sim-ecu-dsl:main-SNAPSHOT")
}

tasks {
    application {
        mainClass.set("MainKt")
    }
}

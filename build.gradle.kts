plugins {
    val kotlinVersion = "1.7.0"
    kotlin("jvm") version kotlinVersion
    id("com.github.johnrengelman.shadow") version "7.1.0"
    application
}

group = "com.github.doip-sim-ecu" // Change this to your organization
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
//    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))

    // You should use the latest released stable version
    implementation("io.github.doip-sim-ecu:doip-sim-ecu-dsl:0.9.5")
//    implementation("com.github.doip-sim-ecu:doip-sim-ecu-dsl:main-SNAPSHOT")
}

tasks {
    application {
        mainClass.set("MainKt")
    }
}

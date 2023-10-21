import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.9.10"
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
    implementation("io.github.doip-sim-ecu:doip-sim-ecu-dsl:0.10.1")
}

tasks {
    application {
        mainClass.set("MainKt")
    }
}

kotlin {
    jvmToolchain(8)
}

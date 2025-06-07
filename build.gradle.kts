plugins {
    val kotlinVersion = "2.1.21"
    kotlin("jvm") version kotlinVersion
    id("com.github.johnrengelman.shadow") version "8.1.0"
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
    implementation("io.github.doip-sim-ecu:doip-sim-ecu-dsl:0.20.1-beta")

    implementation("ch.qos.logback:logback-classic:1.5.18") // EPL-1.0
}

tasks {
    application {
        mainClass.set("MainKt")
    }
}

kotlin {
    jvmToolchain(8)
}

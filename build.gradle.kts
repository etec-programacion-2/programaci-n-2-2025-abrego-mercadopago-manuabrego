plugins {
    kotlin("jvm") version "2.2.0"
    application
}

group = "com.billetera"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.xerial:sqlite-jdbc:3.46.0.0")
}

application {
    mainClass.set("com.billetera.MainKt")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

plugins {
    kotlin("jvm") version "2.2.10"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin standard library
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    
    // SQLite JDBC driver
    implementation("org.xerial:sqlite-jdbc:3.46.0.0")
    
    // SLF4J for logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    
    // Coroutines para operaciones asíncronas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.mockito:mockito-core:5.6.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
}

application {
    mainClass.set("org.example.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

// Tarea personalizada para ejecutar el setup de la base de datos
tasks.register<Exec>("setupDatabase") {
    group = "database"
    description = "Inicializa la base de datos SQLite"
    commandLine("bash", "setup_db.sh")
}

// Tarea para limpiar la base de datos
tasks.register<Delete>("cleanDatabase") {
    group = "database"
    description = "Elimina la base de datos existente"
    delete("database/billetera.db")
}
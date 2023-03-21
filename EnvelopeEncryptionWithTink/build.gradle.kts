import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17
val timestamp = System.currentTimeMillis()
val containerVersion = timestamp

plugins {
    java
    kotlin("jvm") version "1.8.0"
    `maven-publish`
    id("com.diffplug.spotless") version "6.2.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.google.com/")
    }
}

dependencies {
    // https://mvnrepository.com/artifact/com.google.android/annotations
    implementation("com.google.android:annotations:4.1.1.4")

//    implementation("androidx.annotation:annotation:1.3.0")
    implementation("com.google.crypto.tink:tink:1.8.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
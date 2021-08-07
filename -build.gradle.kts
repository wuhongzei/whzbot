import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    java
    kotlin("jvm") version "1.4.32"
}

group = "org.example"
version = "2.1.1"

repositories {
    mavenCentral()
}

tasks.withType(KotlinJvmCompile::class.java) {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    // 开发时使用 mirai-core-api，运行时提供 mirai-core

    api("net.mamoe:mirai-core-api:${properties["version.mirai"]}")
    runtimeOnly("net.mamoe:mirai-core:${properties["version.mirai"]}")

    // 可以简单地只添加 api("net.mamoe:mirai-core:2.6.1")
}

jar {
    manifest {
        attributes "Main-Class": "org.example.whzbot.JavaMain"
        attributes "Version": project.version
    }

    from {
        configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) }
    }
}
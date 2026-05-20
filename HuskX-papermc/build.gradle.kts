plugins {
    kotlin("jvm") version libs.versions.kotlin.get()
    alias(libs.plugins.paperweight.userdev)
}

group = "kr.doka.lab.huskx"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle(libs.versions.paper.api.get())
    implementation(libs.kotlin.stdlib)

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}
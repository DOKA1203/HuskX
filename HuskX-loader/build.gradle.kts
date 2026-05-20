plugins {
    id("java")
    alias(libs.plugins.paperweight.userdev)
}

group = "kr.doka.lab.huskx"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    paperweight.paperDevBundle(libs.versions.paper.api.get())
}

tasks.test {
    useJUnitPlatform()
}
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

val writeHuskxProperties by tasks.registering(WriteProperties::class) {
    description = "Generates HuskX properties file."
    destinationFile = layout.buildDirectory.file("generated-resources/huskx.properties")
    property("github.repo", "DOKA1203/HuskX")
    property("github.asset-name", "huskx-papermc.jar")
    property("main-class", "kr.doka.lab.huskx.HuskXPlugin")
    property("kotlin.version", libs.versions.kotlin.get())
}

sourceSets.main {
    resources.srcDir(layout.buildDirectory.dir("generated-resources"))
}

tasks.jar {
    archiveFileName.set("huskx-loader.jar")
}

tasks.processResources {
    dependsOn(writeHuskxProperties)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.test {
    useJUnitPlatform()
}
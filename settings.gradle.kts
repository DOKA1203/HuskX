pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "HuskX"

include("HuskX-loader")
include("HuskX-papermc")
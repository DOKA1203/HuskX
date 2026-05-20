# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build commands

```bash
./gradlew build                      # build all modules
./gradlew :HuskX-loader:build        # build loader JAR only
./gradlew :HuskX-papermc:build       # build main plugin JAR only
./gradlew :HuskX-loader:compileJava  # compile loader without packaging
./gradlew :HuskX-papermc:compileKotlin
```

There are no tests yet (`test` tasks report NO-SOURCE). The configuration cache and parallel builds are enabled in `gradle.properties`.

## Architecture

HuskX is a self-updating Paper plugin split into two JARs that are deployed separately:

**`HuskX-loader`** (Java, `plugins/` folder — never updated)
- Implements Paper's `PluginLoader` API (`@UnstableApiUsage`)
- `HuskXLoader.classloader()` runs before Paper resolves the plugin's main class
- Reads `huskx.properties` (bundled resource) for `github.repo` and `github.asset-name`
- Hits the GitHub Releases API to find the latest tag, compares against `plugins/HuskX/cache/version.txt`
- Downloads the release asset to `plugins/HuskX/cache/<asset-name>` if outdated
- Injects the cached JAR into the plugin classpath via `JarLibrary`

**`HuskX-papermc`** (Kotlin, distributed via GitHub Releases — auto-downloaded)
- A standard `JavaPlugin` subclass; its classpath is built by the loader above
- `HuskXPlugin` is the entry point declared in `paper-plugin.yml`

**Startup sequence:**
```
Server start → HuskXLoader.classloader()
  → fetch latest GitHub release tag
  → compare with plugins/HuskX/cache/version.txt
  → download huskx-papermc.jar if version differs
  → JarLibrary injects JAR → Paper resolves main class → onEnable()
```

Offline fallback: the cached JAR is reused if GitHub is unreachable. Startup fails only when there is no cache at all.

## Gradle multi-project notes

`paperweight.userdev` must be declared in the **root** `build.gradle.kts` with `apply false`:

```kotlin
plugins {
    alias(libs.plugins.paperweight.userdev) apply false
}
```

Without this, applying the plugin in both subprojects causes a Gradle classloader conflict (`UserdevSetup$Inject_` cast failure). The subprojects then apply it normally via `alias(libs.plugins.paperweight.userdev)` without specifying a version.

Versions are managed in `gradle/libs.versions.toml`. The Paper dev bundle version (`paper-api`) must be a valid PaperMC build number in the form `<MC version>.build.+`.

## Key configuration

`HuskX-loader/src/main/resources/huskx.properties`:
```properties
github.repo=doka1203/huskx-papermc
github.asset-name=huskx-papermc.jar
main-class=kr.doka.lab.huskx.HuskXPlugin
```

`HuskX-loader/src/main/resources/paper-plugin.yml` declares the `loader:` class (must point to `HuskXLoader`) and the `main:` class that lives in the downloaded JAR.
# HuskX

A Paper plugin framework that automatically updates itself at server startup using Paper's `PluginLoader` API.

## How it works

HuskX splits the plugin into two separate JARs:

- **huskx-loader** — A lightweight shell JAR placed in the `plugins/` folder. Contains only the `PluginLoader` and `paper-plugin.yml`. Never needs to be updated.
- **huskx-paper** — The actual plugin logic, hosted on GitHub Releases. Downloaded and loaded automatically on every server start.

On startup, `huskx-loader` checks GitHub for the latest release of `huskx-paper`, downloads it if needed, and injects it into the classpath via `JarLibrary` before Paper resolves the `main` class — so `onEnable()` is called normally with no extra wiring.

```
Server start
  └── HuskXLoader.classloader()
        ├── Check GitHub Releases for latest version
        ├── Compare with cached version
        ├── Download if outdated (cached to plugins/HuskX/cache/)
        └── JarLibrary → huskx-paper.jar injected into classpath
              └── Paper resolves main class → onEnable() called
```

## Offline support

Once `huskx-paper` has been downloaded at least once, the server can start without an internet connection. The cached JAR is reused automatically.

| Situation | Result |
|---|---|
| Online, new version available | Download and use new version |
| Online, already up to date | Use cache |
| Offline, cache exists | Use cache with a warning |
| Offline, no cache | Startup failure |

## Installation

1. Drop `huskx-loader.jar` into your `plugins/` folder.
2. Start the server — `huskx-paper` is downloaded automatically.

No configuration needed.

## Project structure

```
HuskX/
├── huskx-loader/   # Java — PluginLoader only
└── huskx-paper/    # Kotlin — actual plugin logic
```

## Requirements

- Paper 1.19+
- Java 21+
- Internet connection on first run
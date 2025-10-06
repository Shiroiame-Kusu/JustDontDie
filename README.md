# JustDontDie (Gradle)

A Spigot/Paper plugin: If a player would die and is holding any item, consume one of that item and prevent death, applying totem-like effects and animation. On Paper, the actual consumed item is shown in the totem animation; on Spigot, a close particle/sound fallback is used.

## Build (Gradle)

Prerequisites:
- JDK 21
- Gradle 8+ (if you don't have Gradle, install it or add a wrapper; see below)

Commands:

```zsh
# from project root
gradle --version
gradle clean build
```

Artifacts:
- The plugin JAR will be at `build/libs/JustDontDie-<version>.jar`.

## Config

`src/main/resources/config.yml`
- `disable-vanilla-totem`: Whether to cancel vanilla totem resurrection logic (default: false).

## Notes
- This project used to be Maven; it's now Gradle-based. The old `pom.xml` can be removed.
- If you prefer using a Gradle Wrapper, run this with a local Gradle install:

```zsh
gradle wrapper --gradle-version 8.9
./gradlew clean build
```

This will add `gradlew` scripts to the repo so you don't need a system Gradle.


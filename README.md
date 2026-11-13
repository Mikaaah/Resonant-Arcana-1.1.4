# Resonant Arcana 1.1.4

Clean repository for the 1.1.4 update: **Minecraft 1.21.1 / NeoForge 21.1.219 / Java 21**. Requires Iron’s Spells ’n Spellbooks 1.21.1-3.16.3 and its normal dependencies. Install the same Resonant Arcana version on client and server.

[Download verified JAR](downloads/resonant-arcana-1.1.4-neoforge-1.21.1.jar) · [Changelog](CHANGELOG.md) · [Configuration](CONFIGURATION.md) · [Player guides](wiki/Home.md)

## Update

- Separate `resonantarcana-spell-visibility.toml` with individual true/false settings for all 228 spells.
- **Mods → Resonant Arcana → Config → First-person view**, with search, paging, reset defaults, and immediate saving.
- `true` clears nearby spell particles from your aim; `false` preserves full effects. Lightning defaults to `true`; other spells default to `false`.
- Includes the 1.1.2 beam visibility/height fixes and 1.1.3 God spell animation, sound, and visual changes.

## Build

With Java 21 installed:

```sh
./gradlew clean visibilityChecks jar
```

Windows: `gradlew.bat clean visibilityChecks jar`.

Output: `build/libs/resonant-arcana-1.1.4.jar`.

**Source status:** this is the current source update over the exact preserved 1.1.3 binary baseline in `baseline/`. The complete original 1.1.2/1.1.3 source was not recovered. Unchanged classes/resources come from that baseline; current Java files replace their counterparts. This repository is buildable but is not a complete recovered source tree. See [development notes](docs/DEVELOPMENT.md).

The baseline is a build input, not an extra mod to install. Third-party dependency JARs are not bundled. Optional integration classes/assets are preserved from 1.1.3.

## Verification and history

[Verification reports](verification/1.1.4/) record the previous configuration, client, and server checks. A fresh build is also checked before this repository is published. Optional Bosses’Rise/Alex’s Caves runtime tests were not repeated for 1.1.4.

The [original repository](https://github.com/Mikaaah/Resonant-Arcana) retains the earlier history. This separate repository starts with the 1.1.4 workspace and excludes the 18 old release JARs and retired school-wand pages/generators.

[MIT license](LICENSE) · [Audio credits](AUDIO_CREDITS.md) · [Third-party notices](THIRD_PARTY_NOTICES.md)

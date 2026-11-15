<p align="center"><img src="https://i.imgur.com/2FIsYgK.png" alt="Resonant Arcana" width="100%"></p>

<h1 align="center">Resonant Arcana</h1>

<p align="center"><strong>Nine schools of magic, legendary spellbooks and rituals big enough to build a character around.</strong></p>

<p align="center"><a href="https://www.curseforge.com/minecraft/mc-mods/resonant-arcana">Downloads</a> · <a href="https://github.com/Mikaaah/Resonant-Arcana-1.1.4/wiki">Player guide</a> · <a href="https://github.com/Mikaaah/Resonant-Arcana-1.1.4/issues">Report an issue</a> · <a href="https://github.com/Mikaaah/Resonant-Arcana-1.1.4/blob/main/CHANGELOG.md">Changelog</a> · <a href="https://discord.gg/CRYk3uDPVt">Discord</a></p>

<p align="center"><img src="https://img.shields.io/badge/Minecraft-1.21.1-5b8731?style=flat-square" alt="Minecraft 1.21.1"> <img src="https://img.shields.io/badge/NeoForge-d77828?style=flat-square" alt="NeoForge"> <a href="https://mikaaah.github.io/TownsAndKingdoms/projects/"><img src="https://img.shields.io/badge/Towns_%26_Kingdoms-Modding_Hub-7f384a?style=flat-square" alt="Towns & Kingdoms Modding Hub"></a></p>

**Current build: 1.2.0** · Minecraft 1.21.1 · NeoForge · Java 21

[Download 1.2.0](downloads/resonant-arcana-1.2.0-neoforge-1.21.1.jar) · [Release notes](CHANGELOG.md) · [Configuration](wiki/Configuration.md)

## What's new in 1.2.0

- **Five Genshin-inspired Cataclysm bursts:** Zhongli’s Planet Befall, Qiqi’s Preserver of Fortune, Raiden’s Musou Shinsetsu, Venti’s Wind’s Grand Ode and Ganyu’s Celestial Shower.
- **20% lower direct damage** for all 43 existing offensive God/Cataclysm spells.
- Original icons, Legendary scroll recipes, casting poses, character search and per-spell visibility controls. **233 spells** with all integrations.
- [Burst mechanics, base stats and recipes](docs/ELEMENTAL_BURSTS.md).

The 1.1.4 first-person view controls and recent casting/audio fixes are preserved.

## Magic worth building around

- **Nine magic schools**, with searchable categories, readable spell information and one Spells creative tab.
- **Orbital Ray**: gather a growing orb for 15 seconds, launch it skyward, then unleash a vertical arcane beam and nuclear-style explosion. No TNT model or terrain destruction.
- **Faster Cataclysms**: 6-second base casts for spells without supplied recordings.
- **Twenty God-tier rituals**: nine school spells, six cave arts and five boss arts. Base cast time: 10 seconds; mana: 600.
- **Thunder of the Heavens** marks a vast battlefield and rains down 200 violet lightning strikes.
- **Nine legendary spellbooks** with distinct equipment perks and 3D models, plus a searchable debug wand for creative testing. [Grimoire stats and recipes](docs/GRIMOIRES.md).
- **Native Tremorzilla breath** with green, purple and orange variants; following hand-cast beams and themed boss effects.
- **Fifty themed scroll recipes** for addon and God-tier magic, using the appropriate cave or boss materials.

## Install

| Required | Supported version |
| --- | --- |
| Minecraft / Java | 1.21.1 / Java 21 |
| NeoForge | 21.1.219 or later within 21.1.x |
| Iron’s Spells ’n Spellbooks | 1.21.1-3.16.3, with its required dependencies |

Install the same Resonant Arcana version on **client and server**. Equip an inscribed spellbook in the **Curios spellbook slot** and use Iron’s normal casting controls.

| Optional addon | Unlocks |
| --- | --- |
| Bosses’Rise 2.1.2 | 19 boss arts for Kraken, Yeti, Helvar, Sandworm and Dragon |
| Alex’s Caves 2.0.10 + Citadel | 30 cave arts across all six cave areas |
| AAAParticles | Optional effects for selected other spells; God-tier spells and Doom Beam use native particle routes |

**Both gameplay addons are optional.** The core mod works without them; unsupported addon versions leave the corresponding adapter disabled.

## Explore the wiki

| Guide | Includes |
| --- | --- |
| [Getting started](https://github.com/Mikaaah/Resonant-Arcana-1.1.4/wiki/Getting-Started) | Installation, spellbooks and your first scroll |
| [Equipment](https://github.com/Mikaaah/Resonant-Arcana-1.1.4/wiki/Equipment) | Spellbook slots, perks and the debug archive |
| [God-tier magic](https://github.com/Mikaaah/Resonant-Arcana-1.1.4/wiki/God) | All twenty ultimate rituals and their mechanics |
| [Optional addons](https://github.com/Mikaaah/Resonant-Arcana-1.1.4/wiki/Optional-Addons) | Cave themes, boss arts and Tremorzilla colour items |
| [Configuration](https://github.com/Mikaaah/Resonant-Arcana-1.1.4/wiki/Configuration) | Server balance, controls, visuals and sounds |

## Build and source status

With Java 21, run `./gradlew clean visibilityChecks burstChecks jar` (Windows: `gradlew.bat clean visibilityChecks burstChecks jar`). Output: `build/libs/resonant-arcana-1.2.0.jar`.

The current source update builds over the exact preserved 1.1.3 JAR in `baseline/`, which supplies unchanged classes and resources. This is a reproducible source overlay; the complete original 1.1.2/1.1.3 source has not been recovered. See [development notes](docs/DEVELOPMENT.md).

Validation for this update is recorded in [verification/1.2.0](verification/1.2.0/). Earlier 1.1.4 reports remain version-labelled.

## Credits and license

By **Towns & Kingdoms**. [MIT license](LICENSE). Iron’s, Bosses’Rise and Alex’s Caves assets remain owned by their respective authors and are used from the installed mods. See [audio credits](AUDIO_CREDITS.md) for supplied recordings. New God-tier spell icons are temporary while final artwork is prepared.

## Pull up a chair

Questions, builds, feedback, server news and the occasional wildly overambitious factory: join the **Towns & Kingdoms Modding Hub**.

[![Join the Towns & Kingdoms Discord](https://i.imgur.com/Ojgcccm.png)](https://discord.gg/CRYk3uDPVt)

**[Join Discord](https://discord.gg/CRYk3uDPVt)** · [Explore all projects](https://mikaaah.github.io/TownsAndKingdoms/projects/) · [CurseForge projects](https://www.curseforge.com/members/mikaaah/projects)

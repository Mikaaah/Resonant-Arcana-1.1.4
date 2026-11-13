# Changelog

## 1.1.4 — Per-spell first-person visibility

- Added a separate `resonantarcana-spell-visibility.toml` client file with true/false settings for all 228 spells.
- Added Mods → Resonant Arcana → Config → First-person view, with search, pages, reset and immediate saving.
- true clears nearby particles from the first-person aim; false keeps full particles. Distant and third-person effects remain visible.
- Carried exact spell identity through particle packets and tracked existing particles for live changes.
- Lightning defaults to clear view; other spells default to full effects. Update both client and server.

## 1.1.3 — God spell casting, sound and forms

- Charge animations hold until release/cancellation; Eventide Horizon uses a steady charge pose and compact effect.
- Reworked God spell audio with timed sun, moon, eclipse and Helvar impacts, including empty ground.
- Removed dragon wings/flight and Tremorzilla’s head; its beam casts from the hands.
- Primordial Sovereign becomes a volcanic eruption with native tephra while preserving its damage and allied buffs.

## 1.1.2 — Particle visibility and launch height

- Restored non-lightning particles, including Arcane Devastator and Doom Beam.
- Restricted the former shared clear-view filter to lightning spells.
- Raised beams/projectiles and Glacial Rupture so they can pass over one-block cover; taller walls still block them.

## 1.0.1 — Native spell spectacle and book lighting

- Corrected inventory lighting on all nine 3D book model wrappers while retaining their original textures.
- Restored native vanilla and Iron's particle choreography to every God-tier spell; the custom formations now support rather than suppress their particle scenes.
- Reworked Soul Ascendancy's soul/sculk presentation and Helvar's Final Edict's final sword impact.
- Added a small layer of vanilla glints to Rho Aias's existing rose shield.
- Rebuilt Doom Beam as an emerald particle stream, removing its beacon mesh and black charge orb.
- Excluded God-tier spells and Doom Beam from the generic optional AAA seals.
- Retained recorded audio, spell IDs, spell balance and the 1.0.1 icon palettes.

- Recolored all 227 spell icons to match their school or established visual theme.
- Fire uses orange, red and yellow; Ice uses frost blue; Holy uses ivory and gold; Blood uses crimson; Nature uses green; Ender uses violet/magenta; Lightning uses electric blue; Eldritch uses sculk teal; Evocation uses amethyst purple.
- Preserved special palettes for emerald Doom Beam, purple Tremorzilla Cataclysm, rose-violet Rho Aias, amber and candy magic, ocean spells, sandworm arts, ember-red Helvar arts and red/blue magnetic poles.
- Kept the original 16x16 pixel geometry, transparency, icon assignments and all nine grimoire models.
- Added a complete per-spell palette audit and deterministic regeneration so reused artwork no longer inherits an unrelated color.

## 1.0.0 — Illustrated spells and grimoire collection

- Replaced all 227 spell icons with themed artwork from the supplied icon set; matching designs are reused where appropriate.
- Added six native, inscribable grimoires: Book of the Dead, Mythic Grimoire, Grimoire of Blood, Grimoire of Stars, Grimoire of the Forest Spirit and Golden Grimoire.
- Added school-specific equipped perks, native Curios spellbook tags and survival recipes for each new grimoire.
- Integrated the supplied 3D models for all nine books, retaining original flat icons for inventory, hotbar and JEI.
- Preserved the 0.8.1 config startup guards, pre-God audio restoration, recorded voices, beam fixes, Rho Aias and God-tier effects.
- The release version is 1.0.0. Historical public and development versions remain recorded aliases and are not replaced.

## 0.8.1

- Fixed client ticks reading particle settings before the client config has loaded.
- Clear queued particle ripples and reset particle allowances outside a world or while the config is unavailable.
- Guarded wand HUD, mouse input and optional AAA effects against unloaded client settings.

## 0.8.0 — Native audio restoration and God-tier sculptures

- Removed the generated replacement audio. Restored the original pre-God Minecraft and Iron’s Spells routing, including Doom’s staggered native layers.
- God-tier and the twenty tactical spells now use existing Minecraft sounds, with individual cue profiles across charge, release and impact. Charge cues run for the cast and stop through the existing caster-owned cancellation channel.
- Kept the original Explosion, I Am Atomic, Arcane Devastator, Doom Beam and Angel Scream recordings unchanged.
- Replaced the shared God-tier charge and field visuals with twenty distinct animated mesh formations: crystalline armillary, blood moon, solar corona, black-hole accretion, lightning crown, ice lattice, flowering world tree, winged cathedral, spirit procession, magnetic poles, amber fossil, reactor array, layered tides, eclipse eye, peppermint constellation, fracture crown, frozen heart, kraken vortex, sandworm maw and dragon wings.
- Retained Rho Aias’s giant flower, three shield discs, mana sustain and movement cancellation. Retained first-person beam clearance and existing spell mechanics.
- Minecraft 1.21.1 / NeoForge / Java 21. Install the same version on both client and server.


## 0.3.1

- Refined Orbital Ray with a smaller, violet-only charge orb and half-volume charging.
- Slowed the released orb ascent to four seconds and added a three-second descending purple beam before detonation.
- Added sculk-blue and amethyst-purple Iron’s wisps/electricity, native sculk souls and Echo Cannon firing audio.
- Preserved the fifteen-second cast, locked mark, single damage hit, terrain protection and obstruction checks.

## 0.3.0

- Reduced non-recorded Cataclysm base casts, including Doom and cave Cataclysms, to six seconds; reduced all God-tier base casts to ten seconds. Preserved all four supplied recording timings.
- Added original, clearly audible themed God charge beds with synchronized rising envelopes and direct caster playback. Replaced Doom’s overlapping ambient layers with dark-pressure charging, gravity-well ambience and an authored collapse.
- Reworked God effects into brighter, distinct silhouettes: ice spires, roots and canopies, golden arches, soul pillars, tidal crests, crimson/shadow formations, confection orbits, fracture lines and flame wings. Preserved Thunder of the Heavens’ lightning strikes.
- Moved player beam origins close to the hands with matching client/server clipping and luminous source caps. Added yellow and purple Arcane Devastator trails.
- Added **Orbital Ray**: a fixed 15-second charge grows an orb fed by swirling magic; it launches skyward and fires a vertical arcane beam into a nuclear-style explosion. One Legendary rank, 500 mana, 384 base damage, 16-block blast radius, 64-block targeting range and three-minute base cooldown.
- Orbital Ray locks its mark, checks overhead clearance and new obstructions, protects allies and terrain, and never spawns TNT or a nuclear-bomb entity. Native Alex’s Caves cloud is optional; a core-only cloud remains available.
- Expanded the catalogue to **164 core spells / 205 with both optional integrations**. Matching client/server updates are required: protocol **22**.

## 0.2.0

This release combines development after the CurseForge **0.1.0** baseline (internally 0.28.0-alpha), including the work previously labelled 0.29 and 0.30. Public releases use plain version numbers.

### New magic and recipes

- Added optional Alex’s Caves magic across Magnetic, Primordial, Toxic, Abyssal, Forlorn and Candy caves, with native themed particles, sounds, animations and constructs. Requires the supported Alex’s Caves 2.0.10 build and Citadel on both sides.
- Added twenty God-tier rituals: nine native-school spells, six cave arts and five Bosses’Rise arts. Each has one Legendary rank, a 20-second base cast, 600 mana cost and a four-minute cooldown.
- Added Thunder of the Heavens, which locks a 28-block field and releases 200 purple lightning strikes. Other God arts feature distinct sectors, crossing cuts, fronts, tethers, allied blessings and native addon constructs.
- Added fifty material-based scroll recipes for addon and God-tier spells. Generic Scroll Forge shortcuts are disabled for these spells. God scrolls glint while retaining Iron’s native school textures.

### Spell presentation and behaviour

- Replaced Tremorzilla’s rectangular beam with native beam materials and an animated impact model. Green is the default; offhand Amber Curiosity and Tectonic Shard select retro purple and tectonic orange. Removed the miniature boss.
- Moved beam sources to torso height. Arcane Devastator uses a continuous violet beam, and Doom Beam has sustained impact eruptions.
- Rebuilt Hullbreaker’s Breach as a warned rising bite and dive, and Lux’s attack as dodgeable volcanic vents and a falling molten core.
- Replaced Tectonic Stampede with Primordial Instinct, a 20-second non-stacking allied melee, speed and resistance blessing, while preserving its saved scroll ID.
- Refined Polarity Bolt’s red/azure trail and EMP’s radial discharge; removed the unrelated block-seeking magnetic lightning.
- Reworked Helvar’s effects with black-iron red embers and warm fracture sparks, a larger native sword, corrected headroom checks and an expanding damaging shockwave.
- Added cave-specific projectile, pierce, bounce, stun, interruption, hostile-projectile clearing, wave, vortex, Sugar Rush and staged impact behaviours. Native boss AI and destructive explosions never run.

### Audio, catalogue and compatibility

- Matched native charge-cue repetition to clip lengths and prevented stacked nuclear sirens. Nuclear Strike now uses shorter reactor and placement cues before its explosion ambience.
- Made Doom, Tremorzilla and God sound beds caster-owned and cancellable, with cleanup on disconnect, death and dimension changes. Cancelling one caster cannot silence another.
- Added themed icons, biome/tier search keywords, optional spell tags, a God category and accurate sequence information. Retained the current GUI, single Spells creative tab and saved core spell IDs.
- The catalogue contains 163 core spells, 17 optional Bosses’Rise spells and 24 optional Alex’s Caves spells: 204 with both integrations.
- Install **0.2.0 on both client and server**; network protocol remains **21**.

## 0.1.0

Initial CurseForge release, corresponding to the internal 0.28 development baseline. Includes the nine-school spell catalogue, legendary spellbooks, searchable spell archive, Void Domain and Void Exile, optional Bosses’Rise integration, and the spell balance and presentation improvements delivered up to that baseline.


[Guide home](https://github.com/Mikaaah/Resonant-Arcana-1.1.4/wiki) · [Report an issue](https://github.com/Mikaaah/Resonant-Arcana-1.1.4/issues)

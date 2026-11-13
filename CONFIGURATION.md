# Configuration

## First-person spell visibility (1.1.4)

`config/resonantarcana-spell-visibility.toml` is separate from the general client settings. It contains all 228 spell IDs, including optional content. Each entry is a boolean:

| Value | Behavior |
| --- | --- |
| `true` | Clear the spell’s nearby particles from the first-person crosshair/camera. |
| `false` | Keep the full particle presentation. |

Open **Mods → Resonant Arcana → Config → First-person view**. Search by display name or registry name, toggle a spell, or reset defaults. Changes save and apply immediately, including to already tracked particles. Distant effects and third-person particles stay visible. Damage, collision and targeting are unchanged.

Lightning spells default to `true`; other spells default to `false`. The old general `clearFirstPersonAim` key is obsolete and does not override these preferences. Each player’s settings stay local; update both client and server to 1.1.4 because particle packets now carry spell identity.

For manual file changes, edit while the game is closed. A complete default file is included in [the source repository](https://github.com/Mikaaah/Resonant-Arcana/blob/main/examples/resonantarcana-spell-visibility.toml).

Resonant Arcana separates authoritative server rules from local client preferences. Files are generated on first launch.

- **Server:** `<world>/serverconfig/resonantarcana-server.toml`. These values synchronize to joining clients. On a dedicated server, `<world>` is the configured level folder.
- **New-world defaults:** copy the server file to `defaultconfigs/resonantarcana-server.toml` before creating a world. This does not overwrite existing worlds.
- **Client:** `config/resonantarcana-client.toml`. Each player controls their own display, mouse input and native particle density.

Stop the game or server before editing general config files manually. The per-spell visibility screen saves and applies changes immediately. Existing configuration files retain their settings.

## Server settings

| Section and key | Default | Allowed values | Function |
| --- | ---: | --- | --- |
| `casting.enableWands` | `true` | boolean | Allow debug-wand casting; normal spellbooks remain available. |
| `casting.maxActiveSpells` | `128` | 8–512 | Maximum concurrent timed spells across the server. |
| `casting.maxActivePerPlayer` | `8` | 1–16 | Maximum concurrent timed spells owned by one player. |
| `combat.allowPvp` | `true` | boolean | Allow offensive spells against players, subject to server PvP and team rules. |
| `combat.healTeamlessPlayers` | `true` | boolean | Allow healing between players who both have no scoreboard team. Set false for competitive servers. |
| `balance.damageMultiplier` | `1.0` | 0–10 | Multiply direct spell damage after level and equipment scaling. Burning, poison and wither remain vanilla effects. |
| `balance.healingMultiplier` | `1.0` | 0–10 | Multiply direct healing. Does not increase absorption shields. |
| `balance.cooldownMultiplier` | `1.0` | 0.1–10 | Multiply cooldowns before Iron’s equipment modifiers. |
| `audio.volume` | `1.0` | 0–2 | Overall spell audio gain. The Players sound slider still applies. |
| `audio.impactVolume` | `1.0` | 0–3 | Additional impact gain, including Soul Cannon. |
| `audio.personalImpactFeedback` | `true` | boolean | Deliver the caster’s own impact audio without distance attenuation. Other players hear positional impacts. |
| `visuals.trailDensity` | `2` | 1–3 | Vanilla particles per trail point. |
| `visuals.trailPointsPerTick` | `256` | 64–2048 | Shared vanilla trail-point budget per server tick. |
| `visuals.detailMessagesPerTick` | `64` | 16–256 | Shared flight, area and charge visual-message budget per tick. |
| `visuals.accentMessagesPerTick` | `32` | 8–128 | Reserved impact, strike and casting visual-message budget per tick. |
| `visuals.ironsParticlesPerTick` | `128` | 32–512 | Extra native Iron’s particle budget per tick. |

Visual budgets affect presentation; they never select targets or change damage. Active-spell limits can refuse a new cast when capacity is reached. Server per-tick budgets are shared across casters. Native client budgets are local to each player. Range rings and expanding ripples reserve separate capacity, and detail decreases with distance. Up to 32 twelve-tick native ripples may be active per client.

## Client settings

| Section and key | Default | Allowed values | Function |
| --- | ---: | --- | --- |
| `controls.leftClickCasting` | `true` | boolean | Enable left-click wand casting. Disable for combat-mod input conflicts; the cast key remains available. |
| `display.showWandHud` | `true` | boolean | Show the selected spell and rank above the hotbar. |
| `particles.nativeParticlesPerTick` | `1800` | 300–6000 | Native client detail budget, plus a separate range-outline allowance of one third this value. Higher values add detail during crowded fights. |


## Individual spells

All core spells register through Iron’s native spell API. Use Iron’s spell configuration for individual enablement, maximum level, cooldown, school and crafting rules. Resonant Arcana’s server multipliers apply in addition to those rules. Scroll upgrades cannot exceed either the catalogue’s level cap or the server’s configured maximum.

The packaged `assets/resonant_arcana/catalogue.json` defines the addon’s baseline spell damage, mana, timing and descriptions. It is a validated build resource, **not a hot-reloaded datapack or a per-world configuration file**. Maintain a source fork and rebuild if you want to change these defaults. The spell guide is generated from the same resource.

## Namespace break in 0.13

All registered addon IDs now use `resonant_arcana`. Arcane, Cinder and Rift wands use `arcane_wand`, `cinder_wand` and `rift_wand`. Old registries, item selections and learned spell ranks are not migrated; obtain new wands and scrolls and reinscribe spellbooks. Key bindings also use the new namespace and may need rebinding. If you customized individual spells through Iron’s configuration, apply those overrides to their new `resonant_arcana` IDs.

The server/client configuration filenames introduced in 0.12 stay `resonantarcana-server.toml` and `resonantarcana-client.toml`.

## Optional Bosses’Rise spells

The adapter activates only for Bosses’Rise **2.1.2**. Both sides must have matching mod installations. Other versions leave the adapter disabled. Without Bosses’Rise, its twelve spells and visual entity types are not registered; core spells retain their IDs and behavior. Manage the twelve spells’ normal level, enablement and balance settings through Iron’s native spell configuration. Existing Resonant Arcana damage, cooldown, healing, PvP and active-cast limits still apply.

Ground spells require a loaded, visible solid surface within the bounded ground search. Temporary visual entities never save into chunks and are removed when their cast ends or the caster leaves. The addon does not copy, modify or redistribute Bosses’Rise assets.

## Reactive and spellsword spells (0.16)

Individual spell mana, cooldown, maximum level and enabled state use Iron’s spell
configuration. Resonant Arcana’s damage, healing and cooldown multipliers also
apply. Reactive buffs snapshot spell power at cast time. Weapon contributions
use the melee attack attribute at release. Buffs refresh without stacking.

Thunderbrand and Phantom Formation react only to successful native
`minecraft:player_attack` damage. Misses, cancelled hits and spell damage do
not charge them. Combat mods using their own damage types need an explicit
integration; no compatibility with every combat mod is implied.

Crimson Reversal uses a native three-second recast instance. The return is free,
then the normal cooldown starts; expiry also starts the cooldown. It requires
a clear, loaded destination in the same dimension and within 16 blocks.

Spellbreaker removes at most 32 visible hostile/unowned projectiles. It invokes
Iron’s cancellation API only for eligible long casts, checks interruption
immunity, and honours `CounterSpellEvent` cancellation. It cannot erase
already-released fields or arbitrarily cancel independent magic systems.

Kraken’s Wrath is available only with the supported Bosses’Rise 2.1.2 version.
Its visuals stay in that mod; no third-party assets are bundled.

## Equipment and renderer changes in 0.21

The ten survival wands and their assets are removed. One debug wand exposes all enabled spells. Three native spellbooks grant equipment perks only in the Curios spellbook slot. Cataclysm rituals use native particles at the caster’s feet. AAA settings and renderer support are removed.


## Selective rituals in 0.22

The Spells creative tab contains every enabled scroll. Categories remain available in the debug archive. The client `particles.aaaRituals` option (default true) enables only floor seals, Explosion seals and Doom when AAAParticles is installed and supported. Native detail uses the existing budget. Without AAA, native circles, the vertical Explosion beacon and incoming streams still render.

Explosion snapshots the aimed entity position or block/range mark at the beginning of its native cast. Turning does not move it. A new cast acquires a new mark; cancellation clears the snapshot and cannot detonate it. The mark must remain loaded and within 64 blocks with line of sight at release.

### Recorded spells in 0.26

These four spells have fixed windups so voices and sound effects keep their original pitch. Cast-speed equipment still affects other spells. Native mana, cooldowns, interruption, spell power and enabled-state settings remain active.

| Spell | Fixed charge | Audio after release | Source |
| --- | ---: | ---: | --- |
| Explosion | 7.7 s | 6.28 s | Megumin Explosion (Sound Redesign), entire recording |
| I Am Atomic | 13.5 s | 9 s | iamatomic, shortened blast tail with 1.5 s fade |
| Arcane Devastator | 6.5 s | 7.36 s | Cosmic Beam, entire recording |
| Doom Beam | 13 s | 9.5 s | Laser of Doom, selected consecutive excerpts |

Each complete source is loudness-normalized before splitting; neither phase is independently amplified. The old bass-boosted Explosion recording and pitch scaling are removed. Voices and buildup follow their caster, while released audio comes from the spell origin. Optional direct caster impact feedback and all existing audio sliders still apply. Caster/spell-specific sound instances stop on interruption, death and dimension changes without silencing another ritual. Released tails finish naturally.

Explosion's existing sound-event IDs `resonant_arcana:explosion_chant` and `resonant_arcana:explosion_blast` are preserved and point to the redesigned recording. Other IDs are `atomic_charge`, `atomic_release`, `cosmic_charge`, `cosmic_release`, `doom_laser_charge` and `doom_laser_release`, all in the `resonant_arcana` namespace. Resource packs can override those events through `assets/resonant_arcana/sounds.json`. Replacement charge recordings should retain the corresponding fixed duration.

### Atomic containment

I Am Atomic anchors a sixteen-block sphere at the start of its native cast. Moving more than three blocks interrupts it. Enemies captured at initiation are returned to their last clear position if they cross the sphere, subject to loaded-space and collision checks. Bosses and fully knockback-resistant enemies resist this displacement. Hostile-owned projectiles crossing the boundary are absorbed; allied projectiles are preserved. Visible allies receive brief, refreshed Resistance I while channeling.

The focus is a temporary, invulnerable netherite sword display. Interruption and release remove it; it cannot save into chunks or drop an item. The violet membrane is translucent, with native energy streams gathering at the sword tip. One 512-base-damage detonation occurs at the first release tick. Its expanding shock fronts are visual only. Caster/allied protection, PvP rules and line of sight remain authoritative, and terrain is never destroyed.

Arcane Devastator now sustains its beam for 7.4 seconds after a fixed 6.5-second windup. Each enemy can be damaged once, including enemies entering after release. The path is clipped against current loaded terrain each tick, so newly placed cover also stops it.

Doom uses original dark-pressure charge and gravity-well sound beds followed by one authored collapse. The old seven overlapping ambient clips are removed. Doom Beam retains the supplied Laser of Doom recording.

### Attached Doom Beam and reference Explosion in 0.26

Doom Beam's damage path follows the caster's hands and current look direction on every server tick. A target can still take damage only once over the full release, even if you sweep across it again. Every path is clipped against currently loaded terrain. Client geometry follows the interpolated caster on each rendered frame; short visual leases expire after interruption, death, dimension changes or missing updates. The centre uses Iron's beacon texture and ray hull helper; the outer plasma texture is original. Iron's wisp and electricity sprites receive per-particle green tint, without changing other spells or copying their assets. Native particle budgets still apply.

Doom/Atomic's core and membrane renderers no longer apply the world camera rotation twice. The core and its rings now share the same world-space centre.

Explosion keeps its 7.7-second chant, locked target and single 256-damage release. A vertical ignition column and five orange-gold seals build over the mark, with coloured mana sparks around the caster. An original textured fireball expands, rises and fades over 4.5 seconds with an expanding ground shockwave. The extended release spectacle adds no repeated damage or terrain destruction. Existing audio and particle sliders apply.

## Void Exile

Void Exile is Ender / Control, with an 80-tick base cast (normal cast-speed bonuses apply), 200-tick banishment and a 600-tick cooldown. Rank one costs 110 mana; each of five ranks adds 10 mana and normal spell power. It returns the target before applying 20 base Ender damage, then Weakness II for 100 ticks. Damage multipliers are captured at release.

Targets actually enter `resonant_arcana:void_exile`; UUID, equipment, NBT and original AI, gravity and invulnerability flags are preserved. Return records use overworld SavedData, independently of the caster or active spell list. Offline players return after login if their timer has elapsed. The timer uses server ticks and pauses with the server.

Bosses, creative/spectator players, invulnerable or dimension-immune targets, passengers, mounts and leashed creatures resist. Add entity types to `resonant_arcana:exile_immune` with a datapack to extend immunity. Ally and PvP protections are checked again on return.

If the origin becomes obstructed, the spell checks nearby clear positions and then world spawn. If no clear return is available it retries rather than suffocating or deleting the target. This spell temporarily loads its private void cell and return chunks; it does not edit battlefield blocks. Up to 128 active cells are supported.

## Void Domain and search

Void Domain has a 100-tick base cast, 240 base mana (+20 per rank), a 1,800-tick cooldown and a fixed 14-block radius. Its 300-tick duration scales with spell power up to 600 ticks. Each ten-tick pulse refreshes effects for thirty ticks, so benefits fade within 1.5 seconds of leaving. It grants Resistance II, Regeneration II, Speed II and +25% spell power to eligible allies; enemies receive Slowness III, Weakness II and Darkness. Benefits do not stack with another domain's copy of the same effect. Line of sight, PvP and ally rules apply. Teamless players count as allies when `healTeamlessPlayers` is enabled.

Individual scrolls share Iron's scroll item ID. Search their normal tooltip keywords or use the debug archive's text box; combine terms such as `ender buff aoe`, `ice slow` or `cataclysm`. Real spell-registry datapack tags live under `resonant_arcana:categories/<category>` and `resonant_arcana:schools/<school>` in the `irons_spellbooks:spells` registry. Optional boss entries are marked non-required. These are spell tags, not per-scroll item tags. Equipment item tags are `resonant_arcana:spellbooks` and `resonant_arcana:debug_tools`.

The 0.1.0 balance and presentation pass preserves saved spell IDs and audio timing. Both client and server must install the same release (0.3.0 uses protocol 22). See [the catalogue audit](CATALOGUE_AUDIT.md) for the reviewed rank-one values.

## Alex’s Caves integration in 0.3.0

Install **Alex’s Caves 2.0.10** and its required Citadel dependency on client and server. All twenty-four cave arts, including six God-tier rituals, and the visual marker entity register only when that supported version is present. Other versions leave the adapter disabled. Removing the mod hides its spells from the debug archive and creative scrolls; optional spell tags do not prevent startup. Core spells and the separate Bosses’Rise adapter remain available.

The normal Iron’s per-spell configuration applies to cave arts, including enablement, mana and cooldown. The six cave cataclysms have one Legendary rank; novice and master arts have five ranks. Spellbook and school power scale damage normally. Native `stunned`, `bubbled` and `sugar_rush` effects are supplied by Alex’s Caves. Bosses resist the stun, bubble and vortex pull.

Visual proxies are harmless, expire with their cast, have no loot or AI, and are not saved. Nuclear Strike does not place a functional bomb or change blocks. Native client particles share the addon’s detail and outline budgets; at most two native mushroom clouds are started concurrently per client. The native cloud supplies its own explosion ambience and screen effects.

Both sides must install **0.3.0 / protocol 22**. No Alex’s Caves or Citadel assets/classes are distributed in the addon.

## God tier and reworked integration spells in 0.3.0

God-tier arts have ten-second base casts, 600 mana, one Legendary rank and 240-second base cooldowns. Iron’s normal school power, cast-speed and server spell settings still apply. Targeted God rituals lock their field at the start. Cancellation spends no mana and removes the ritual mark. Offensive effects protect teammates, pets and terrain and use loaded terrain and cover checks.

Core non-God spells stay in the native Scroll Forge. Addon and God scrolls use the recipes in [SCROLL_RECIPES.md](SCROLL_RECIPES.md), consuming the listed proof instead of allowing a generic focus to bypass it. These recipes load only for a supported integration version. Nine native Iron’s school scroll textures are reused; no parent assets are bundled.

Tremorzilla breath has no boss proxy. Hold `alexscaves:amber_curiosity` in the offhand at release for the retro purple variant, or `alexscaves:tectonic_shard` for orange; otherwise it is green. Its God art uses retro purple. All native beam variants share the same combat rules. Beams originate at 55% of the caster’s current body height, with their existing forward, wall-clipped offset.

Doom, Tremorzilla and God charge ambience now uses caster-owned client instances with fourteen-tick leases. Completion, cancellation, death, disconnect and dimension changes stop those instances. Native long clips cannot linger for twenty seconds after cleanup. Other casters retain their own sound layers. Existing owner-supplied recordings keep their timing. Client sound and visual playtesting is still required.

The protocol is now **22**; install matching **0.3.0** addon builds on client and server.


## Orbital Ray and faster rituals in 0.3.0

Non-recorded Cataclysms, including Doom and cave Cataclysms, now have a six-second base cast. All God-tier arts have ten-second base casts. Native cast-speed equipment still applies to those spells. Explosion (7.7 s), I Am Atomic (13.5 s), Arcane Devastator (6.5 s) and Doom Beam (13 s) retain their supplied recording timings.

Orbital Ray has one Legendary rank, a fixed 15-second cast, 500 mana and a three-minute base cooldown. Obtain its scroll through the native Scroll Forge. It locks visible ground within 64 blocks and requires at least 12 clear blocks above the mark. After release, the orb flies upward for two seconds, a vertical beam forms, and the explosion lands at release tick 60. Each visible enemy within 16 blocks takes one 384-base-damage hit. The beam and cloud add no repeated damage. Interruption during charging releases no attack; new obstruction during ascent or before detonation aborts it. Allies and terrain remain protected. No TNT or nuclear-bomb entity is spawned; Alex’s Caves can supply its native mushroom cloud, with a core-only cloud fallback.

God-tier charge beds are original themed sounds that rise with synchronized cast progress. The caster hears them without positional attenuation; observers hear them from the caster. Existing volume controls apply, and each caster’s sound leases stop independently on cancellation, death, disconnect or dimension change.

Player beams now begin just 0.18 blocks forward at torso/hand height, with the same origin on the client and server and a closed luminous source. Arcane Devastator adds yellow and purple wisps and electrical trails.

Install matching **0.3.0** builds on client and server; network protocol is **22**.

[← Guide home](https://github.com/Mikaaah/Resonant-Arcana-1.1.4/wiki) · [Project](https://github.com/Mikaaah/Resonant-Arcana-1.1.4) · [Get help](https://github.com/Mikaaah/Resonant-Arcana-1.1.4/issues)

# Configuration — 1.1.4

## First-person spell visibility (1.1.4)

`config/resonantarcana-spell-visibility.toml` is separate from the general client settings. It contains all 228 spell IDs, including optional content. Each entry is a boolean:

| Value | Behavior |
| --- | --- |
| `true` | Clear the spell’s nearby particles from the first-person crosshair/camera. |
| `false` | Keep the full particle presentation. |

Open **Mods → Resonant Arcana → Config → First-person view**. Search by display name or registry name, toggle a spell, or reset defaults. Changes save and apply immediately, including to already tracked particles. Distant effects and third-person particles stay visible. Damage, collision and targeting are unchanged.

Lightning spells default to `true`; other spells default to `false`. The old general `clearFirstPersonAim` key is obsolete and does not override these preferences. Each player’s settings stay local; update both client and server to 1.1.4 because particle packets now carry spell identity.

For manual file changes, edit while the game is closed. A complete default file is included in [the source repository](https://github.com/Mikaaah/Resonant-Arcana-1.1.4/blob/main/examples/resonantarcana-spell-visibility.toml).

## General configuration

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

## Compatibility and updates

Install **Resonant Arcana 1.1.4 on both client and server**. Particle packets carry spell identity; older protocol/version notes do not apply to this release. The optional Bosses’Rise and Alex’s Caves integrations remain optional.

The debug wand is for creative testing; retired school wands have not returned. Normal survival casting uses inscribed spellbooks. See [Equipment](Equipment) and [Optional addons](Optional-Addons).

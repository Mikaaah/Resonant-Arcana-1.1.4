# Elemental bursts — 1.2.0

All five are standalone Legendary Cataclysm spells, using existing Iron’s schools. Character names and element names are searchable keywords. Geo, Electro, Anemo and Cryo describe the inspirations; this update does not introduce a separate elemental-reaction system.

| Inspiration | Spell / ID | Existing school | Base damage | Effect | Cast / mana / cooldown |
| --- | --- | --- | --- | --- | --- |
| Zhongli | Planet Befall / `planet_befall` | Evocation | 180 once | 10-block radius; four-second Petrified status | 5 s / 420 / 90 s |
| Qiqi | Adeptus Art: Preserver of Fortune / `preserver_of_fortune` | Ice | 24 once | 12-block radius; heals allies 8; 15-second talismans heal attacking allies 4, once per second per attacker | 3 s / 360 / 90 s |
| Raiden | Secret Art: Musou Shinsetsu / `musou_shinsetsu` | Lightning | 144 opening + 4 × 72 | Forward arc within 12 blocks, fixed at release | 4 s / 420 / 90 s |
| Venti | Wind’s Grand Ode / `winds_grand_ode` | Ender | 8 × 16 | Eight-second vortex; 10-block radius; ordinary enemies are pulled, tagged bosses resist displacement | 3.5 s / 360 / 90 s |
| Ganyu | Celestial Shower / `celestial_shower` | Ice | 12 × 18 | Twelve targeted icicles, 3-block local impact radius, 12-block field; Slowness II | 4 s / 400 / 90 s |

Numbers are base health points before equipment, spell power, server multipliers, damage mitigation and cover. Twenty health points equal ten hearts. The last icicle lands at 11 seconds; the pearl remains for the full 12-second field duration.

Petrification stops AI without permanently changing an entity’s original AI state. Removal, cleansing and natural expiration restore that state. Already-fired projectiles remain independent. Fortune heals require a player’s direct attack or spell hit with a direct player damage source; burning, poison and autonomous projectiles cannot repeatedly trigger healing. The interval is shared across marked targets for each attacker.

Scroll recipes use paper, Legendary Ink, a Nether Star and the following materials:

| Spell | Materials |
| --- | --- |
| Planet Befall | Gold Block, Amethyst Shard, Obsidian |
| Preserver of Fortune | Ghast Tear, Blue Ice, Golden Apple |
| Musou Shinsetsu | Lightning Rod, Amethyst Shard, Diamond Sword |
| Wind’s Grand Ode | Breeze Rod, Ender Pearl, Feather |
| Celestial Shower | Blue Ice, Prismarine Crystals, Snow Block |

The catalogue damage reduction is 20% for all 43 existing offensive entries in the God and Cataclysm categories. Ordinary spells, healing, costs, times, cooldowns and sequences are unchanged. It reduces burst lethality; heavily amplified endgame spells can still kill low-health bosses in one cast.

[← Guide home](https://github.com/Mikaaah/Resonant-Arcana-1.1.4/wiki) · [Project](https://github.com/Mikaaah/Resonant-Arcana-1.1.4) · [Get help](https://github.com/Mikaaah/Resonant-Arcana-1.1.4/issues)

# Control

**Guide version 1.1.4.** Values below come from the packaged 1.1.4 catalogue, before equipment, resistance and server settings. Two health points equal one heart. Damage is per normal hit or pulse; descriptions explain multi-stage damage. Travel and release sequences follow the cast bar.

### Mind Haze

Disorient one visible enemy with Nausea for five seconds and Blindness for two seconds. Deals no direct damage.

**Evocation** · Cast **0.4s** · Cooldown **5s** · Range **24 blocks** · Radius / half-width **1 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | — | — | 18 |
| II | — | — | 21 |
| III | — | — | 24 |
| IV | — | — | 27 |
| V | — | — | 30 |

Spell ID: `resonant_arcana:confuse`. Inspect ranks with `/resonantarcana spell confuse <rank>`.

### Stun

Bind one visible aimed enemy with arcane sparks. Apply Slowness VI and Weakness II for three seconds, with one direct hit; immune enemies may resist the effects.

**Evocation** · Cast **0.4s** · Cooldown **5s** · Range **24 blocks** · Radius / half-width **1 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | 2 | — | 20 |
| II | 2.5 | — | 24 |
| III | 3 | — | 28 |
| IV | 3.5 | — | 32 |
| V | 4 | — | 36 |

Spell ID: `resonant_arcana:stun`. Inspect ranks with `/resonantarcana spell stun <rank>`.

### Arcane Bind

Wind an arcane cage around one aimed enemy for three seconds at level I. Suppress movement and melee damage while the link holds. Cover, range or displacement of more than four blocks breaks it.

**Evocation** · Cast **0.6s** · Cooldown **8s** · Range **24 blocks** · Radius / half-width **1 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | — | — | 24 |
| II | — | — | 28 |
| III | — | — | 32 |
| IV | — | — | 36 |
| V | — | — | 40 |

Base release / active phase: **3s**.

Spell ID: `resonant_arcana:capture`. Inspect ranks with `/resonantarcana spell capture <rank>`.

### Launch

Send a rising arcane strike into one visible aimed enemy, adding upward velocity. This lifts the target; it does not grant the enemy fall protection.

**Evocation** · Cast **0s** · Cooldown **3.5s** · Range **24 blocks** · Radius / half-width **1 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | 2 | — | 19 |
| II | 2.5 | — | 22 |
| III | 3 | — | 25 |
| IV | 3.5 | — | 28 |
| V | 4 | — | 31 |

Spell ID: `resonant_arcana:launch`. Inspect ranks with `/resonantarcana spell launch <rank>`.

### Hemorrhagic Shock

Close a crimson binding around one visible aimed enemy. One hit applies Slowness VI and Weakness II for three seconds; this spell does not drain health.

**Blood** · Cast **0.4s** · Cooldown **5s** · Range **24 blocks** · Radius / half-width **1 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | 3 | — | 22 |
| II | 3.75 | — | 26 |
| III | 4.5 | — | 30 |
| IV | 5.25 | — | 34 |
| V | 6 | — | 38 |

Spell ID: `resonant_arcana:blood_stun`. Inspect ranks with `/resonantarcana spell blood_stun <rank>`.

### Dread Mark

Fasten an orbiting shadow curse to one aimed enemy. After two seconds it bursts around that enemy, damaging and weakening nearby enemies. Losing line of sight or exceeding the link range breaks the curse.

**Ender** · Cast **0.5s** · Cooldown **8s** · Range **24 blocks** · Radius / half-width **3.5 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | 24 | — | 52 |
| II | 30 | — | 62 |
| III | 36 | — | 72 |
| IV | 42 | — | 82 |
| V | 48 | — | 92 |

Base release / active phase: **2s**.

Spell ID: `resonant_arcana:shadow_cage`. Inspect ranks with `/resonantarcana spell shadow_cage <rank>`.

### Static Shock

Overload one visible enemy with static. Deal one hit and apply Slowness VI and Weakness II for three seconds.

**Lightning** · Cast **0.4s** · Cooldown **4s** · Range **28 blocks** · Radius / half-width **1 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | 4 | — | 22 |
| II | 5 | — | 26 |
| III | 6 | — | 30 |
| IV | 7 | — | 34 |
| V | 8 | — | 38 |

Spell ID: `resonant_arcana:static_shock`. Inspect ranks with `/resonantarcana spell static_shock <rank>`.

### Force Push

Strike one visible aimed enemy with an outward spatial impulse. Deal direct damage and launch it away from the caster with a small upward lift.

**Ender** · Cast **0s** · Cooldown **3.25s** · Range **24 blocks** · Radius / half-width **1 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | 3 | — | 18 |
| II | 3.75 | — | 21 |
| III | 4.5 | — | 24 |
| IV | 5.25 | — | 27 |
| V | 6 | — | 30 |

Spell ID: `resonant_arcana:force_push`. Inspect ranks with `/resonantarcana spell force_push <rank>`.

### Force Pull

Catch one visible aimed enemy with a spatial link. Deal direct damage and pull it horizontally toward you with a small upward lift.

**Ender** · Cast **0s** · Cooldown **3.25s** · Range **24 blocks** · Radius / half-width **1 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | 3 | — | 18 |
| II | 3.75 | — | 21 |
| III | 4.5 | — | 24 |
| IV | 5.25 | — | 27 |
| V | 6 | — | 30 |

Spell ID: `resonant_arcana:force_pull`. Inspect ranks with `/resonantarcana spell force_pull <rank>`.

### Suspension

Suspend one visible aimed enemy above its starting point for two seconds. Deal one hit, slow it and briefly grant Slow Falling. Cover, range or displacement beyond four blocks breaks the hold.

**Ender** · Cast **0.6s** · Cooldown **7s** · Range **24 blocks** · Radius / half-width **1 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | 3 | — | 26 |
| II | 3.75 | — | 31 |
| III | 4.5 | — | 36 |
| IV | 5.25 | — | 41 |
| V | 6 | — | 46 |

Base release / active phase: **2s**.

Spell ID: `resonant_arcana:gravity_choke`. Inspect ranks with `/resonantarcana spell gravity_choke <rank>`.

### Soul Prison

Imprison one aimed enemy in a winding soul cage for three seconds at level I. Three one-second hits apply Darkness while the hold suppresses movement. Cover, range or displacement breaks the link.

**Eldritch** · Cast **0.6s** · Cooldown **7s** · Range **24 blocks** · Radius / half-width **1 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | 3 | — | 26 |
| II | 3.75 | — | 31 |
| III | 4.5 | — | 36 |
| IV | 5.25 | — | 41 |
| V | 6 | — | 46 |

Base release / active phase: **3s**.

Spell ID: `resonant_arcana:soul_prison`. Inspect ranks with `/resonantarcana spell soul_prison <rank>`.

### Crimson Tether

Tether an aimed enemy for 3 seconds, gently pulling it and dealing three damage pulses. Cover or moving out of range breaks the link.

**Blood** · Cast **0.8s** · Cooldown **8s** · Range **24 blocks** · Radius / half-width **1 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | 5 | — | 42 |
| II | 6.25 | — | 50 |
| III | 7.5 | — | 58 |
| IV | 8.75 | — | 66 |
| V | 10 | — | 74 |

Base release / active phase: **3s**.

Spell ID: `resonant_arcana:crimson_tether`. Inspect ranks with `/resonantarcana spell crimson_tether <rank>`.

### Thorn Lash

Catch an aimed enemy with a thorn tether, pull it towards you and poison it.

**Nature** · Cast **0.5s** · Cooldown **5.5s** · Range **20 blocks** · Radius / half-width **1 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | 16 | — | 30 |
| II | 20 | — | 36 |
| III | 24 | — | 42 |
| IV | 28 | — | 48 |
| V | 32 | — | 54 |

Spell ID: `resonant_arcana:thorn_lash`. Inspect ranks with `/resonantarcana spell thorn_lash <rank>`.

### Gorgon Gaze

Fix visible enemies in a wide forward cone with a petrifying gaze, applying heavy slowness and weakness for two seconds at level I.

**Nature** · Cast **1.2s** · Cooldown **10s** · Range **10 blocks** · Radius / half-width **0 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | 4 | — | 48 |
| II | 5 | — | 57 |
| III | 6 | — | 66 |
| IV | 7 | — | 75 |
| V | 8 | — | 84 |

Base release / active phase: **2s**.

Spell ID: `resonant_arcana:gorgon_gaze`. Inspect ranks with `/resonantarcana spell gorgon_gaze <rank>`.

### Glacial Prison

Hold an aimed enemy in spectral ice for three seconds at level I. Cover, distance or large movement breaks the restraint.

**Ice** · Cast **1s** · Cooldown **9s** · Range **24 blocks** · Radius / half-width **1 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | — | — | 45 |
| II | — | — | 54 |
| III | — | — | 63 |
| IV | — | — | 72 |
| V | — | — | 81 |

Base release / active phase: **3s**.

Spell ID: `resonant_arcana:glacial_prison`. Inspect ranks with `/resonantarcana spell glacial_prison <rank>`.

### Void Exile

Banish one hostile living target for ten seconds, leaving a violet rift. On return, it takes damage before receiving Weakness II for five seconds. Bosses, mounted, leashed and dimension-immune targets resist.

**Ender** · Cast **4s** · Cooldown **30s** · Range **24 blocks** · Radius / half-width **1.5 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | 20 | — | 110 |
| II | 25 | — | 120 |
| III | 30 | — | 130 |
| IV | 35 | — | 140 |
| V | 40 | — | 150 |

Base release / active phase: **10s**.

Spell ID: `resonant_arcana:void_exile`. Inspect ranks with `/resonantarcana spell void_exile <rank>`.

### EMP Surge

Release an electromagnetic pulse around yourself. Damage enemies, stun non-bosses for three seconds, interrupt eligible spellcasts and destroy hostile projectiles.

**Lightning** · Cast **3s** · Cooldown **30s** · Range **0 blocks** · Radius / half-width **8 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | 56 | — | 120 |
| II | 70 | — | 128 |
| III | 84 | — | 136 |
| IV | 98 | — | 144 |
| V | 112 | — | 152 |

Base release / active phase: **1.2s**.

Requires **Alex’s Caves 2.0.10** and Citadel on client and server. No native boss AI or terrain destruction runs.

Spell ID: `resonant_arcana:emp_surge`. Inspect ranks with `/resonantarcana spell emp_surge <rank>`.

### Frostmint Hex

Place a sugar-staff hex surrounded by orbiting frostmint discs. Five pulses damage and freeze enemies inside; eligible allies gain Sugar Rush while holding the circle.

**Ice** · Cast **3.2s** · Cooldown **37.5s** · Range **28 blocks** · Radius / half-width **7 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | 12 | — | 140 |
| II | 15 | — | 148 |
| III | 18 | — | 156 |
| IV | 21 | — | 164 |
| V | 24 | — | 172 |

Base release / active phase: **5s**.

Requires **Alex’s Caves 2.0.10** and Citadel on client and server. No native boss AI or terrain destruction runs.

Spell ID: `resonant_arcana:frostmint_hex`. Inspect ranks with `/resonantarcana spell frostmint_hex <rank>`.

### Angel Scream

Unleash a holy scream as a travelling golden shockwave. The wave reaches twelve blocks in roughly 0.7 seconds. Each enemy it reaches in the 120-degree forward cone is held for three seconds and receives Weakness II for five seconds at level I. Walls and allies are respected.

**Holy** · Cast **1.5s** · Cooldown **20s** · Range **12 blocks** · Radius / half-width **0 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | — | — | 75 |
| II | — | — | 83 |
| III | — | — | 91 |
| IV | — | — | 99 |
| V | — | — | 107 |

Base release / active phase: **3s**.

Spell ID: `resonant_arcana:angel_scream`. Inspect ranks with `/resonantarcana spell angel_scream <rank>`.

### Cinder Fuse

Mark up to three nearby enemies with a burning fuse. After a two-second warning, each still-visible mark detonates once around itself. Breaking the visible connection or leaving range severs that mark.

**Fire** · Cast **2s** · Cooldown **25s** · Range **24 blocks** · Radius / half-width **3 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | 24 | — | 85 |
| II | 30 | — | 91 |
| III | 36 | — | 97 |
| IV | 42 | — | 103 |
| V | 48 | — | 109 |

Base release / active phase: **3s**.

Spell ID: `resonant_arcana:cinder_fuse`. Inspect ranks with `/resonantarcana spell cinder_fuse <rank>`.

### Orbit Exchange

Mark an aimed enemy and its nearest eligible neighbor. After a one-second warning exchange their supported positions if both destinations and the connecting path remain safe. Bosses, mounted and leashed entities resist.

**Ender** · Cast **1.25s** · Cooldown **20s** · Range **20 blocks** · Radius / half-width **8 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | — | — | 65 |
| II | — | — | 71 |
| III | — | — | 77 |
| IV | — | — | 83 |
| V | — | — | 89 |

Base release / active phase: **1.25s**.

Spell ID: `resonant_arcana:orbit_exchange`. Inspect ranks with `/resonantarcana spell orbit_exchange <rank>`.

### Mirror Ice

Raise a directional ice mirror for eight seconds. Capture up to four supported hostile projectiles approaching its front. Recast to release them along your aim for no extra mana. Captured shots expire with the mirror and cannot become free ammunition.

**Ice** · Cast **1.25s** · Cooldown **20s** · Range **16 blocks** · Radius / half-width **3 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | — | — | 65 |
| II | — | — | 71 |
| III | — | — | 77 |
| IV | — | — | 83 |
| V | — | — | 89 |

Base release / active phase: **8s**.

Spell ID: `resonant_arcana:mirror_ice`. Inspect ranks with `/resonantarcana spell mirror_ice <rank>`.

### Lodestone Snare

Raise red and blue magnetic pylons around an eight-block trap. Their arcing field drags enemies toward the centre, applies Slowness IV and discharges 5 base damage per second for eight seconds. Bosses resist the pull. Walls block the field.

**Lightning** · Cast **1.75s** · Cooldown **25s** · Range **16 blocks** · Radius / half-width **2.5 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | 5 | — | 80 |
| II | 6.25 | — | 86 |
| III | 7.5 | — | 92 |
| IV | 8.75 | — | 98 |
| V | 10 | — | 104 |

Base release / active phase: **8s**.

Requires **Alex’s Caves 2.0.10** and Citadel on client and server. No native boss AI or terrain destruction runs.

Spell ID: `resonant_arcana:lodestone_snare`. Inspect ranks with `/resonantarcana spell lodestone_snare <rank>`.

### Stolen Shadow

Rip a visible enemy's shadow into a dark humanoid double. The victim suffers Weakness and Slowness II. Its next damaging attack on an ally triggers a delayed shadow slash for 12 plus 75% of the attack damage, up to 30 base damage; bosses take half. If it refuses to attack, the shadow lashes for 16 base damage after three seconds. Triggers once.

**Ender** · Cast **1.75s** · Cooldown **25s** · Range **20 blocks** · Radius / half-width **0 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | — | — | 85 |
| II | — | — | 91 |
| III | — | — | 97 |
| IV | — | — | 103 |
| V | — | — | 109 |

Base release / active phase: **8s**.

Requires **Alex’s Caves 2.0.10** and Citadel on client and server. No native boss AI or terrain destruction runs.

Spell ID: `resonant_arcana:stolen_shadow`. Inspect ranks with `/resonantarcana spell stolen_shadow <rank>`.

### Whiteout Hunt

Summon a sweeping eight-second hunting blizzard. Allies in the storm gain Speed II and Resistance I. Visible enemies suffer Slowness III, glow through the snow and take 4 base frost damage every 1.5 seconds. Ordinary mobs lose locks on sheltered allies; bosses retain their targeting.

**Ice** · Cast **2s** · Cooldown **30s** · Range **20 blocks** · Radius / half-width **8 blocks**.

| Rank | Base damage | Base healing | Mana |
| --- | ---: | ---: | ---: |
| I | 4 | — | 95 |
| II | 5 | — | 101 |
| III | 6 | — | 107 |
| IV | 7 | — | 113 |
| V | 8 | — | 119 |

Base release / active phase: **8s**.

Requires **Bosses’Rise 2.1.2** on client and server. Models/textures come from the installed mod; original boss AI does not run.

Spell ID: `resonant_arcana:whiteout_hunt`. Inspect ranks with `/resonantarcana spell whiteout_hunt <rank>`.

[← Wiki home](https://github.com/Mikaaah/Resonant-Arcana-1.1.4/wiki/Home)

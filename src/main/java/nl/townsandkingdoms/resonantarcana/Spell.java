package nl.townsandkingdoms.resonantarcana;

import java.util.Arrays;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import nl.townsandkingdoms.resonantarcana.BossesRiseCompat;
import nl.townsandkingdoms.resonantarcana.CavesCompat;
import nl.townsandkingdoms.resonantarcana.SpellCatalogue;
import nl.townsandkingdoms.resonantarcana.SpellCategory;

public enum Spell {
    SPARK,
    ARCANE_STRIKE,
    BLOOD_SPARK,
    POISON_SPARK,
    BLOOD_WAVE,
    POISON_WAVE,
    FLAME_WAVE,
    CONFUSE,
    STUN,
    CAPTURE,
    ASTRAL_COMET,
    FIRE_COMET,
    ARCANE_REPULSE,
    FIREBALL,
    IGNITE,
    LAUNCH,
    LEAP,
    LIGHTNING,
    SMITE,
    FIRE_TWISTER,
    FLAME_THROWER,
    FLAME_SHOCK_WAVE,
    BLOOD_EXPLODE,
    BLOOD_STUN,
    BLOOD_BLOCK,
    SHADOW_BOLT,
    SOUL_SEEKER,
    DARK_WAVE,
    NIGHTFALL,
    SHADOW_CAGE,
    SOUL_AURA,
    THUNDER_BOLT,
    CHAIN_LIGHTNING,
    THUNDER_WAVE,
    STORM_CALL,
    THUNDER_PULSE,
    STATIC_SHOCK,
    GRAVITY_BOLT,
    GRAVITY_WELL,
    FORCE_PUSH,
    FORCE_PULL,
    GRAVITY_CHOKE,
    SINGULARITY,
    ECHO_BOLT,
    SCULK_WAVE,
    SONIC_SCREAM,
    SOUL_SIPHON,
    SOUL_PRISON,
    SHRIEKING_PULSE,
    AMETHYST_NOVA,
    ECHO_WARD,
    ARCANE_VOLLEY,
    SANGUINE_DRAIN,
    BLOOD_LANCE,
    EMBER_VEIL,
    VENOM_BOLT,
    MIASMA_FIELD,
    WITHERING_HEX,
    SOUL_CANNON,
    SOLAR_CATACLYSM,
    TEMPEST_SPEAR,
    EVENT_HORIZON,
    DOOM,
    SANCTUARY,
    GLACIAL_RUPTURE,
    EVOKERS_EDICT,
    ARCANE_STEP,
    BLOOD_NEEDLES,
    BLOOD_REAP,
    CRIMSON_TETHER,
    SANGUINE_PACT,
    CRIMSON_HARVEST,
    CINDER_STEP,
    PHOENIX_RUSH,
    DRAGON_BREATH,
    UMBRAL_STEP,
    DUSK_CLEAVE,
    DREAD_EFFIGY,
    VEIL_OF_NIGHT,
    NIGHT_BLOOM,
    STORM_STEP,
    THUNDERCLAP,
    ARC_RICOCHET,
    RIFT_BLINK,
    ORBITAL_BLADES,
    PRISMATIC_RUIN,
    GRAVITY_STAMP,
    SPATIAL_SHEAR,
    WISP_LANTERN,
    RESONANT_GUARD,
    MEMORY_FRACTURE,
    THORN_LASH,
    VIPER_FANGS,
    BRAMBLE_RING,
    TOXIC_MOLT,
    SPORE_TOTEM,
    CORROSIVE_RAIN,
    GORGON_GAZE,
    LUMINOUS_DART,
    MENDING_LIGHT,
    BLESSING_OF_VIGOR,
    CONSECRATION,
    RADIANT_SWEEP,
    PILGRIM_STEP,
    ABSOLUTION,
    DAWN_WARD,
    JUDGEMENT_RAY,
    SERAPHIC_DESCENT,
    RENEWAL_HYMN,
    FROST_NEEDLE,
    ICE_SERPENT,
    RIME_NOVA,
    CRYO_LANCE,
    FROZEN_AEGIS,
    GLACIER_STEP,
    SHATTER,
    HAILSTORM,
    GLACIAL_PRISON,
    CRYSTAL_BARRAGE,
    ABSOLUTE_ZERO,
    WARSCREAM,
    SOULBLAST,
    ARCANE_EDGE,
    BULWARK_CHANT,
    BATTLE_TEMPO,
    CINDER_CLEAVE,
    RIME_THRUST,
    BLOOD_REND,
    KRAKENS_GRASP,
    ICE_SPIKE_ERUPTION,
    HELVARS_RIFT,
    KRAKENS_WRATH,
    GLACIAL_COUNTER,
    RIFT_DRAW,
    CRIMSON_REVERSAL,
    THUNDERBRAND,
    LAST_STAND,
    ASHEN_REBIRTH,
    PHANTOM_FORMATION,
    FUNERAL_BELL,
    SPELLBREAKER,
    KRAKENS_UNDERTOW,
    YETIS_GLACIAL_WALL,
    HELVARS_FRACTURE,
    SANDWORMS_ERUPTION,
    SANDWORMS_QUICKSAND,
    DRAGONS_GUARD,
    DRAGONS_PYRE,
    HELVARS_SWORDFALL,
    ASTRAL_APOCALYPSE,
    CRIMSON_ECLIPSE,
    WORLD_PYRE,
    VOID_CORONATION,
    HEAVENS_THUNDER,
    RIFT_COLLISION,
    SOUL_REQUIEM,
    WORLDROOT_CATASTROPHE,
    FINAL_JUDGEMENT,
    ETERNAL_WINTER,
    EXPLOSION,
    SERAPH_ASCENDANT,
    ARCANE_DEVASTATOR,
    I_AM_ATOMIC,
    DOOM_BEAM,
    VOID_EXILE,
    VOID_DOMAIN,
    POLARITY_BOLT,
    EMP_SURGE,
    MAGNETAR_ANNIHILATION,
    AMBER_TALON,
    TECTONIC_STAMPEDE,
    LUX_EXTINCTION,
    ACID_GLOB,
    TREMORZILLA_BREATH,
    NUCLEAR_STRIKE,
    BRINE_BUBBLE,
    HULLBREAKER_SURGE,
    ABYSSAL_DELUGE,
    DESOLATE_DAGGER,
    FORSAKEN_SONAR,
    FORLORN_RECKONING,
    GUMBALL_VOLLEY,
    FROSTMINT_HEX,
    CANDY_CATASTROPHE,
    THUNDER_OF_THE_HEAVENS,
    ARCANE_CREATION,
    CRIMSON_APOTHEOSIS,
    SOLAR_SUPERNOVA,
    EVENTIDE_HORIZON,
    ABSOLUTE_GLACIATION,
    VERDANT_AWAKENING,
    HEAVENS_COVENANT,
    SOUL_ASCENDANCY,
    PLANETARY_POLARITY,
    PRIMORDIAL_SOVEREIGN,
    TREMORZILLA_CATACLYSM,
    ABYSSAL_WORLD_TIDE,
    FORSAKEN_ECLIPSE,
    CONFECTION_APOCALYPSE,
    KRAKEN_WORLD_DROWN,
    YETI_WORLD_HEART,
    HELVAR_FINAL_EDICT,
    SANDWORM_WORLD_DEVOURER,
    DRAGON_PRIMORDIAL_INFERNO,
    ORBITAL_RAY,
    ANGEL_SCREAM,
    ARCANE_HAND,
    SPELL_PRISM,
    BLOOD_PRICE,
    CINDER_FUSE,
    FOLDED_PASSAGE,
    ORBIT_EXCHANGE,
    STORM_RAIL,
    ECHO_DECOY,
    CHOIR_OF_THE_LOST,
    MYCELIAL_RELAY,
    MARTYRS_AEGIS,
    MIRROR_ICE,
    LODESTONE_SNARE,
    AMBER_STASIS,
    FALLOUT_BLOOM,
    PRESSURE_BELL,
    STOLEN_SHADOW,
    CARAMEL_CATAPULT,
    OATHBREAKER_CHAINS,
    WHITEOUT_HUNT,
    RHO_AIAS,
    WARDENS_JUDGEMENT,
    PLANET_BEFALL,
    PRESERVER_OF_FORTUNE,
    MUSOU_SHINSETSU,
    WINDS_GRAND_ODE,
    CELESTIAL_SHOWER;

    public final String title;
    public final Kind kind;
    public final Theme theme;
    public final float damage;
    public final double radius;
    public final double range;
    public final int cooldown;
    final SpellCatalogue.Definition definition = SpellCatalogue.definition(this.name());

    private Spell() {
        this.title = this.definition.title();
        this.kind = Kind.valueOf(this.definition.kind());
        this.theme = Theme.valueOf(this.definition.theme());
        this.damage = this.definition.damage();
        this.radius = this.definition.radius();
        this.range = this.definition.range();
        this.cooldown = this.definition.cooldown();
    }

    public boolean available() {
        return !(BossesRiseCompat.optional(this) && !BossesRiseCompat.available() || CavesCompat.optional(this) && !CavesCompat.available());
    }

    public static Spell[] enabledValues() {
        return (Spell[])Arrays.stream(Spell.values()).filter(Spell::available).toArray(Spell[]::new);
    }

    public int castTicks() {
        return this.definition.castTicks();
    }

    public int manaCost() {
        return this.definition.mana();
    }

    public int manaStep() {
        return this.definition.manaStep();
    }

    public int levelCap() {
        return this.definition.maxLevel();
    }

    public String bookId() {
        return this.definition.bookId();
    }

    public MutableComponent displayName() {
        return Component.translatableWithFallback((String)("spell.resonant_arcana." + this.bookId()), (String)this.title);
    }

    public MutableComponent descriptionText() {
        return Component.translatableWithFallback((String)("spell.resonant_arcana." + this.bookId() + ".description"), (String)this.description());
    }

    public String description() {
        return this.definition.description();
    }

    public SpellCategory category() {
        return SpellCategory.valueOf(this.definition.category());
    }

    public String role() {
        return this.definition.role();
    }

    public int duration() {
        return this.definition.duration();
    }

    public int interval() {
        return this.definition.interval();
    }

    public int pulses() {
        return this.definition.pulses();
    }

    public float healing() {
        return this.definition.healing();
    }

    public float power(int level) {
        return 1.0f + (float)(SpellCatalogue.POWER_PER_LEVEL * Math.clamp((long)(level - 1), 0, this.levelCap() - 1)) / 100.0f;
    }

    public float damageAtLevel(int level) {
        return this.damage * this.power(level);
    }

    public float healingAtLevel(int level) {
        return this.healing() * this.power(level);
    }

    public int manaAtLevel(int level) {
        return this.manaCost() + this.manaStep() * Math.clamp((long)(level - 1), 0, this.levelCap() - 1);
    }

    public String cooldownKey() {
        return switch (this.kind.ordinal()) {
            case 36, 53, 56, 57 -> "shared_heal";
            case 25 -> "shared_siphon";
            case 15 -> "shared_ward";
            case 5, 22, 27 -> "shared_restraint";
            case 4 -> "shared_stun";
            default -> this.name();
        };
    }

    public static enum Kind {
        BOLT,
        TARGET,
        WAVE,
        CONFUSE,
        STUN,
        CAPTURE,
        COMET,
        PULSE,
        LAUNCH,
        LEAP,
        LIGHTNING,
        SMITE,
        TWISTER,
        CHANNEL,
        BURST,
        WARD,
        SEEKER,
        CHAIN,
        STORM,
        WELL,
        PUSH,
        PULL,
        CHOKE,
        AURA,
        SONIC,
        SIPHON,
        MARK,
        SUSPEND,
        VOLLEY,
        LANCE,
        FIELD,
        CANNON,
        METEOR,
        RAIL,
        COLLAPSE,
        DOOM,
        HEAL,
        FROSTFRONT,
        FANGS,
        BLINK,
        FAN,
        SWEEP,
        TETHER,
        BUFF,
        EXECUTE,
        DASH,
        CONE_CHANNEL,
        TOTEM,
        TRAP,
        RICOCHET,
        ORBIT,
        PIERCING_ORB,
        PILLAR,
        CLEANSE,
        RAIN,
        GAZE,
        REMEDY,
        HYMN,
        SERPENT,
        DOMAIN,
        WARCRY,
        SOULBLAST,
        MARTIAL_BUFF,
        BLADE_ARC,
        BLADE_THRUST,
        BOSS_TENTACLE,
        BOSS_ICE,
        BOSS_RIFT,
        BOSS_WRATH,
        BOSS_SAND,
        BOSS_DRAGON,
        BOSS_SWORD,
        COUNTER,
        DRAW,
        REVERSAL,
        BRAND,
        BANNER,
        REBIRTH,
        FORMATION,
        BELL,
        BREAKER,
        CATACLYSM,
        ASCENSION,
        DEVASTATOR,
        EXILE,
        VOID_DOMAIN,
        CAVE_ART,
        GOD_ART,
        ORBITAL_RAY,
        HOLY_SCREAM,
        EXPANSION,
        PETAL_WARD;

    }

    public static enum Theme {
        MAGIC(11749877),
        BLOOD(10555176),
        POISON(6871865),
        FIRE(16744986),
        THUNDER(8118271),
        DARK(6499765),
        GRAVITY(13588712),
        SOUL(2414539),
        HOLY(16770465),
        ICE(10938367);

        public final int color;

        private Theme(int color) {
            this.color = color;
        }
    }
}


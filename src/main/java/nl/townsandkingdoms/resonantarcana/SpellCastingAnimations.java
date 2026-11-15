package nl.townsandkingdoms.resonantarcana;

import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import java.util.EnumMap;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;
import nl.townsandkingdoms.resonantarcana.Spell;

final class SpellCastingAnimations {
    private static final EnumMap<Pose, AnimationHolder> CHARGES = new EnumMap(Pose.class);
    private static final EnumMap<Spell, AnimationHolder> BEAM_RELEASES = new EnumMap(Spell.class);

    private SpellCastingAnimations() {
    }

    static AnimationHolder start(Spell spell) {
        return CHARGES.get((Object)SpellCastingAnimations.pose(spell));
    }

    static AnimationHolder finish(Spell spell) {
        return SpellCastingAnimations.pose(spell) == Pose.BEAM ? BEAM_RELEASES.get((Object)spell) : SpellCastingAnimations.pose((Spell)spell).release;
    }

    static int beamTicks(Spell spell) {
        return switch (spell.kind) {
            case Spell.Kind.DEVASTATOR, Spell.Kind.CHANNEL -> spell.duration();
            case Spell.Kind.RAIL -> 16;
            case Spell.Kind.CANNON -> 10;
            case Spell.Kind.CAVE_ART, Spell.Kind.GOD_ART -> spell.duration();
            default -> 8;
        };
    }

    static Pose pose(Spell spell) {
        return switch (spell) {
            case PLANET_BEFALL, CELESTIAL_SHOWER -> Pose.SKY;
            case PRESERVER_OF_FORTUNE -> Pose.WARD;
            case MUSOU_SHINSETSU -> Pose.SLASH;
            case WINDS_GRAND_ODE -> Pose.RITUAL;
            case Spell.TEMPEST_SPEAR -> Pose.SPEAR;
            case Spell.JUDGEMENT_RAY, Spell.TREMORZILLA_BREATH, Spell.TREMORZILLA_CATACLYSM -> Pose.BEAM;
            case Spell.EVENTIDE_HORIZON -> Pose.SINGULARITY;
            case Spell.PRIMORDIAL_SOVEREIGN -> Pose.GROUND;
            case Spell.ORBITAL_RAY, Spell.THUNDER_OF_THE_HEAVENS, Spell.SOLAR_SUPERNOVA, Spell.HEAVENS_COVENANT, Spell.HELVAR_FINAL_EDICT, Spell.NUCLEAR_STRIKE, Spell.LUX_EXTINCTION, Spell.CANDY_CATASTROPHE, Spell.CONFECTION_APOCALYPSE, Spell.STORM_RAIL -> Pose.SKY;
            case Spell.ANGEL_SCREAM, Spell.SONIC_SCREAM, Spell.SOULBLAST, Spell.WARDENS_JUDGEMENT, Spell.FORSAKEN_SONAR, Spell.FORLORN_RECKONING, Spell.DRAGON_BREATH, Spell.DRAGON_PRIMORDIAL_INFERNO -> Pose.BREATH;
            case Spell.GRAVITY_STAMP, Spell.TECTONIC_STAMPEDE, Spell.EMP_SURGE, Spell.YETI_WORLD_HEART -> Pose.STOMP;
            case Spell.ABSOLUTE_GLACIATION, Spell.VERDANT_AWAKENING, Spell.WORLDROOT_CATASTROPHE, Spell.ABYSSAL_WORLD_TIDE, Spell.KRAKEN_WORLD_DROWN, Spell.SANDWORM_WORLD_DEVOURER, Spell.FALLOUT_BLOOM, Spell.MYCELIAL_RELAY -> Pose.GROUND;
            case Spell.SOUL_ASCENDANCY, Spell.CHOIR_OF_THE_LOST -> Pose.PRAYER;
            case Spell.POLARITY_BOLT, Spell.ACID_GLOB, Spell.BRINE_BUBBLE, Spell.DESOLATE_DAGGER, Spell.GUMBALL_VOLLEY -> Pose.PROJECTILE;
            case Spell.AMBER_TALON -> Pose.SLASH;
            case Spell.SPELL_PRISM, Spell.MARTYRS_AEGIS, Spell.MIRROR_ICE, Spell.AMBER_STASIS, Spell.PRESSURE_BELL, Spell.WHITEOUT_HUNT -> Pose.WARD;
            case Spell.BLOOD_PRICE, Spell.FOLDED_PASSAGE, Spell.ORBIT_EXCHANGE, Spell.ECHO_DECOY, Spell.STOLEN_SHADOW -> Pose.SELF;
            case Spell.MAGNETAR_ANNIHILATION, Spell.ABYSSAL_DELUGE, Spell.FROSTMINT_HEX, Spell.CINDER_FUSE, Spell.LODESTONE_SNARE, Spell.CARAMEL_CATAPULT, Spell.OATHBREAKER_CHAINS, Spell.ARCANE_HAND -> Pose.RITUAL;
            default -> {
                switch (spell.kind) {
                    case DEVASTATOR: 
                    case CHANNEL: 
                    case CANNON: 
                    case LANCE: 
                    case SIPHON: 
                    case TETHER: {
                        yield Pose.BEAM;
                    }
                    case RAIL: {
                        yield Pose.SPEAR;
                    }
                    case LIGHTNING: 
                    case STORM: 
                    case SMITE: 
                    case TWISTER: 
                    case METEOR: 
                    case PILLAR: 
                    case RAIN: 
                    case BOSS_SWORD: {
                        yield Pose.SKY;
                    }
                    case SONIC: 
                    case HOLY_SCREAM: 
                    case SOULBLAST: 
                    case CONE_CHANNEL: 
                    case BOSS_DRAGON: {
                        yield Pose.BREATH;
                    }
                    case SWEEP: 
                    case BLADE_ARC: 
                    case DRAW: 
                    case REVERSAL: 
                    case BREAKER: 
                    case EXECUTE: {
                        yield Pose.SLASH;
                    }
                    case BLADE_THRUST: {
                        yield Pose.PROJECTILE;
                    }
                    case BOSS_WRATH: 
                    case BOSS_ICE: {
                        yield Pose.STOMP;
                    }
                    case FROSTFRONT: 
                    case FANGS: 
                    case TOTEM: 
                    case TRAP: 
                    case BOSS_TENTACLE: 
                    case BOSS_SAND: {
                        yield Pose.GROUND;
                    }
                    case PETAL_WARD: 
                    case WARD: 
                    case BUFF: 
                    case CLEANSE: 
                    case HYMN: 
                    case REMEDY: 
                    case MARTIAL_BUFF: 
                    case COUNTER: 
                    case BANNER: {
                        yield Pose.WARD;
                    }
                    case HEAL: 
                    case AURA: 
                    case BLINK: 
                    case DASH: 
                    case LEAP: 
                    case ASCENSION: 
                    case REBIRTH: {
                        yield Pose.SELF;
                    }
                    case BOLT: 
                    case SEEKER: 
                    case FAN: 
                    case RICOCHET: 
                    case VOLLEY: 
                    case PIERCING_ORB: 
                    case SERPENT: {
                        yield Pose.PROJECTILE;
                    }
                    case TARGET: 
                    case CONFUSE: 
                    case STUN: 
                    case CAPTURE: 
                    case LAUNCH: 
                    case PUSH: 
                    case PULL: 
                    case CHOKE: 
                    case MARK: 
                    case SUSPEND: 
                    case CHAIN: 
                    case GAZE: 
                    case BRAND: {
                        yield Pose.PROJECTILE;
                    }
                }
                yield Pose.RITUAL;
            }
        };
    }

    static {
        for (Pose pose : Pose.values()) {
            CHARGES.put(pose, new AnimationHolder(ResourceLocation.fromNamespaceAndPath((String)"resonant_arcana", (String)("charge_" + pose.name().toLowerCase(Locale.ROOT))), false));
        }
        for (Enum enum_ : Spell.values()) {
            if (SpellCastingAnimations.pose((Spell)enum_) != Pose.BEAM) continue;
            BEAM_RELEASES.put((Spell)enum_, new AnimationHolder(ResourceLocation.fromNamespaceAndPath((String)"resonant_arcana", (String)("beam_release_" + SpellCastingAnimations.beamTicks((Spell)enum_))), true));
        }
    }

    static enum Pose {
        SINGULARITY(SpellAnimations.ANIMATION_CONTINUOUS_CAST, SpellAnimations.ANIMATION_LONG_CAST_FINISH),
        PROJECTILE(SpellAnimations.ONE_HANDED_RAY_CHARGE, SpellAnimations.ANIMATION_INSTANT_CAST),
        BEAM(SpellAnimations.ANIMATION_CONTINUOUS_CAST, AnimationHolder.none()),
        SPEAR(SpellAnimations.CHARGE_RAISED_HAND, SpellAnimations.THROW_SINGLE_ITEM),
        SKY(SpellAnimations.CHARGE_RAISED_HAND, SpellAnimations.ANIMATION_LONG_CAST_FINISH),
        RITUAL(SpellAnimations.CHARGE_WAVY_ANIMATION, SpellAnimations.ANIMATION_LONG_CAST_FINISH),
        WARD(SpellAnimations.PREPARE_CROSS_ARMS, SpellAnimations.SELF_CAST_TWO_HANDS),
        SELF(SpellAnimations.SELF_CAST_TWO_HANDS, SpellAnimations.SELF_CAST_ANIMATION),
        SLASH(SpellAnimations.PREPARE_CROSS_ARMS, SpellAnimations.ONE_HANDED_HORIZONTAL_SWING_ANIMATION),
        OVERHEAD(SpellAnimations.CHARGE_RAISED_HAND, SpellAnimations.OVERHEAD_MELEE_SWING_ANIMATION),
        STOMP(SpellAnimations.PREPARE_CROSS_ARMS, SpellAnimations.STOMP),
        BREATH(SpellAnimations.CHARGE_SPIT_ANIMATION, SpellAnimations.SPIT_FINISH_ANIMATION),
        GROUND(SpellAnimations.CHARGE_WAVY_ANIMATION, SpellAnimations.TOUCH_GROUND_ANIMATION),
        PRAYER(SpellAnimations.CAST_KNEELING_PRAYER, SpellAnimations.SELF_CAST_TWO_HANDS);

        final AnimationHolder charge;
        final AnimationHolder release;

        private Pose(AnimationHolder charge, AnimationHolder release) {
            this.charge = charge;
            this.release = release;
        }
    }
}


package nl.townsandkingdoms.resonantarcana;

import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import nl.townsandkingdoms.resonantarcana.BossesRiseCompat;
import nl.townsandkingdoms.resonantarcana.CavesCompat;
import nl.townsandkingdoms.resonantarcana.ExpansionSpells;
import nl.townsandkingdoms.resonantarcana.GodSpells;
import nl.townsandkingdoms.resonantarcana.IronsCompat;
import nl.townsandkingdoms.resonantarcana.Spell;
import nl.townsandkingdoms.resonantarcana.SpellCategory;
import nl.townsandkingdoms.resonantarcana.SpellInfo;
import nl.townsandkingdoms.resonantarcana.SpellText;

final class SpellIndex {
    private static final Map<ResourceLocation, Spell> IDS = new HashMap<ResourceLocation, Spell>();
    private static final Map<Spell, List<String>> TAGS = new EnumMap<Spell, List<String>>(Spell.class);

    static String tier(Spell s) {
        if (ExpansionSpells.handles(s)) {
            return s.definition.tier().substring(0, 1) + s.definition.tier().substring(1).toLowerCase(Locale.ROOT);
        }
        if (GodSpells.handles(s)) {
            return "God";
        }
        if (CavesCompat.optional(s)) {
            return s.definition.tier().substring(0, 1) + s.definition.tier().substring(1).toLowerCase(Locale.ROOT);
        }
        return s.category() == SpellCategory.CATACLYSM || s == Spell.DOOM || s == Spell.DOOM_BEAM ? "Cataclysm" : (s.manaCost() >= 100 || s.castTicks() >= 60 ? "Master" : (s.manaCost() >= 55 || s.castTicks() >= 30 ? "Expert" : (s.manaCost() >= 30 || s.castTicks() >= 15 ? "Adept" : "Novice")));
    }

    static List<String> tags(Spell s) {
        return TAGS.get((Object)s);
    }

    static boolean matches(Spell s, String query) {
        String haystack = (s.title + " " + s.displayName().getString() + " " + s.bookId() + " " + String.join((CharSequence)" ", SpellIndex.tags(s))).toLowerCase(Locale.ROOT);
        for (String word : query.toLowerCase(Locale.ROOT).trim().split("\\s+")) {
            if (haystack.contains(word)) continue;
            return false;
        }
        return true;
    }

    static List<Spell> ordered() {
        return Arrays.stream(Spell.enabledValues()).sorted(Comparator.comparing((Spell s) -> SpellInfo.school(s.theme)).thenComparing(s -> s.category().ordinal()).thenComparing(s -> s.title)).toList();
    }

    static Spell spell(ItemStack stack) {
        if (!stack.is((Item)ItemRegistry.SCROLL.get())) {
            return null;
        }
        ISpellContainer container = ISpellContainer.get((ItemStack)stack);
        if (container == null || container.isEmpty()) {
            return null;
        }
        return IDS.get(container.getSpellAtIndex(0).getSpell().getSpellResource());
    }

    static void tooltip(ItemTooltipEvent event) {
        Spell spell = SpellIndex.spell(event.getItemStack());
        if (spell == null) {
            return;
        }
        event.getToolTip().add(SpellText.text("tooltip.keywords", "Keywords: %s", String.join((CharSequence)" \u00b7 ", SpellIndex.tags(spell))).withStyle(ChatFormatting.DARK_GRAY));
    }

    private SpellIndex() {
    }

    static {
        for (Spell spell : Spell.values()) {
            IDS.put(IronsCompat.id(spell), spell);
            LinkedHashSet<String> tags = new LinkedHashSet<String>();
            tags.add(SpellInfo.school(spell.theme).toLowerCase(Locale.ROOT));
            tags.add(spell.category().title.toLowerCase(Locale.ROOT));
            tags.add(SpellIndex.tier(spell).toLowerCase(Locale.ROOT));
            if (CavesCompat.optional(spell)) {
                tags.add("alexscaves");
                tags.add(spell.definition.area().replace('_', ' '));
            }
            if (spell.damage > 0.0f) {
                tags.add("damage");
            }
            if (spell.healing() > 0.0f) {
                tags.add("healing");
            }
            if (spell.radius >= 3.0 && spell.category() != SpellCategory.MOBILITY) {
                tags.add("aoe");
            }
            switch (spell.kind) {
                case BUFF: 
                case MARTIAL_BUFF: 
                case BANNER: 
                case ASCENSION: 
                case VOID_DOMAIN: {
                    tags.add("buff");
                    break;
                }
                case CAPTURE: 
                case CHOKE: 
                case SUSPEND: 
                case STUN: 
                case GAZE: 
                case EXILE: 
                case HOLY_SCREAM: {
                    tags.add("debuff");
                    break;
                }
                case BLINK: 
                case DASH: 
                case LEAP: 
                case REVERSAL: {
                    tags.add("mobility");
                    break;
                }
                case TOTEM: 
                case TRAP: 
                case DOMAIN: {
                    tags.add("ground");
                    break;
                }
            }
            if (spell == Spell.RHO_AIAS) {
                tags.add("rho aias shield barrier seven petals defense defence projectile guard protection");
            }
            if (spell == Spell.ANGEL_SCREAM) {
                tags.add("angel holy scream stun root weakness cone");
            }
            if (spell == Spell.VOID_DOMAIN) {
                tags.add("debuff");
                tags.add("void");
                tags.add("domain");
            }
            if (!CavesCompat.optional(spell) && spell.theme == Spell.Theme.FIRE && spell.damage > 0.0f) {
                tags.add("burning");
            }
            if (!CavesCompat.optional(spell) && spell.theme == Spell.Theme.ICE && spell.damage > 0.0f) {
                tags.add("slow");
            }
            if (spell.theme == Spell.Theme.POISON && spell.damage > 0.0f && !BossesRiseCompat.optional(spell) && !CavesCompat.optional(spell)) {
                tags.add("poison");
            }
            if (!CavesCompat.optional(spell) && spell.theme == Spell.Theme.DARK && spell.damage > 0.0f) {
                tags.add("weakness");
            }
            if (spell.pulses() > 1) {
                tags.add("multi hit");
            }
            if (BossesRiseCompat.optional(spell)) {
                tags.add("bossesrise");
            }
            if (CavesCompat.optional(spell)) {
                switch (spell) {
                    case TECTONIC_STAMPEDE: {
                        tags.add("buff melee dinosaur resistance");
                        break;
                    }
                    case POLARITY_BOLT: {
                        tags.add("magnet pull");
                        break;
                    }
                    case EMP_SURGE: {
                        tags.add("stun interrupt emp");
                        break;
                    }
                    case ACID_GLOB: {
                        tags.add("poison acid");
                        break;
                    }
                    case FROSTMINT_HEX: {
                        tags.add("slow freeze buff sugar");
                        break;
                    }
                    case DESOLATE_DAGGER: {
                        tags.add("piercing weakness");
                        break;
                    }
                    case BRINE_BUBBLE: {
                        tags.add("bubble debuff");
                        break;
                    }
                    case ABYSSAL_DELUGE: {
                        tags.add("vortex pull");
                        break;
                    }
                    case HULLBREAKER_SURGE: {
                        tags.add("wave slow");
                        break;
                    }
                    case FORSAKEN_SONAR: {
                        tags.add("sonar slow reveal");
                        break;
                    }
                    case FORLORN_RECKONING: {
                        tags.add("darkness reveal");
                        break;
                    }
                    case LUX_EXTINCTION: {
                        tags.add("burning tephra lux");
                        break;
                    }
                    case NUCLEAR_STRIKE: {
                        tags.add("nuke weakness");
                        break;
                    }
                }
            }
            if (ExpansionSpells.handles(spell)) {
                tags.add(switch (spell) {
                    case Spell.ARCANE_HAND -> "utility drops loot pickup";
                    case Spell.SPELL_PRISM, Spell.MIRROR_ICE -> "projectile redirect mirror recast";
                    case Spell.BLOOD_PRICE, Spell.MARTYRS_AEGIS -> "buff shield ally support";
                    case Spell.MYCELIAL_RELAY -> "healing relay mushroom ally";
                    case Spell.CHOIR_OF_THE_LOST -> "souls healing recast";
                    case Spell.FOLDED_PASSAGE, Spell.ORBIT_EXCHANGE -> "teleport rift swap";
                    case Spell.CINDER_FUSE -> "fuse mark fire delayed";
                    case Spell.STORM_RAIL -> "recast dash lightning trap";
                    case Spell.ECHO_DECOY -> "decoy taunt sonic";
                    case Spell.LODESTONE_SNARE -> "magnet pull slow trap";
                    case Spell.AMBER_STASIS -> "stasis defense protection";
                    case Spell.FALLOUT_BLOOM -> "cleanse poison wither radiation";
                    case Spell.PRESSURE_BELL -> "water breathing projectile recast";
                    case Spell.STOLEN_SHADOW -> "reflect echo shadow";
                    case Spell.CARAMEL_CATAPULT -> "launch bounce sticky slow";
                    case Spell.OATHBREAKER_CHAINS -> "sword chain melee";
                    case Spell.WHITEOUT_HUNT -> "stealth snow reveal glow";
                    default -> "utility";
                });
            }
            if(ElementalBursts.handles(spell)) {
                tags.add("genshin elemental burst");
                tags.add(switch(spell) {
                    case PLANET_BEFALL -> "zhongli geo meteor petrified petrification stun";
                    case PRESERVER_OF_FORTUNE -> "qiqi cryo fortune talisman healing";
                    case MUSOU_SHINSETSU -> "raiden electro sword slash";
                    case WINDS_GRAND_ODE -> "venti anemo wind vortex pull";
                    default -> "ganyu cryo ice icicle";
                });
            }
            TAGS.put(spell, List.copyOf(tags));
        }
    }
}


package nl.townsandkingdoms.resonantarcana;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.DeferredRegister;

final class ArcanaEffects {
    static final DeferredRegister<MobEffect> REGISTER = DeferredRegister.create((ResourceKey)Registries.MOB_EFFECT, (String)"resonant_arcana");
    static final Holder<MobEffect> WARCRY = REGISTER.register("warcry", () -> new Beneficial(16766346).addAttributeModifier(Attributes.ATTACK_DAMAGE, ArcanaEffects.id("warcry_melee"), 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL).addAttributeModifier((Holder)AttributeRegistry.SPELL_POWER, ArcanaEffects.id("warcry_magic"), 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    static final Holder<MobEffect> ARCANE_EDGE = REGISTER.register("arcane_edge", () -> new Beneficial(11749877).addAttributeModifier(Attributes.ATTACK_DAMAGE, ArcanaEffects.id("edge_damage"), 0.5, AttributeModifier.Operation.ADD_VALUE).addAttributeModifier(Attributes.ATTACK_SPEED, ArcanaEffects.id("edge_speed"), 0.025, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    static final Holder<MobEffect> TEMPO = REGISTER.register("battle_tempo", () -> new Beneficial(8118271).addAttributeModifier(Attributes.ATTACK_SPEED, ArcanaEffects.id("tempo_attack"), 0.025, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL).addAttributeModifier(Attributes.MOVEMENT_SPEED, ArcanaEffects.id("tempo_move"), 0.025, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    static final Holder<MobEffect> GLACIAL_GUARD = REGISTER.register("glacial_guard", () -> new Beneficial(10938367));
    static final Holder<MobEffect> THUNDERBRAND = REGISTER.register("thunderbrand", () -> new Beneficial(8118271));
    static final Holder<MobEffect> PHANTOM_FORMATION = REGISTER.register("phantom_formation", () -> new Beneficial(11749877));
    static final Holder<MobEffect> SERAPH_FLIGHT = REGISTER.register("seraph_flight", () -> new Beneficial(16771493).addAttributeModifier(NeoForgeMod.CREATIVE_FLIGHT, ArcanaEffects.id("seraph_flight"), 1.0, AttributeModifier.Operation.ADD_VALUE));
    static final Holder<MobEffect> SERAPH_GRACE = REGISTER.register("seraph_grace", () -> new Beneficial(16771493).addAttributeModifier((Holder)AttributeRegistry.SPELL_POWER, ArcanaEffects.id("seraph_power"), 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    static final Holder<MobEffect> VOID_EMPOWERMENT = REGISTER.register("void_empowerment", () -> new Beneficial(10249694).addAttributeModifier((Holder)AttributeRegistry.SPELL_POWER, ArcanaEffects.id("void_power"), 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    static final Holder<MobEffect> PRIMAL_INSTINCT = REGISTER.register("primal_instinct", () -> new Beneficial(16759893).addAttributeModifier(Attributes.ATTACK_DAMAGE, ArcanaEffects.id("primal_damage"), 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL).addAttributeModifier(Attributes.ATTACK_SPEED, ArcanaEffects.id("primal_attack"), 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL).addAttributeModifier(Attributes.MOVEMENT_SPEED, ArcanaEffects.id("primal_move"), 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    static final Holder<MobEffect> COVENANT_GRACE = REGISTER.register("covenant_grace", () -> new Beneficial(16770465).addAttributeModifier((Holder)AttributeRegistry.SPELL_POWER, ArcanaEffects.id("covenant_power"), 0.35, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    static final Holder<MobEffect> DEAFENED = REGISTER.register("deafened", () -> new Harmful(6675685).addAttributeModifier(Attributes.MOVEMENT_SPEED, ArcanaEffects.id("sonic_shock_move"), -0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL).addAttributeModifier(Attributes.ATTACK_SPEED, ArcanaEffects.id("sonic_shock_attack"), -0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    static final Holder<MobEffect> DRAGON_WINGS = REGISTER.register("dragon_wings", () -> new Beneficial(16742461).addAttributeModifier(NeoForgeMod.CREATIVE_FLIGHT, ArcanaEffects.id("dragon_wings_flight"), 1.0, AttributeModifier.Operation.ADD_VALUE));

    static final Holder<MobEffect> PETRIFIED = REGISTER.register("petrified", () -> new Harmful(0xc5a158)
        .addAttributeModifier(Attributes.MOVEMENT_SPEED, id("petrified_move"), -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
        .addAttributeModifier(Attributes.ATTACK_SPEED, id("petrified_attack"), -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    static final Holder<MobEffect> FORTUNE_TALISMAN = REGISTER.register("fortune_talisman", () -> new Harmful(0xbdefff));

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath((String)"resonant_arcana", (String)path);
    }

    private ArcanaEffects() {
    }

    private static final class Beneficial
    extends MobEffect {
        Beneficial(int color) {
            super(MobEffectCategory.BENEFICIAL, color);
        }
    }

    private static final class Harmful
    extends MobEffect {
        Harmful(int color) {
            super(MobEffectCategory.HARMFUL, color);
        }
    }
}


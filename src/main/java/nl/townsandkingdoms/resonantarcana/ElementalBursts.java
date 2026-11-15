package nl.townsandkingdoms.resonantarcana;

import static nl.townsandkingdoms.resonantarcana.SpellEngine.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.NbtUtils;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Five finite, server-owned bursts. All constructs are visual displays, never terrain. */
final class ElementalBursts {
    static final Set<Spell> SPELLS = Collections.unmodifiableSet(EnumSet.of(Spell.PLANET_BEFALL,
        Spell.PRESERVER_OF_FORTUNE, Spell.MUSOU_SHINSETSU, Spell.WINDS_GRAND_ODE, Spell.CELESTIAL_SHOWER));
    static boolean handles(Spell spell) { return SPELLS.contains(spell); }
    static final class State {
        final List<Display.BlockDisplay> props = new ArrayList<>();
        final List<Vec3> offsets = new ArrayList<>();
        Vec3 top;
    }
    private ElementalBursts() {}

    private static Vec3 mark(ServerPlayer player, Spell spell) {
        if (spell == Spell.PRESERVER_OF_FORTUNE || spell == Spell.MUSOU_SHINSETSU)
            return player.position().add(0, .15, 0);
        Vec3 eye = player.getEyePosition();
        Vec3 end = clipLoaded(player.serverLevel(), eye, player.getLookAngle(), spell.range, player);
        LivingEntity enemy = target(player, eye, end, .5);
        Vec3 point = BossesRiseCompat.ground(player.serverLevel(), enemy == null ? end : enemy.position());
        if (point == null || !player.serverLevel().hasChunkAt(BlockPos.containing(point))) return null;
        point = point.add(0, .2, 0);
        return clipLoaded(player.serverLevel(), eye, point.subtract(eye).normalize(), eye.distanceTo(point), player)
            .distanceToSqr(point) < .04 ? point : null;
    }
    static boolean canStart(ServerPlayer player, Spell spell) {
        Vec3 point = mark(player, spell);
        if (point == null) return false;
        return spell != Spell.PLANET_BEFALL || clipLoaded(player.serverLevel(), point,
            new Vec3(0, 1, 0), 28, player).y - point.y >= 4;
    }
    static void charge(ServerPlayer player, Spell spell, double progress, int elapsed) {
        if (elapsed % 4 != 0) return;
        Vec3 point = mark(player, spell);
        if (point == null) return;
        SpellVisuals.send(player.serverLevel(), spell, SpellVisuals.Stage.RITUAL_TARGET, point, point.add(0, 1, 0), spell.radius);
        SpellVisuals.send(player.serverLevel(), spell, SpellVisuals.Stage.CHARGE,
            player.position().add(0, .1, 0), point, 2 + progress * 3);
    }
    static boolean begin(Cast c) {
        c.pos = mark(c.caster, c.spell);
        if (c.pos == null || !canStart(c.caster, c.spell)) return false;
        c.duration = c.spell.duration();
        c.burst = new State();
        if (c.spell == Spell.PLANET_BEFALL) {
            c.burst.top = clipLoaded(c.level, c.pos, new Vec3(0, 1, 0), 28, c.caster).add(0, -2, 0);
            display(c, c.burst.top, Vec3.ZERO, 4.4f, Blocks.CHISELED_DEEPSLATE);
            for (int i = 0; i < 6; i++) {
                Vec3 offset = switch(i) {
                    case 0 -> new Vec3(2.2, 0, 0); case 1 -> new Vec3(-2.2, 0, 0);
                    case 2 -> new Vec3(0, 2.2, 0); case 3 -> new Vec3(0, -2.2, 0);
                    case 4 -> new Vec3(0, 0, 2.2); default -> new Vec3(0, 0, -2.2);
                };
                display(c, c.burst.top, offset, 1.3f, Blocks.GOLD_BLOCK);
            }
        } else if (c.spell == Spell.CELESTIAL_SHOWER) {
            Vec3 top = clipLoaded(c.level, c.pos, new Vec3(0, 1, 0), 10, c.caster).add(0, -.6, 0);
            c.burst.top = top;
            display(c, top, Vec3.ZERO, 2.2f, Blocks.BLUE_ICE);
        }
        SpellVisuals.send(c.level, c.spell, SpellVisuals.Stage.AREA, c.pos, c.pos.add(0, 1, 0), c.spell.radius);
        return true;
    }
    private static void display(Cast c, Vec3 point, Vec3 offset, float size, net.minecraft.world.level.block.Block block) {
        // Reuse the registered no-save, finite-lifetime prop type so server restarts cannot leave permanent meteors.
        SpellCentrepieces.Prop prop = new SpellCentrepieces.Prop(SpellCentrepieces.PROP.get(), c.level);
        CompoundTag data = new CompoundTag();
        data.put("block_state", NbtUtils.writeBlockState(block.defaultBlockState()));
        CompoundTag transform = new CompoundTag();
        transform.put("translation", floats(-size / 2, -size / 2, -size / 2));
        transform.put("scale", floats(size, size, size));
        Quaternionf rotation = new Quaternionf().rotateXYZ(.25f, .5f, .15f);
        transform.put("left_rotation", floats(rotation.x, rotation.y, rotation.z, rotation.w));
        transform.put("right_rotation", floats(0, 0, 0, 1));
        data.put("transformation", transform);
        data.putBoolean("Invulnerable", true);
        data.putInt("teleport_duration", 2);
        data.putFloat("view_range", 1.5f);
        data.putFloat("width", 8);
        data.putFloat("height", 8);
        prop.configure(data);
        prop.setPos(point.add(offset));
        prop.setInvulnerable(true);
        c.level.addFreshEntity(prop);
        c.centrepieces.add(prop);
        c.burst.props.add(prop);
        c.burst.offsets.add(offset);
    }
    private static ListTag floats(float... values) {
        ListTag result = new ListTag();
        for(float value : values) result.add(FloatTag.valueOf(value));
        return result;
    }
    private static List<LivingEntity> enemies(Cast c, Vec3 center, double radius) {
        return near(c.caster, center, radius).stream()
            .filter(v -> v.getBoundingBox().getCenter().distanceToSqr(center) <= radius * radius
                && visible(c, center.add(0, .4, 0), v.getBoundingBox().getCenter()))
            .limit(64).toList();
    }
    private static void damage(Cast c, LivingEntity victim, Vec3 center, float multiplier) {
        float old = c.weaponBonus;
        c.weaponBonus = old + c.spell.damage * (multiplier - 1);
        try { hit(c, victim, center, true); } finally { c.weaponBonus = old; }
    }
    static boolean tick(Cast c) {
        switch(c.spell) {
            case PLANET_BEFALL -> meteor(c);
            case PRESERVER_OF_FORTUNE -> fortune(c);
            case MUSOU_SHINSETSU -> musou(c);
            case WINDS_GRAND_ODE -> vortex(c);
            case CELESTIAL_SHOWER -> shower(c);
            default -> throw new IllegalArgumentException("Unknown elemental burst " + c.spell);
        }
        return c.age < c.duration;
    }
    private static void meteor(Cast c) {
        if (c.age <= 36) {
            Vec3 point = c.burst.top.lerp(c.pos.add(0, 2, 0), Math.pow(c.age / 36.0, 2));
            for (int i = 0; i < c.burst.props.size(); i++) c.burst.props.get(i).setPos(point.add(c.burst.offsets.get(i)));
            if (c.age % 2 == 0) {
                SpellVisuals.send(c.level, c.spell, SpellVisuals.Stage.FLIGHT, point, point.add(0, 2, 0), 4);
                dust(c, point, 0xe4ae35, 3);
            }
        }
        if (c.age == 36) {
            c.hit.clear();
            for (LivingEntity victim : enemies(c, c.pos, c.spell.radius)) {
                damage(c, victim, c.pos, 1);
                if (victim.isAlive()) BurstStatus.petrify(victim, 80);
            }
            c.burst.props.forEach(Display.BlockDisplay::discard);
            impact(c, c.pos, c.spell.radius, "entity.generic.explode", .65f);
            SpellParticles.send(c.level, ParticleTypes.EXPLOSION_EMITTER, c.pos.x, c.pos.y + 1, c.pos.z, 1, 0, 0, 0, 0);
        }
        if (c.age >= 36 && c.age % 4 == 0) ring(c, c.pos, c.spell.radius * (c.age - 35) / 29, 0xe4ae35);
    }
    private static void fortune(Cast c) {
        if (c.age != 1) return;
        for (LivingEntity victim : enemies(c, c.pos, c.spell.radius)) {
            damage(c, victim, c.pos, 1);
            effect(victim, ArcanaEffects.FORTUNE_TALISMAN, c.duration, 0);
            BurstStatus.mark(victim, c);
        }
        for (LivingEntity ally : SpellActions.allies(c, c.pos, c.spell.radius)) SpellActions.heal(c, ally, c.spell.healing());
        impact(c, c.pos, c.spell.radius, "block.amethyst_cluster.break", .8f);
    }
    private static void musou(Cast c) {
        if (c.age != 1 && !(c.age >= 31 && c.age <= 121 && (c.age - 31) % 30 == 0)) return;
        Vec3 facing = c.dir.multiply(1, 0, 1).normalize();
        if (facing.lengthSqr() < .01) facing = new Vec3(0, 0, 1);
        c.hit.clear();
        for (LivingEntity victim : enemies(c, c.pos, c.spell.radius)) {
            Vec3 delta = victim.position().subtract(c.pos).multiply(1, 0, 1);
            if (delta.lengthSqr() < 4 || delta.normalize().dot(facing) >= .25)
                damage(c, victim, c.pos, c.age == 1 ? 2 : 1);
        }
        Vec3 side = new Vec3(-facing.z, 0, facing.x);
        Vec3 center = c.pos.add(facing.scale(6)).add(0, 1.2, 0);
        SpellVisuals.send(c.level, c.spell, SpellVisuals.Stage.STRIKE,
            center.subtract(side.scale(9)), center.add(side.scale(9)), 3);
        impact(c, center, 3, "entity.lightning_bolt.impact", 1.3f);
    }
    private static void vortex(Cast c) {
        if (c.age % 2 == 1) {
            for (LivingEntity victim : enemies(c, c.pos, c.spell.radius)) {
                if (victim.getType().is(net.neoforged.neoforge.common.Tags.EntityTypes.BOSSES)) continue;
                double resistance = 1 - Math.clamp(victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0, 1);
                Vec3 delta = c.pos.add(0, .6, 0).subtract(victim.getBoundingBox().getCenter());
                victim.setDeltaMovement(victim.getDeltaMovement().scale(.5).add(delta.normalize().scale(Math.min(.28, delta.length() * .12) * resistance)));
                victim.hurtMarked = true;
            }
        }
        if ((c.age - 1) % 20 == 0) {
            c.hit.clear();
            for (LivingEntity victim : enemies(c, c.pos, c.spell.radius)) damage(c, victim, c.pos, 1);
            impact(c, c.pos, 4, "entity.breeze.wind_burst", 1);
        }
        if (c.age % 4 == 0) {
            SpellVisuals.send(c.level, c.spell, SpellVisuals.Stage.AREA, c.pos, c.pos.add(0, 5, 0), c.spell.radius);
            for (int i = 0; i < 12; i++) {
                double a = c.age * .2 + i * Math.PI / 6;
                double y = (i % 6) * .7;
                dust(c, c.pos.add(Math.cos(a) * (1 + y * .5), y, Math.sin(a) * (1 + y * .5)), 0x63e6bc, 1.3f);
            }
        }
    }
    private static void shower(Cast c) {
        if(c.age%4==0)SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.AREA,c.pos,c.burst.top,c.spell.radius);
        if ((c.age - 1) % 20 != 0) return;
        c.hit.clear();
        List<LivingEntity> victims = enemies(c, c.pos, c.spell.radius);
        // One selected landing point per volley; overlap never multiplies a victim's damage.
        Vec3 point = victims.isEmpty() ? c.pos : victims.get(((c.age - 1) / 20) % victims.size()).position();
        Vec3 top = clipLoaded(c.level, point.add(0, .4, 0), new Vec3(0, 1, 0), 10, c.caster);
        SpellVisuals.send(c.level, c.spell, SpellVisuals.Stage.STRIKE, top, point, 1.5);
        for (LivingEntity victim : enemies(c, point, 3)) {
            damage(c, victim, point, 1);
            effect(victim, MobEffects.MOVEMENT_SLOWDOWN, 45, 1);
        }
        impact(c, point, 3, "block.glass.break", 1.3f);
        ring(c, c.pos, c.spell.radius, 0xaeeaff);
    }
    private static void impact(Cast c, Vec3 point, double radius, String sound, float pitch) {
        SpellVisuals.send(c.level, c.spell, SpellVisuals.Stage.IMPACT, point, point.add(0, 1, 0), radius);
        SpellAudio.world(c.level, point, sound, 1.2f, pitch);
    }
    private static void ring(Cast c, Vec3 point, double radius, int color) {
        for (int i = 0; i < 32; i++) {
            double a = i * Math.PI / 16;
            dust(c, point.add(Math.cos(a) * radius, .15, Math.sin(a) * radius), color, 1.3f);
        }
    }
    private static void dust(Cast c, Vec3 point, int rgb, float size) {
        Vector3f color = new Vector3f((rgb >> 16 & 255) / 255f, (rgb >> 8 & 255) / 255f, (rgb & 255) / 255f);
        SpellParticles.point(c.level, new DustParticleOptions(color, size), point, Vec3.ZERO);
    }
    static void cleanup(Cast c) {
        if (c.burst != null) c.burst.props.forEach(Display.BlockDisplay::discard);
    }
}

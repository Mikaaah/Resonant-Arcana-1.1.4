package nl.townsandkingdoms.resonantarcana;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Original elemental geometry, using the existing particle budgets and per-spell visibility scope. */
final class BurstClientEffects {
    private BurstClientEffects() {}
    static void visual(WandNetwork.Visual message) {
        if (!message.valid()) return;
        Spell spell = Spell.values()[message.spell()];
        FirstPersonEffects.forSpell(spell, () -> render(message, spell));
    }
    private static void render(WandNetwork.Visual message, Spell spell) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) return;
        Vec3 point = new Vec3(message.x(), message.y(), message.z());
        Vec3 end = new Vec3(message.endX(), message.endY(), message.endZ());
        if (mc.player.position().distanceToSqr(point.lerp(end, .5)) > 96 * 96) return;
        SpellVisuals.Stage stage = SpellVisuals.Stage.values()[message.stage()];
        double size = message.size(), time = level.getGameTime() * .15;
        int color = switch(spell) {
            case PLANET_BEFALL -> 0xe5b348;
            case PRESERVER_OF_FORTUNE -> 0xc3f3ff;
            case MUSOU_SHINSETSU -> 0xba89ff;
            case WINDS_GRAND_ODE -> 0x64e5bd;
            default -> 0x8ad8ff;
        };
        if (stage == SpellVisuals.Stage.CHARGE || stage == SpellVisuals.Stage.RITUAL_TARGET) {
            ring(level, point, size, color, true);
            if (stage == SpellVisuals.Stage.CHARGE) {
                for (int i = 0; i < 32; i++) {
                    double a = i * Math.PI / 16 + time;
                    dust(level, point.add(Math.cos(a) * 1.6, .3 + (i % 8) * .2, Math.sin(a) * 1.6), color, 1);
                }
            }
            return;
        }
        if (spell == Spell.PRESERVER_OF_FORTUNE && stage == SpellVisuals.Stage.LINK) {
            // A floating fortune seal: diamond frame, central stroke and two crossbars.
            line(level, point.add(-.3, .4, 0), point.add(0, .8, 0), color);
            line(level, point.add(0, .8, 0), point.add(.3, .4, 0), color);
            line(level, point.add(.3, .4, 0), point, color);
            line(level, point, point.add(-.3, .4, 0), color);
            line(level, point.add(0, .15, 0), point.add(0, .65, 0), 0xffe6af);
            line(level, point.add(-.12, .3, 0), point.add(.12, .3, 0), 0xffe6af);
            line(level, point.add(-.12, .5, 0), point.add(.12, .5, 0), 0xffe6af);
            return;
        }
        if (stage == SpellVisuals.Stage.STRIKE) {
            line(level, point, end, color);
            if (spell == Spell.MUSOU_SHINSETSU) {
                line(level, point.add(0, .25, 0), end.add(0, -.25, 0), 0xf2daff);
                for (int i = 0; i < 16; i++) {
                    Vec3 p = point.lerp(end, i / 15.0);
                    NativeSpellEffects.point(level, ParticleTypes.ELECTRIC_SPARK, p,
                        new Vec3(0, .03, 0), 10, false);
                }
            }
        }
        if (stage == SpellVisuals.Stage.IMPACT) {
            ring(level, point, size, color, false);
            for (int i = 0; i < 72; i++) {
                double a = i * 2.399963, r = 1 + (i % 12) * size / 12;
                Vec3 p = point.add(Math.cos(a) * r, .2 + i % 5 * .3, Math.sin(a) * r);
                dust(level, p, color, 1.3f);
                if (spell == Spell.PRESERVER_OF_FORTUNE || spell == Spell.CELESTIAL_SHOWER)
                    NativeSpellEffects.point(level, ParticleTypes.SNOWFLAKE, p, new Vec3(0, -.03, 0), 25, false);
            }
        }
        if (spell == Spell.PLANET_BEFALL && stage == SpellVisuals.Stage.FLIGHT) {
            for (int i = 0; i < 48; i++) {
                double a = i * Math.PI / 24 + time;
                dust(level, point.add(Math.cos(a) * 3, .5 + i % 4, Math.sin(a) * 3), color, 1.5f);
            }
        }
        if (spell == Spell.WINDS_GRAND_ODE && stage == SpellVisuals.Stage.AREA) {
            for (int i = 0; i < 100; i++) {
                double y = i * .055, a = i * .34 + time, r = 1.2 + y * .55;
                dust(level, point.add(Math.cos(a) * r, y, Math.sin(a) * r), color, 1.2f);
            }
            ring(level, point, size, color, true);
        }
        if (spell == Spell.CELESTIAL_SHOWER && stage == SpellVisuals.Stage.AREA) {
            ring(level, point, size, color, true);
            for (int i = 0; i < 48; i++) {
                double a = i * 2.399963 + time, y = 1 - 2 * (i + .5) / 48, r = Math.sqrt(1 - y * y) * 1.7;
                Vec3 p = end.add(Math.cos(a) * r, y * 1.7, Math.sin(a) * r);
                dust(level, p, color, 1);
            }
        }
    }
    private static void ring(ClientLevel level, Vec3 center, double radius, int color, boolean boundary) {
        var dust = particle(color, 1);
        for (int i = 0; i < 64; i++) {
            double a = i * Math.PI / 32;
            NativeSpellEffects.point(level, dust, center.add(Math.cos(a) * radius, .12, Math.sin(a) * radius), Vec3.ZERO, 18, boundary);
        }
    }
    private static void line(ClientLevel level, Vec3 from, Vec3 to, int color) {
        for (int i = 0; i <= 48; i++) dust(level, from.lerp(to, i / 48.0), color, 1.1f);
    }
    private static DustParticleOptions particle(int color, float size) {
        return new DustParticleOptions(new Vector3f((color >> 16 & 255) / 255f,
            (color >> 8 & 255) / 255f, (color & 255) / 255f), size);
    }
    private static void dust(ClientLevel level, Vec3 point, int color, float size) {
        NativeSpellEffects.point(level, particle(color, size), point, Vec3.ZERO, 16, false);
    }
}

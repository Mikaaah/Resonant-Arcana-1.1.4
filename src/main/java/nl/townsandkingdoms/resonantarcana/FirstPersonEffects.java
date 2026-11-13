package nl.townsandkingdoms.resonantarcana;

import java.util.ArrayDeque;
import java.util.Iterator;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Vector3f;

@EventBusSubscriber(modid="resonant_arcana", value={Dist.CLIENT})
public final class FirstPersonEffects {
    private static final int MAX_TRACKED = 16384;
    private static final ArrayDeque<Tracked> TRACKED = new ArrayDeque<>();
    private static ClientLevel world;
    private static Spell currentSpell;
    private record Tracked(Particle particle, Spell spell) {}

    static void forSpell(Spell spell, Runnable effect) {
        Spell previous = currentSpell;
        currentSpell = spell;
        try { effect.run(); } finally { currentSpell = previous; }
    }
    // Retained for development checks; runtime callers use exact spell identity.
    static void withLightning(boolean clear, Runnable effect) {
        forSpell(clear ? Spell.LIGHTNING : null, effect);
    }

    static Scope scope(Spell spell) { return new Scope(spell); }
    static final class Scope implements AutoCloseable {
        private final Spell previous;
        private Scope(Spell spell) { previous = currentSpell; currentSpell = spell; }
        @Override public void close() { currentSpell = previous; }
    }
    private FirstPersonEffects() {
    }

    private static boolean enabled() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && mc.getCameraEntity() != null && mc.options.getCameraType().isFirstPerson();
    }

    static boolean cameraClear(Vec3 position) {
        return FirstPersonEffects.cameraClear(position, 0.0);
    }

    private static boolean cameraClear(Vec3 position, double radius) {
        if (!SpellVisibilityConfig.clear(currentSpell) || !FirstPersonEffects.enabled()) {
            return true;
        }
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 offset = position.subtract(camera.getPosition());
        Vector3f look = camera.getLookVector();
        return !AimVisibility.obscures(offset.x, offset.y, offset.z, look.x(), look.y(), look.z(), radius);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean cameraClear(Particle particle) {
        double d;
        Vec3 vec3 = particle.getPos();
        if (particle instanceof SingleQuadParticle) {
            SingleQuadParticle quad = (SingleQuadParticle)particle;
            d = quad.getQuadSize(0.0f);
        } else {
            d = 0.15;
        }
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 offset = vec3.subtract(camera.getPosition());
        Vector3f look = camera.getLookVector();
        return !AimVisibility.obscures(offset.x, offset.y, offset.z, look.x(), look.y(), look.z(), d);
    }

    static Particle create(ParticleOptions options, Vec3 position, Vec3 parameters) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !FirstPersonEffects.cameraClear(position)) {
            return null;
        }
        Particle particle = mc.particleEngine.createParticle(options, position.x, position.y, position.z, parameters.x, parameters.y, parameters.z);
        if (particle != null && currentSpell != null) {
            if (enabled() && SpellVisibilityConfig.clear(currentSpell) && !FirstPersonEffects.cameraClear(particle)) {
                FirstPersonEffects.hide(particle);
                return null;
            }
            if (world != mc.level) {
                TRACKED.clear();
                world = mc.level;
            }
            if (TRACKED.size() == 16384) {
                TRACKED.removeFirst();
            }
            TRACKED.addLast(new Tracked(particle, currentSpell));
        }
        return particle;
    }

    private static void hide(Particle particle) {
        particle.scale(0.0f);
        particle.remove();
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Pre event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != world) {
            TRACKED.clear();
            world = level;
        }
        boolean clearAim = FirstPersonEffects.enabled();
        Iterator<Tracked> iterator = TRACKED.iterator();
        while (iterator.hasNext()) {
            Tracked tracked = iterator.next();
            Particle particle = tracked.particle();
            if (!particle.isAlive()) {
                iterator.remove();
                continue;
            }
            if (!clearAim || !SpellVisibilityConfig.clear(tracked.spell()) || FirstPersonEffects.cameraClear(particle)) continue;
            FirstPersonEffects.hide(particle);
            iterator.remove();
        }
    }

    static void handle(WandNetwork.Particles message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !message.valid()) {
            return;
        }
        ClientboundLevelParticlesPacket packet = message.particle();
        if (packet.getCount() == 0) {
            FirstPersonEffects.spawn(packet, message.spell(), new Vec3(packet.getX(), packet.getY(), packet.getZ()), new Vec3((double)(packet.getMaxSpeed() * packet.getXDist()), (double)(packet.getMaxSpeed() * packet.getYDist()), (double)(packet.getMaxSpeed() * packet.getZDist())));
        } else {
            RandomSource random = mc.level.random;
            for (int i = 0; i < packet.getCount(); ++i) {
                Vec3 p = new Vec3(packet.getX() + random.nextGaussian() * (double)packet.getXDist(), packet.getY() + random.nextGaussian() * (double)packet.getYDist(), packet.getZ() + random.nextGaussian() * (double)packet.getZDist());
                Vec3 v = new Vec3(random.nextGaussian() * (double)packet.getMaxSpeed(), random.nextGaussian() * (double)packet.getMaxSpeed(), random.nextGaussian() * (double)packet.getMaxSpeed());
                FirstPersonEffects.spawn(packet, message.spell(), p, v);
            }
        }
    }

    private static void spawn(ClientboundLevelParticlesPacket packet, int spellId, Vec3 p, Vec3 v) {
        Minecraft mc = Minecraft.getInstance();
        boolean force = packet.isOverrideLimiter() || packet.getParticle().getType().getOverrideLimiter();
        ParticleStatus setting = (ParticleStatus)mc.options.particles().get();
        if (setting == ParticleStatus.DECREASED && mc.level.random.nextInt(3) == 0) {
            setting = ParticleStatus.MINIMAL;
        }
        if (!force && (setting == ParticleStatus.MINIMAL || mc.gameRenderer.getMainCamera().getPosition().distanceToSqr(p) > 1024.0)) {
            return;
        }
        FirstPersonEffects.forSpell(spellId < 0 ? null : Spell.values()[spellId], () -> FirstPersonEffects.create(packet.getParticle(), p, v));
    }
}


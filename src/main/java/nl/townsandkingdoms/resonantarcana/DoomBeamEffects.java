package nl.townsandkingdoms.resonantarcana;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsspellbooks.render.SpellRenderingHelper;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@EventBusSubscriber(
   modid = "resonant_arcana",
   value = {Dist.CLIENT}
)
final class DoomBeamEffects {
   private static final Map<DoomBeamEffects.Key, DoomBeamEffects.Lease> CASTERS = new HashMap<>();
   private static ClientLevel world;
   private static final ParticleOptions PURPLE = dust(0.65F, 0.18F, 1.0F, 1.25F);
   private static final ParticleOptions YELLOW = dust(1.0F, 0.82F, 0.15F, 1.2F);
   private static final ParticleOptions ARCANE_WHITE = dust(1.0F, 0.9F, 0.72F, 1.1F);
   private static final ParticleOptions GREEN = dust(0.12F, 1.0F, 0.015F, 1.5F);
   private static final ParticleOptions WHITE = dust(0.8F, 1.0F, 0.65F, 1.4F);
   private static final ParticleOptions GOLD = dust(1.0F, 0.65F, 0.18F, 1.0F);

   static void track(WandNetwork.BeamFocus cue) {
      ClientLevel level = Minecraft.getInstance().level;
      if (level != null && cue.valid()) {
         if (level != world) {
            CASTERS.clear();
            world = level;
         }

         DoomBeamEffects.Key key = new DoomBeamEffects.Key(cue.caster(), cue.spell());
         if (cue.phase() == 0) {
            CASTERS.remove(key);
         } else {
            if (CASTERS.size() < 16 || CASTERS.containsKey(key)) {
               CASTERS.put(key, new DoomBeamEffects.Lease(Spell.values()[cue.spell()], cue.phase(), cue.progress(), cue.length(), level.getGameTime() + 8L));
            }
         }
      }
   }

   private static void prune() {
      ClientLevel level = Minecraft.getInstance().level;
      if (level != world) {
         CASTERS.clear();
         world = level;
      }

      CASTERS.entrySet()
         .removeIf(
            e -> level == null
                  || e.getValue().expires() < level.getGameTime()
                  || level.getEntity(e.getKey().caster()) == null
                  || !level.getEntity(e.getKey().caster()).isAlive()
         );
   }

   private static DoomBeamEffects.Endpoints endpoints(Entity caster, DoomBeamEffects.Lease lease, float partial) {
      Vec3 eye = caster.getEyePosition(partial);
      Vec3 dir = caster.getViewVector(partial).normalize();
      Vec3 start = lease.phase() == 1
         ? eye
         : world.clip(
               new ClipContext(BeamRitual.body(caster, partial), BeamRitual.body(caster, partial).add(dir.scale(0.18)), Block.COLLIDER, Fluid.NONE, caster)
            )
            .getLocation();
      Vec3 end = world.clip(new ClipContext(start, start.add(dir.scale((double)lease.length())), Block.COLLIDER, Fluid.NONE, caster)).getLocation();
      if (lease.phase() == 1) {
         Vec3 focus = end;
         if ((BeamRenderer.firstPerson(caster) && SpellVisibilityConfig.clear(lease.spell()))) {
            Vector3f up = Minecraft.getInstance().gameRenderer.getMainCamera().getUpVector();
            focus = end.add((double)(-up.x()) * 0.8, (double)(-up.y()) * 0.8, (double)(-up.z()) * 0.8);
         }

         return new DoomBeamEffects.Endpoints(focus, focus.add(dir));
      } else {
         return new DoomBeamEffects.Endpoints(BeamRenderer.presentationStart(caster, start, end), end);
      }
   }

   static void tick() {
      prune();
      if (world != null && ArcanaConfig.CLIENT.isLoaded()) {
         for (Entry<DoomBeamEffects.Key, DoomBeamEffects.Lease> e : CASTERS.entrySet()) {
            DoomBeamEffects.Lease lease = e.getValue();
            // Doom is a particle volume, updated each tick. Keep Arcane's existing cadence.
            if (lease.spell() != Spell.DOOM_BEAM && world.getGameTime() % 2L != 0L) continue;
            DoomBeamEffects.Endpoints points = endpoints(world.getEntity(e.getKey().caster()), lease, 1.0F);
            Vec3 particleStart = lease.phase() == 1
               ? points.start()
               : BeamRenderer.particleStart(world.getEntity(e.getKey().caster()), points.start(), points.end());
            particles(
               world,
               lease.spell(),
               lease.phase() == 1 ? SpellVisuals.Stage.CHARGE : SpellVisuals.Stage.LINK,
               particleStart,
               points.end(),
               lease.phase() == 1 ? (double)(5.0F + lease.progress() * 2.0F) : 6.0,
               lease.phase() == 2 && (BeamRenderer.firstPerson(world.getEntity(e.getKey().caster())) && SpellVisibilityConfig.clear(lease.spell())) ? (lease.spell() == Spell.DOOM_BEAM ? 0.6 : 0.3) : 1.0
            );
         }
      }
   }

   static void visual(ClientLevel l, Spell spell, SpellVisuals.Stage stage, Vec3 p, Vec3 end, double size) {
      if (stage == SpellVisuals.Stage.RITUAL_TARGET || stage == SpellVisuals.Stage.IMPACT || stage == SpellVisuals.Stage.HIT) {
         particles(l, spell, stage, p, end, size);
      }
   }

   @SubscribeEvent
   public static void render(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_PARTICLES) {
         prune();
         if (world != null && !CASTERS.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            BufferSource buffers = mc.renderBuffers().bufferSource();
            Vec3 camera = event.getCamera().getPosition();
            float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);

            for (Entry<DoomBeamEffects.Key, DoomBeamEffects.Lease> e : CASTERS.entrySet()) {
               DoomBeamEffects.Lease lease = e.getValue();
               // Doom Beam has no rendered core, source sphere, beacon or plasma mesh.
               // Arcane Devastator retains its own existing renderer.
               if (lease.spell() == Spell.DOOM_BEAM) continue;
               Entity caster = world.getEntity(e.getKey().caster());
               DoomBeamEffects.Endpoints points = endpoints(caster, lease, partial);
               PoseStack pose = new PoseStack();
               Vec3 start = points.start();
               pose.translate(start.x - camera.x, start.y - camera.y, start.z - camera.z);
               if (lease.phase() == 1) {
                  RenderType type = RenderType.debugStructureQuads();
                  VertexConsumer v = buffers.getBuffer(type);
                  Matrix4f matrix = pose.last().pose();
                  double size = (0.45 + (double)lease.progress() * 0.8) * ((BeamRenderer.firstPerson(caster) && SpellVisibilityConfig.clear(lease.spell())) ? 0.38 : 0.8);

                  for (int lat = 0; lat < 16; lat++) {
                     for (int lon = 0; lon < 32; lon++) {
                        double a = (-Math.PI / 2) + (double)lat * Math.PI / 16.0;
                        double b = (double)lon * Math.PI / 16.0;
                        core(v, matrix, a, b, size);
                        core(v, matrix, a + (Math.PI / 16), b, size);
                        core(v, matrix, a + (Math.PI / 16), b + (Math.PI / 16), size);
                        core(v, matrix, a, b + (Math.PI / 16), size);
                     }
                  }

                  buffers.endBatch(type);
               } else {
                  Vec3 direction = points.end().subtract(start);
                  float length = (float)direction.length();
                  if (!(length < 0.05F)) {
                     pose.mulPose(new Quaternionf().rotationTo(new Vector3f(0.0F, 0.0F, 1.0F), direction.normalize().toVector3f()));
                     boolean arcane = lease.spell() == Spell.ARCANE_DEVASTATOR;
                     float near = SpellVisibilityConfig.clear(lease.spell()) ? BeamRenderer.near(caster, start, points.end()) : 0;
                     if (!(length <= near)) {
                        float localScale = (BeamRenderer.firstPerson(caster) && SpellVisibilityConfig.clear(lease.spell())) ? 0.35F : 1.0F;
                        float scroll = -((float)world.getGameTime() + partial) * 0.06F;
                        RenderType core = RenderType.entityTranslucentCull(SpellRenderingHelper.BEACON);
                        VertexConsumer v = buffers.getBuffer(core);
                        BeamRenderer.square(
                           pose, v, 0.42F * localScale, near, length, arcane ? 240 : 210, arcane ? 200 : 255, arcane ? 255 : 175, 255, scroll, 0.16666667F
                        );
                        BeamRenderer.square(
                           pose, v, 1.1F * localScale, near, length, arcane ? 170 : 55, arcane ? 70 : 255, arcane ? 255 : 8, 165, scroll * 0.7F, 0.125F
                        );
                        buffers.endBatch(core);
                        RenderType corona = RenderType.entityTranslucentCull(PlasmaAssets.PLASMA);
                        VertexConsumer glow = buffers.getBuffer(corona);
                        BeamRenderer.square(
                           pose,
                           glow,
                           (arcane ? 1.8F : 2.9F) * localScale,
                           Math.max(near, 0.6F),
                           length,
                           arcane ? 150 : 40,
                           arcane ? 65 : 255,
                           arcane ? 255 : 3,
                           95,
                           scroll,
                           0.2F
                        );
                        buffers.endBatch(corona);
                     }
                  }
               }
            }
         }
      }
   }

   private static void core(VertexConsumer v, Matrix4f m, double lat, double lon, double size) {
      double r = Math.cos(lat) * size;
      v.addVertex(m, (float)(Math.cos(lon) * r), (float)(Math.sin(lat) * size), (float)(Math.sin(lon) * r)).setColor(2, 1, 1, 255);
   }

   private static DustParticleOptions dust(float r, float g, float b, float size) {
      return new DustParticleOptions(new Vector3f(r, g, b), size);
   }

   static boolean green(double progress) {
      return progress >= 0.5;
   }

   private static void particles(ClientLevel l, Spell spell, SpellVisuals.Stage stage, Vec3 p, Vec3 end, double size) {
      particles(l, spell, stage, p, end, size, 1.0);
   }

   private static void particles(ClientLevel l, Spell spell, SpellVisuals.Stage stage, Vec3 p, Vec3 end, double size, double presentationScale) {
        try (var visibilityScope = FirstPersonEffects.scope(spell)) {
      if (spell == Spell.DOOM_BEAM) {
         doomParticles(l, stage, p, end, size, presentationScale);
         return;
      }
      boolean arcane = spell == Spell.ARCANE_DEVASTATOR;
      ParticleOptions green = arcane ? PURPLE : GREEN;
      ParticleOptions white = arcane ? ARCANE_WHITE : WHITE;
      double time = (double)l.getGameTime() * 0.12;
      Vec3 axis = end.subtract(p).normalize();
      if (axis.lengthSqr() < 0.01) {
         axis = new Vec3(0.0, 0.0, 1.0);
      }

      Vec3 side = axis.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
      if (side.lengthSqr() < 0.01) {
         side = new Vec3(1.0, 0.0, 0.0);
      }

      Vec3 up = side.cross(axis).normalize();
      if (stage == SpellVisuals.Stage.RITUAL_TARGET) {
         for (int ring = 0; ring < 2; ring++) {
            for (int i = 0; i < 100; i++) {
               double a = (double)i * Math.PI / 50.0;
               double r = size - (double)ring * 0.25;
               NativeSpellEffects.point(l, green, p.add(Math.cos(a) * r, 0.03, Math.sin(a) * r), Vec3.ZERO, 12, true);
            }
         }
      } else if (stage == SpellVisuals.Stage.CHARGE) {
         double progress = Math.clamp((size - 5.0) / 2.0, 0.0, 1.0);
         double radius = 0.45 + progress * 0.8;
         boolean charged = green(progress);
         ParticleOptions rim = charged ? green : GOLD;

         for (int i = 0; i < 144; i++) {
            double a = (double)i * Math.PI / 72.0 + time;
            Vec3 off = side.scale(Math.cos(a) * radius).add(up.scale(Math.sin(a) * radius));
            point(l, rim, p.add(off), Vec3.ZERO);
         }

         for (int arm = 0; arm < 5; arm++) {
            for (int i = 0; i < 36; i++) {
               double t = (double)i / 36.0;
               double a = (double)arm * Math.PI * 2.0 / 5.0 + time + t * 9.0;
               double r = radius + (1.0 - t) * 4.0;
               Vec3 off = side.scale(Math.cos(a) * r).add(up.scale(Math.sin(a) * r)).add(axis.scale((1.0 - t) * 2.0));
               if (!arcane && charged && i % 3 == 0) {
                  NativeSpellEffects.tinted(l, i % 2 == 0 ? ParticleTypes.END_ROD : ParticleTypes.END_ROD, p.add(off), off.scale(-0.08), 14, 4849418, 0.65F);
               } else {
                  point(l, arcane && i % 3 == 0 ? YELLOW : (charged && i % 5 == 0 ? white : rim), p.add(off), off.scale(-0.08));
               }
            }
         }
      } else {
         if (stage == SpellVisuals.Stage.LINK) {
            int n = (int)Math.clamp(p.distanceTo(end) * 2.0, 24.0, 128.0);

            for (int ix = 0; ix <= n; ix++) {
               double t = (double)ix / (double)n;
               double a = t * 48.0 - time * 2.0;
               Vec3 q = p.lerp(end, t);
               if (ix % 4 == 0) {
                  NativeSpellEffects.tinted(
                     l,
                     ParticleTypes.END_ROD,
                     q,
                     axis.scale(0.12),
                     8,
                     arcane ? (ix % 8 == 0 ? 16767798 : 12552959) : 10157906,
                     (float)(0.7 * presentationScale)
                  );
               }

               for (int arm = 0; arm < 2; arm++) {
                  double angle = a + (double)arm * Math.PI;
                  double r = (0.65 + 0.25 * Math.sin(t * 16.0 + time)) * presentationScale;
                  Vec3 off = side.scale(Math.cos(angle) * r).add(up.scale(Math.sin(angle) * r));
                  NativeSpellEffects.tinted(
                     l,
                     ix % 3 == 0 ? ParticleTypes.END_ROD : ParticleTypes.END_ROD,
                     q.add(off),
                     off.scale(0.015),
                     6,
                     arcane ? (arm == 0 ? 16765230 : 10243583) : 3538693,
                     (float)(0.65 * presentationScale)
                  );
               }
            }

            for (int branch = 0; branch < 4; branch++) {
               Vec3 previous = p;

               for (int ix = 1; ix <= 36; ix++) {
                  double t = (double)ix / 36.0;
                  double a = (double)branch * Math.PI / 2.0 + t * 17.0 + time;
                  double r = (1.3 + 0.65 * Math.sin((double)ix * 2.7 + time * 3.0 + (double)branch)) * presentationScale;
                  Vec3 q = p.lerp(end, t).add(side.scale(Math.cos(a) * r)).add(up.scale(Math.sin(a) * r));

                  for (int j = 0; j < 2; j++) {
                     point(l, arcane && branch % 2 == 0 ? YELLOW : (j == 0 ? white : green), previous.lerp(q, (double)j / 2.0), Vec3.ZERO);
                  }

                  previous = q;
               }
            }

            if (presentationScale >= 0.99) {
               for (int ring = 0; ring < 3; ring++) {
                  for (int ix = 0; ix < 72; ix++) {
                     double a = (double)ix * Math.PI / 36.0 + time;
                     double r = 1.4 + (double)ring * 0.65;
                     Vec3 off = side.scale(Math.cos(a) * r).add(up.scale(Math.sin(a) * r));
                     point(l, arcane && ring % 2 == 0 ? YELLOW : green, p.add(axis.scale(0.15 + (double)ring * 0.25)).add(off), off.scale(0.025));
                  }
               }
            }
         } else if (stage == SpellVisuals.Stage.IMPACT || stage == SpellVisuals.Stage.HIT) {
            for (int ix = 0; ix < 160; ix++) {
               double a = (double)ix * 2.39996;
               double y = 1.0 - 2.0 * ((double)ix + 0.5) / 160.0;
               double r = Math.sqrt(1.0 - y * y);
               Vec3 off = new Vec3(Math.cos(a) * r, y, Math.sin(a) * r);
               point(l, arcane && ix % 3 == 0 ? YELLOW : (ix % 4 == 0 ? white : green), p.add(off.scale(1.5)), off.scale(0.25));
            }

            point(l, ParticleTypes.FLASH, p, Vec3.ZERO);
         }
      }

        }
    }

   /** Emerald plasma made entirely of vanilla particles and neutral Iron's sparkles. No solid centre. */
   private static void doomParticles(ClientLevel l, SpellVisuals.Stage stage, Vec3 p, Vec3 end, double size, double presentationScale) {
      double time = l.getGameTime();
      Vec3 axis = end.subtract(p).normalize();
      if (axis.lengthSqr() < .01) axis = impactAxis(p);
      Vec3 side = axis.cross(new Vec3(0, 1, 0)).normalize();
      if (side.lengthSqr() < .01) side = new Vec3(1, 0, 0);
      Vec3 up = side.cross(axis).normalize();
      ParticleOptions sparkle = ParticleRegistry.FALLING_SPARKLE_PARTICLE.get();
      if (stage == SpellVisuals.Stage.CHARGE) {
         double progress = Math.clamp((size - 5) / 2, 0, 1);
         // Four open focusing talons wind inward. They never close into a filled orb.
         double radius = .8 + progress * .65;
         for (int arm = 0; arm < 4; arm++) {
            for (int step = 0; step < 17; step++) {
               double t = step / 16.0;
               double angle = arm * Math.PI / 2 + time * .075 + t * 2.5;
               double r = radius * (1 - t * .83);
               Vec3 radial = side.scale(Math.cos(angle) * r).add(up.scale(Math.sin(angle) * r));
               Vec3 q = p.add(radial).add(axis.scale(-1.4 + t * 1.65));
               Vec3 velocity = radial.scale(-.10).add(axis.scale(.07 + progress * .08));
               doomParticle(l, step % 3 == 0 ? ParticleTypes.END_ROD : sparkle,
                  q, velocity, 8, step % 4 == 0 ? 0xe8ffd4 : 0x5bff82, step % 3 == 0 ? .55f : .85f);
            }
         }
         // Sparks shoot forward from a small open aperture; no black ball is drawn.
         for (int i = 0; i < 10; i++) {
            double angle = i * 2.399963 + time * .1;
            Vec3 off = side.scale(Math.cos(angle) * .18).add(up.scale(Math.sin(angle) * .18));
            doomParticle(l, ParticleTypes.ELECTRIC_SPARK, p.add(off).add(axis.scale(.4)),
               axis.scale(.18 + progress * .2), 7, 0xd4ffab, .6f);
         }
      } else if (stage == SpellVisuals.Stage.LINK) {
         double length = p.distanceTo(end);
         if (length < .15) return;
         int samples = (int)Math.clamp(Math.ceil(length * 5), 20, 240);
         // Two interlaced luminous streams provide the continuous body. Emit these before
         // accents so reduced particle budgets preserve the ray's shape first.
         for (int i = 0; i <= samples; i++) {
            double t = i / (double)samples;
            Vec3 centre = p.lerp(end, t);
            double angle = t * length * 1.6 - time * .45;
            double r = (.12 + .035 * Math.sin(time * .3 + t * 18)) * presentationScale;
            Vec3 off = side.scale(Math.cos(angle) * r).add(up.scale(Math.sin(angle) * r));
            doomParticle(l, sparkle, centre.add(off), axis.scale(.12), 5,
               i % 4 == 0 ? 0xe8ffd8 : 0x45ef72, (float)(1.05 * presentationScale));
            doomParticle(l, sparkle, centre.subtract(off), axis.scale(.16), 5,
               i % 3 == 0 ? 0xe4ffd2 : 0x83ff9c, (float)(.78 * presentationScale));
         }
         // Wider, broken double helices carry the green plasma wake, rather than an
         // opaque cylinder. Gaps and opposing velocities make the flow legible.
         for (int i = 0; i <= samples; i += 2) {
            double t = i / (double)samples;
            double angle = t * length * 1.15 - time * .24;
            double r = (.58 + .12 * Math.sin(t * 20 - time * .18)) * presentationScale;
            for (int arm = 0; arm < 2; arm++) {
               double a = angle + arm * Math.PI;
               Vec3 off = side.scale(Math.cos(a) * r).add(up.scale(Math.sin(a) * r));
               doomParticle(l, i % 6 == 0 ? ParticleTypes.ELECTRIC_SPARK : sparkle,
                  p.lerp(end, t).add(off), axis.scale(.22).add(off.scale(.025)), 6,
                  arm == 0 ? 0x32cf6b : 0xb6ff9a, (float)(.75 * presentationScale));
            }
         }
         // Three thin travelling compression fronts break up the ray into rushing pulses.
         for (int pulse = 0; pulse < 3; pulse++) {
            double t = ((time * .035 + pulse / 3.0) % 1.0);
            Vec3 centre = p.lerp(end, t);
            for (int i = 0; i < 14; i++) {
               double a = i * Math.PI / 7 + time * .1;
               Vec3 off = side.scale(Math.cos(a) * .78 * presentationScale)
                  .add(up.scale(Math.sin(a) * .78 * presentationScale));
               doomParticle(l, ParticleTypes.END_ROD, centre.add(off), axis.scale(.4),
                  4, 0xd0ffb9, (float)(.55 * presentationScale));
            }
         }
      } else if (stage == SpellVisuals.Stage.RITUAL_TARGET) {
         // A sparse segmented ground tell, not a second generic magic-circle effect.
         for (int arm = 0; arm < 8; arm++) {
            double a = arm * Math.PI / 4 + time * .006;
            for (int i = 0; i < 6; i++) {
               double r = Math.max(.8, size) * (.58 + i * .055);
               Vec3 off = new Vec3(Math.cos(a) * r, .06, Math.sin(a) * r);
               doomParticle(l, sparkle, p.add(off), new Vec3(0, .025, 0),
                  9, i == 5 ? 0xcaffaf : 0x3dcc70, .65f);
            }
         }
      } else if (stage == SpellVisuals.Stage.IMPACT || stage == SpellVisuals.Stage.HIT) {
         double radius = Math.clamp(size * .42, .8, 2.1);
         // A white-hot contact star fractures into radial emerald spokes and backward
         // exhaust. The short burst stays at the hit location, never at the caster's face.
         for (int arm = 0; arm < 12; arm++) {
            double a = arm * Math.PI / 6 + time * .025;
            Vec3 radial = side.scale(Math.cos(a)).add(up.scale(Math.sin(a)));
            for (int step = 0; step < 8; step++) {
               double t = step / 7.0;
               Vec3 off = radial.scale(radius * t).subtract(axis.scale(.1 + t * .6));
               doomParticle(l, step < 2 ? ParticleTypes.END_ROD : sparkle, p.add(off),
                  radial.scale(.15 + t * .16).subtract(axis.scale(.10)), 8 + step % 3,
                  step < 2 ? 0xf2ffe4 : arm % 2 == 0 ? 0x54f584 : 0xb8ffa0, .9f);
            }
         }
         for (int i = 0; i < 24; i++) {
            double a = i * 2.399963;
            Vec3 off = side.scale(Math.cos(a) * radius * .45).add(up.scale(Math.sin(a) * radius * .45));
            doomParticle(l, ParticleTypes.ELECTRIC_SPARK, p.add(off),
               off.normalize().scale(.4).subtract(axis.scale(.25)), 7, 0xbfff98, .9f);
         }
         NativeSpellEffects.point(l, ParticleTypes.FLASH, p, Vec3.ZERO, 2, false);
      }
   }

   private static void doomParticle(ClientLevel level, ParticleOptions type, Vec3 p, Vec3 velocity,
      int life, int color, float scale) {
      // Falling Sparkle has neutral, fullbright frames, unlike the baked-yellow wisp.
      float carrierScale = type == ParticleRegistry.FALLING_SPARKLE_PARTICLE.get() ? 1.8f : 1f;
      NativeSpellEffects.tinted(level, type, p, velocity, life, color, scale * carrierScale);
   }

   private static Vec3 impactAxis(Vec3 impact) {
      Vec3 result = new Vec3(0, 0, 1);
      double nearest = 16;
      if (world == null) return result;
      for (Entry<DoomBeamEffects.Key, DoomBeamEffects.Lease> entry : CASTERS.entrySet()) {
         if (entry.getValue().spell() != Spell.DOOM_BEAM || entry.getValue().phase() != 2) continue;
         Entity caster = world.getEntity(entry.getKey().caster());
         if (caster == null) continue;
         Endpoints points = endpoints(caster, entry.getValue(), 1);
         double distance = points.end().distanceToSqr(impact);
         if (distance < nearest) {
            nearest = distance;
            Vec3 candidate = points.end().subtract(points.start()).normalize();
            if (candidate.lengthSqr() > .01) result = candidate;
         }
      }
      return result;
   }

   private static void point(ClientLevel l, ParticleOptions o, Vec3 p, Vec3 v) {
      NativeSpellEffects.point(l, o, p, v, 5, false);
   }

   private static record Endpoints(Vec3 start, Vec3 end) {
   }

   private static record Key(int caster, int spell) {
   }

   private static record Lease(Spell spell, int phase, float progress, float length, long expires) {
   }
}

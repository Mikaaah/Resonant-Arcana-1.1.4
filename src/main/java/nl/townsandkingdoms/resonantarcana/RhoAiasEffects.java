package nl.townsandkingdoms.resonantarcana;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import org.joml.Matrix4f;

@EventBusSubscriber(
   modid = "resonant_arcana",
   value = {Dist.CLIENT}
)
final class RhoAiasEffects {
   private static final int SEGMENTS = 196;
   private static final double[] X = new double[197];
   private static final double[] Y = new double[197];
   private static final double[] R = new double[197];
   private static final Map<Integer, RhoAiasEffects.Ward> WARDS;
   private static ClientLevel world;

   static void handle(WandNetwork.RhoFocus msg) {
        try (var visibilityScope = FirstPersonEffects.scope(Spell.RHO_AIAS)) {
      Minecraft mc = Minecraft.getInstance();
      ClientLevel level = mc.level;
      if (level != world) {
         WARDS.clear();
         world = level;
      }

      if (level != null && mc.player != null && msg.valid()) {
         WandNetwork.Visual v = msg.visual();
         Vec3 p = new Vec3(v.x(), v.y(), v.z());
         Vec3 n = new Vec3(v.endX(), v.endY(), v.endZ()).subtract(p).normalize();
         if (!(mc.player.position().distanceToSqr(p) > 9216.0)) {
            SpellVisuals.Stage stage = SpellVisuals.Stage.values()[v.stage()];
            if (stage == SpellVisuals.Stage.RITUAL_END || msg.layers() <= 0.0F) {
               WARDS.remove(msg.caster());
               if (stage == SpellVisuals.Stage.RITUAL_END) {
                  return;
               }
            }

            if (stage == SpellVisuals.Stage.AREA && msg.layers() > 0.0F) {
               RhoAiasEffects.Ward s = WARDS.get(msg.caster());
               if (s == null) {
                  if (WARDS.size() >= 32) {
                     return;
                  }

                  s = new RhoAiasEffects.Ward(p, n, level.getGameTime());
                  WARDS.put(msg.caster(), s);
               }

               s.oldCenter = s.center;
               s.oldNormal = s.normal;
               s.center = p;
               s.normal = n;
               s.layers = msg.layers();
               s.seen = level.getGameTime();
               if (level.getGameTime() % 4L == 0L) {
                  Vec3 side = n.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
                  if (side.lengthSqr() < 0.01) {
                     side = new Vec3(1.0, 0.0, 0.0);
                  }

                  Vec3 up = side.cross(n).normalize();

                  for (int layer = 0; layer <= 3; layer++) {
                     for (int i = 0; i < 36; i++) {
                        double a = (double)i * Math.PI / 18.0 + (double)level.getGameTime() * 0.025;
                        double r = layer == 0 ? RhoAias.outline(a) : RhoAias.shieldRadius(layer);
                        Vec3 offset = side.scale(Math.cos(a) * r).add(up.scale(Math.sin(a) * r));
                        NativeSpellEffects.tinted(
                           level,
                           ParticleTypes.END_ROD,
                           p.add(n.scale((double)layer * 1.15)).add(offset),
                           offset.normalize().scale(0.035),
                           22,
                           i % 3 == 0 ? 16758522 : 10179327,
                           1.2F
                        );
                        // Small vanilla glints accompany the existing rose geometry.
                        if (i % 9 == 0) {
                           NativeSpellEffects.point(level, ParticleTypes.ENCHANTED_HIT,
                              p.add(n.scale((double)layer * 1.15)).add(offset),
                              up.scale(0.025).add(n.scale(0.012)), 12, false);
                        }
                     }
                  }
               }
            } else {
               if (stage == SpellVisuals.Stage.HIT || stage == SpellVisuals.Stage.IMPACT) {
                  RhoAiasEffects.Ward sx = WARDS.get(msg.caster());
                  if (sx != null) {
                     sx.pulse = level.getGameTime();
                  }

                  burst(level, p, n, stage == SpellVisuals.Stage.IMPACT);
               }
            }
         }
      }

        }
    }

   static void visual(ClientLevel level, SpellVisuals.Stage stage, Vec3 p, Vec3 end, double size) {
      Vec3 n = end.subtract(p).normalize();
      Vec3 side = n.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
      if (side.lengthSqr() < 0.01) {
         side = new Vec3(1.0, 0.0, 0.0);
      }

      Vec3 up = side.cross(n).normalize();
      if (stage == SpellVisuals.Stage.CHARGE || stage == SpellVisuals.Stage.CAST) {
         double time = (double)level.getGameTime() * 0.06;

         for (int petal = 0; petal < 7; petal++) {
            for (int i = 0; i < 7; i++) {
               double a = (double)petal * Math.PI * 2.0 / 7.0 + time;
               double t = (double)i / 6.0;
               double r = (0.2 + t * 0.65) * Math.min(1.0, size);
               Vec3 offset = side.scale(Math.cos(a) * r).add(up.scale(Math.sin(a) * r));
               NativeSpellEffects.tinted(level, ParticleTypes.END_ROD, p.add(offset), offset.scale(-0.025), 10, petal % 2 == 0 ? 16746212 : 10123519, 0.4F);
            }
         }
      }
   }

   private static void burst(ClientLevel l, Vec3 p, Vec3 n, boolean fracture) {
      Vec3 side = n.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
      if (side.lengthSqr() < 0.01) {
         side = new Vec3(1.0, 0.0, 0.0);
      }

      Vec3 up = side.cross(n).normalize();

      for (int i = 0; i < (fracture ? 144 : 88); i++) {
         double a = (double)i * 2.399963;
         double r = 0.25 + (double)(i % 7) * 0.035;
         Vec3 v = side.scale(Math.cos(a) * r).add(up.scale(Math.sin(a) * r));
         NativeSpellEffects.tinted(
            l,
            ParticleTypes.END_ROD,
            p.add(v.scale(0.25)),
            v.add(n.scale(0.025)),
            fracture ? 24 : 18,
            i % 4 == 0 ? 15260159 : (i % 2 == 0 ? 16737492 : 10845183),
            fracture ? 0.65F : 0.4F
         );
      }
   }

   @SubscribeEvent
   public static void render(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_PARTICLES) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != world) {
            WARDS.clear();
            world = mc.level;
         }

         if (world != null) {
            WARDS.entrySet().removeIf(e -> world.getGameTime() - e.getValue().seen > 8L || world.getEntity(e.getKey()) == null);
            if (!WARDS.isEmpty()) {
               BufferSource buffers = mc.renderBuffers().bufferSource();
               RenderType type = RenderType.debugStructureQuads();
               VertexConsumer v = buffers.getBuffer(type);
               Vec3 camera = event.getCamera().getPosition();
               PoseStack pose = new PoseStack();
               double partial = (double)event.getPartialTick().getGameTimeDeltaPartialTick(false);
               double time = (double)world.getGameTime() + partial;

               for (Entry<Integer, RhoAiasEffects.Ward> entry : WARDS.entrySet()) {
                  RhoAiasEffects.Ward s = entry.getValue();
                  double clarity = mc.options.getCameraType().isFirstPerson() && mc.getCameraEntity() != null && mc.getCameraEntity().getId() == entry.getKey()
                     ? 0.18
                     : 1.0;
                  double t = Math.clamp((time - (double)s.seen) / 2.0, 0.0, 1.0);
                  double age = time - (double)s.born;
                  double pulse = Math.max(0.0, 1.0 - (time - (double)s.pulse) / 9.0);
                  Vec3 p = s.oldCenter.lerp(s.center, t);
                  Vec3 n = s.oldNormal.lerp(s.normal, t).normalize();
                  if (n.lengthSqr() < 0.01) {
                     n = s.normal;
                  }

                  Vec3 side = n.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
                  if (side.lengthSqr() < 0.01) {
                     side = new Vec3(1.0, 0.0, 0.0);
                  }

                  Vec3 up = side.cross(n).normalize();
                  pose.pushPose();
                  pose.translate(p.x - camera.x, p.y - camera.y, p.z - camera.z);
                  Matrix4f m = pose.last().pose();
                  double unfold = ease(age / 9.0);

                  for (int i = 0; i < 196; i++) {
                     int petal = (i + 14) / 28 % 7;

                     for (int band = 0; band < 10; band++) {
                        double a = (double)band / 10.0;
                        double b = (double)(band + 1) / 10.0;
                        int rgb = blend(16754159, petal % 2 == 0 ? 11021279 : 7682530, (a + b) * 0.5);
                        strip(
                           v,
                           m,
                           side,
                           up,
                           n,
                           i,
                           a * unfold,
                           b * unfold,
                           -0.65 * a * a,
                           -0.65 * b * b,
                           rgb,
                           (int)((65.0 + 24.0 * Math.sin(b * Math.PI) + pulse * 35.0) * clarity)
                        );
                     }

                     for (int glow = 0; glow < 4; glow++) {
                        strip(
                           v,
                           m,
                           side,
                           up,
                           n,
                           i,
                           (1.0 + (double)glow * 0.014) * unfold,
                           (1.006 + (double)glow * 0.02) * unfold,
                           -0.65,
                           -0.65,
                           glow == 0 ? 16761338 : 9718015,
                           (int)((double)((glow == 0 ? 220 : 70) / (glow + 1)) * clarity)
                        );
                     }
                  }

                  for (int petal = 0; petal < 7; petal++) {
                     double angle = (double)petal * Math.PI * 2.0 / 7.0;
                     line(v, m, side, up, n, angle, 0.45 * unfold, 5.82 * unfold, -0.5, 0.012, 16753909, (int)(150.0 * clarity));

                     for (int vein = 0; vein < 6; vein++) {
                        double radius = (1.7 + (double)vein * 0.57) * unfold;
                        double spread = 0.17 - (double)vein * 0.017;
                        line(v, m, side, up, n, angle - spread, radius, radius + 0.45 * unfold, -0.51, 0.018, 15492590, (int)(95.0 * clarity));
                        line(v, m, side, up, n, angle + spread, radius, radius + 0.45 * unfold, -0.51, 0.018, 15492590, (int)(95.0 * clarity));
                     }
                  }

                  ring(v, m, side, up, n, 0.36 * unfold, 0.13, -0.7, 16766972, (int)(230.0 * clarity), 0.0);
                  ring(v, m, side, up, n, 0.78 * unfold, 0.06, -0.7, 16747241, (int)(170.0 * clarity), 0.0);

                  for (int layer = 1; layer <= 3; layer++) {
                     double formed = ease((age - (double)(5 + layer * 5)) / 8.0);
                     if (!(formed <= 0.0)) {
                        double radius = RhoAias.shieldRadius(layer) * formed;
                        double z = (double)layer * 1.15;
                        int face = (int)((13.0 + pulse * 24.0) * clarity * formed);

                        for (int i = 0; i < 112; i++) {
                           double a = (double)i * Math.PI / 56.0;
                           double b = (double)(i + 1) * Math.PI / 56.0;
                           quad(
                              v,
                              m,
                              side,
                              up,
                              n,
                              0.0,
                              0.0,
                              z,
                              Math.cos(a) * radius,
                              Math.sin(a) * radius,
                              z,
                              Math.cos(b) * radius,
                              Math.sin(b) * radius,
                              z,
                              0.0,
                              0.0,
                              z,
                              8865238,
                              face
                           );
                           double outer = radius * 0.94;
                           double inner = outer - (i % 4 == 0 ? 0.48 : 0.24) * formed;
                           quad(
                              v,
                              m,
                              side,
                              up,
                              n,
                              Math.cos(a) * inner,
                              Math.sin(a) * inner,
                              z + 0.012,
                              Math.cos(b) * inner,
                              Math.sin(b) * inner,
                              z + 0.012,
                              Math.cos(b) * outer,
                              Math.sin(b) * outer,
                              z + 0.012,
                              Math.cos(a) * outer,
                              Math.sin(a) * outer,
                              z + 0.012,
                              i % 4 == 0 ? 16363775 : 12020479,
                              (int)(65.0 * clarity * formed)
                           );
                        }

                        for (int halo = 3; halo >= 0; halo--) {
                           ring(
                              v,
                              m,
                              side,
                              up,
                              n,
                              radius + (double)halo * 0.023,
                              halo == 0 ? 0.026 : 0.055,
                              z,
                              halo == 0 ? 16767231 : 8734463,
                              (int)((double)(halo == 0 ? 230 : 32) * clarity * formed),
                              0.0
                           );
                        }

                        ring(v, m, side, up, n, radius * 0.89, 0.014, z + 0.02, 14916351, (int)(180.0 * clarity * formed), 0.0);
                        ring(v, m, side, up, n, radius * 0.65, 0.012, z + 0.03, 12225279, (int)(100.0 * clarity * formed), 0.0);
                        ring(v, m, side, up, n, radius * 0.33, 0.02, z + 0.04, 16230911, (int)(160.0 * clarity * formed), 0.0);
                        double spin = time * 0.0025 * (double)(layer % 2 == 0 ? -1 : 1) + (double)layer * 0.15;

                        for (int rune = 0; rune < 28; rune++) {
                           double a = (double)rune * Math.PI / 14.0 + spin;
                           line(
                              v,
                              m,
                              side,
                              up,
                              n,
                              a,
                              radius * 0.72,
                              radius * (rune % 4 == 0 ? 0.84 : 0.78),
                              z + 0.04,
                              0.016,
                              15776511,
                              (int)(170.0 * clarity * formed)
                           );
                        }

                        for (int petal = 0; petal < 7; petal++) {
                           double a = (double)petal * Math.PI * 2.0 / 7.0 + spin;
                           double b = a + (Math.PI * 4.0 / 7.0);
                           double ax = Math.cos(a) * radius * 0.33;
                           double ay = Math.sin(a) * radius * 0.33;
                           double bx = Math.cos(b) * radius * 0.33;
                           double by = Math.sin(b) * radius * 0.33;
                           stroke(v, m, side, up, n, ax, ay, bx, by, z + 0.045, 0.012, 16173055, (int)(150.0 * clarity * formed));
                        }
                     }
                  }

                  pose.popPose();
               }

               buffers.endBatch(type);
            }
         }
      }
   }

   private static double ease(double t) {
      t = Math.clamp(t, 0.0, 1.0);
      return 1.0 - Math.pow(1.0 - t, 3.0);
   }

   private static void ring(
      VertexConsumer v, Matrix4f m, Vec3 side, Vec3 up, Vec3 n, double radius, double width, double z, int rgb, int alpha, double rotation
   ) {
      for (int i = 0; i < 112; i++) {
         double a = (double)i * Math.PI / 56.0 + rotation;
         double b = (double)(i + 1) * Math.PI / 56.0 + rotation;
         quad(
            v,
            m,
            side,
            up,
            n,
            Math.cos(a) * (radius - width),
            Math.sin(a) * (radius - width),
            z,
            Math.cos(b) * (radius - width),
            Math.sin(b) * (radius - width),
            z,
            Math.cos(b) * (radius + width),
            Math.sin(b) * (radius + width),
            z,
            Math.cos(a) * (radius + width),
            Math.sin(a) * (radius + width),
            z,
            rgb,
            alpha
         );
      }
   }

   private static void line(
      VertexConsumer v, Matrix4f m, Vec3 side, Vec3 up, Vec3 n, double a, double from, double to, double z, double width, int rgb, int alpha
   ) {
      stroke(v, m, side, up, n, Math.cos(a) * from, Math.sin(a) * from, Math.cos(a) * to, Math.sin(a) * to, z, width, rgb, alpha);
   }

   private static void stroke(
      VertexConsumer v, Matrix4f m, Vec3 side, Vec3 up, Vec3 n, double ax, double ay, double bx, double by, double z, double width, int rgb, int alpha
   ) {
      double length = Math.hypot(bx - ax, by - ay);
      if (!(length < 1.0E-6)) {
         double dx = -(by - ay) / length * width;
         double dy = (bx - ax) / length * width;
         quad(v, m, side, up, n, ax + dx, ay + dy, z, bx + dx, by + dy, z, bx - dx, by - dy, z, ax - dx, ay - dy, z, rgb, alpha);
      }
   }

   private static int blend(int a, int b, double f) {
      return (int)((double)(a >> 16 & 0xFF) * (1.0 - f) + (double)(b >> 16 & 0xFF) * f) << 16
         | (int)((double)(a >> 8 & 0xFF) * (1.0 - f) + (double)(b >> 8 & 0xFF) * f) << 8
         | (int)((double)(a & 0xFF) * (1.0 - f) + (double)(b & 0xFF) * f);
   }

   private static void strip(VertexConsumer v, Matrix4f m, Vec3 side, Vec3 up, Vec3 n, int i, double a, double b, double za, double zb, int color, int alpha) {
      quad(
         v,
         m,
         side,
         up,
         n,
         X[i] * R[i] * a,
         Y[i] * R[i] * a,
         za,
         X[i + 1] * R[i + 1] * a,
         Y[i + 1] * R[i + 1] * a,
         za,
         X[i + 1] * R[i + 1] * b,
         Y[i + 1] * R[i + 1] * b,
         zb,
         X[i] * R[i] * b,
         Y[i] * R[i] * b,
         zb,
         color,
         alpha
      );
   }

   private static void quad(
      VertexConsumer v,
      Matrix4f m,
      Vec3 side,
      Vec3 up,
      Vec3 n,
      double x0,
      double y0,
      double z0,
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      double x3,
      double y3,
      double z3,
      int rgb,
      int alpha
   ) {
      vertex(v, m, side, up, n, x0, y0, z0, rgb, alpha);
      vertex(v, m, side, up, n, x1, y1, z1, rgb, alpha);
      vertex(v, m, side, up, n, x2, y2, z2, rgb, alpha);
      vertex(v, m, side, up, n, x3, y3, z3, rgb, alpha);
      vertex(v, m, side, up, n, x3, y3, z3, rgb, alpha);
      vertex(v, m, side, up, n, x2, y2, z2, rgb, alpha);
      vertex(v, m, side, up, n, x1, y1, z1, rgb, alpha);
      vertex(v, m, side, up, n, x0, y0, z0, rgb, alpha);
   }

   private static void vertex(VertexConsumer v, Matrix4f m, Vec3 side, Vec3 up, Vec3 n, double x, double y, double z, int rgb, int alpha) {
      v.addVertex(m, (float)(side.x * x + up.x * y + n.x * z), (float)(side.y * x + up.y * y + n.y * z), (float)(side.z * x + up.z * y + n.z * z))
         .setColor(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, Math.clamp((long)alpha, 0, 255));
   }

   private RhoAiasEffects() {
   }

   static {
      for (int i = 0; i <= 196; i++) {
         double a = (double)i * Math.PI * 2.0 / 196.0;
         X[i] = Math.cos(a);
         Y[i] = Math.sin(a);
         R[i] = RhoAias.outline(a);
      }

      WARDS = new LinkedHashMap<>();
   }

   private static final class Ward {
      Vec3 center;
      Vec3 oldCenter;
      Vec3 normal;
      Vec3 oldNormal;
      float layers;
      long seen;
      long born;
      long pulse = -100L;

      Ward(Vec3 p, Vec3 n, long tick) {
         this.center = this.oldCenter = p;
         this.normal = this.oldNormal = n;
         this.seen = this.born = tick;
      }
   }
}

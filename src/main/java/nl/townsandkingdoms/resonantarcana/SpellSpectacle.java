package nl.townsandkingdoms.resonantarcana;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

final class SpellSpectacle {
   private static final List<SpellSpectacle.Front> FRONTS = new ArrayList<>();
   private static final Map<String, Long> LAST = new HashMap<>();
   private static ClientLevel world;

   static void tick() {
      ClientLevel level = Minecraft.getInstance().level;
      if (level != world) {
         FRONTS.clear();
         LAST.clear();
         world = level;
      }

      if (level != null && !Minecraft.getInstance().isPaused()) {
         long now = level.getGameTime();
         FRONTS.removeIf(fx -> now - fx.born > 16L);
         if (now % 2L == 0L) {
            for (SpellSpectacle.Front f : FRONTS) {
               double age = (double)(now - f.born);
               double t = age / 16.0;
               double r = f.radius * (0.12 + 0.88 * t);
               double lift = Math.sin(t * Math.PI) * (f.god ? 1.3 : 0.4);
               int count = f.god ? 100 : 56;

               for (int i = 0; i < count; i++) {
                  double a = (double)i * Math.PI * 2.0 / (double)count;
                  Vec3 q = f.center.add(Math.cos(a) * r, 0.13 + lift, Math.sin(a) * r);
                  FirstPersonEffects.forSpell(f.spell, () -> spark(level, q, new Vec3(Math.cos(a) * 0.04, 0.025, Math.sin(a) * 0.04), f.spell.theme.color, 14, f.god ? 0.9F : 0.65F));
                  if (f.god && i % 4 == 0) {
                     FirstPersonEffects.forSpell(f.spell, () -> spark(level, q.add(0.0, 0.6, 0.0), new Vec3(0.0, 0.07, 0.0), 16378623, 16, 0.65F));
                  }
               }
            }

            if (now % 200L == 0L) {
               LAST.entrySet().removeIf(e -> now - e.getValue() > 40L);
            }
         }
      }
   }

   static void visual(ClientLevel l, Spell s, SpellVisuals.Stage stage, Vec3 p, Vec3 end, double size) {
      boolean god = GodSpells.handles(s);
      if (god) return;
      boolean newSpell = ExpansionSpells.handles(s) || s == Spell.ANGEL_SCREAM;
      if (stage == SpellVisuals.Stage.IMPACT || stage == SpellVisuals.Stage.CAST && newSpell) {
         String key = s.name() + ":" + Math.round(p.x / 3.0) + ":" + Math.round(p.z / 3.0);
         long now = l.getGameTime();
         if (now - LAST.getOrDefault(key, -100L) >= 8L && FRONTS.size() < 40) {
            LAST.put(key, now);
            FRONTS.add(new SpellSpectacle.Front(s, p, Math.clamp(size * (god ? 1.2 : 1.1), 1.2, god ? 26.0 : 10.0), now, god));
         }

         int count = god ? 160 : (newSpell ? 96 : 40);

         for (int i = 0; i < count; i++) {
            double a = (double)i * 2.399963;
            double y = 1.0 - 2.0 * ((double)i + 0.5) / (double)count;
            double r = Math.sqrt(1.0 - y * y);
            Vec3 v = new Vec3(Math.cos(a) * r, y * 0.8 + 0.25, Math.sin(a) * r);
            spark(
               l,
               p.add(v.scale(Math.min(3.0, size * 0.22))),
               v.scale(god ? 0.48 : 0.22),
               i % 5 == 0 ? 16772575 : s.theme.color,
               20 + i % 14,
               god ? 1.05F : 0.65F
            );
         }
      }

      if (god && stage == SpellVisuals.Stage.CHARGE) {
         godCharge(l, s, p, size, (double)l.getGameTime() * 0.065);
      }

      if (newSpell && stage == SpellVisuals.Stage.AREA) {
         double time = (double)l.getGameTime() * 0.09;
         int count = s == Spell.WHITEOUT_HUNT ? 180 : 64;

         for (int i = 0; i < count; i++) {
            double a = (double)i * 2.39996 + time;
            double r = Math.max(0.8, size) * (0.35 + 0.6 * ((double)(i % 17) / 17.0));
            double y = (double)(i % 13) / 13.0 * (s == Spell.WHITEOUT_HUNT ? 5.0 : 2.5);
            Vec3 q = p.add(Math.cos(a) * r, y, Math.sin(a) * r);
            switch (s) {
               case WHITEOUT_HUNT:
               case MIRROR_ICE:
                  NativeSpellEffects.point(l, ParticleTypes.SNOWFLAKE, q, new Vec3(-Math.sin(a) * 0.13, -0.04, Math.cos(a) * 0.13), 24, false);
                  break;
               case PRESSURE_BELL:
                  spark(l, q, new Vec3(0.0, 0.06, 0.0), 9235711, 24, 0.65F);
                  break;
               case ECHO_DECOY:
               case CHOIR_OF_THE_LOST:
                  if (i % 2 == 0) {
                     NativeSpellEffects.point(l, ParticleTypes.SCULK_SOUL, q, new Vec3(0.0, 0.04, 0.0), 20, false);
                  }
                  break;
               case STOLEN_SHADOW:
                  spark(l, q, new Vec3(-Math.cos(a) * 0.05, 0.02, -Math.sin(a) * 0.05), 10841599, 16, 0.8F);
                  break;
               case LODESTONE_SNARE:
               case STORM_RAIL:
                  spark(
                     l,
                     p.lerp(end, ((double)i + 0.5) / (double)count).add(Math.cos(a) * 0.55, 0.8 + Math.sin(a) * 0.55, 0.0),
                     Vec3.ZERO,
                     i % 2 == 0 ? 16728425 : 6074111,
                     14,
                     0.8F
                  );
                  break;
               case FOLDED_PASSAGE:
               case ORBIT_EXCHANGE:
                  Vec3 axis = end.subtract(p).normalize();
                  Vec3 side = axis.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
                  Vec3 q1 = p.add(side.scale(Math.cos(a) * 1.2)).add(0.0, 1.4 + Math.sin(a) * 1.5, 0.0);
                  spark(l, q1, axis.scale(0.08), 14066431, 18, 0.7F);
                  break;
               case CARAMEL_CATAPULT:
                  spark(l, p.add(Math.cos(a) * 1.2, 0.1 + Math.abs(Math.sin(a * 3.0)) * 0.5, Math.sin(a) * 1.2), new Vec3(0.0, 0.05, 0.0), 16762224, 22, 0.65F);
                  break;
               case OATHBREAKER_CHAINS:
                  spark(l, p.lerp(end, ((double)i + 0.5) / (double)count).add(0.0, 0.12 * Math.sin(a), 0.0), new Vec3(0.0, 0.05, 0.0), 16742204, 20, 0.7F);
                  break;
               default:
                  if (i % 3 == 0) {
                     spark(l, q, new Vec3(0.0, 0.025, 0.0), s.theme.color, 16, 0.55F);
                  }
            }
         }
      }
   }

   private static void godCharge(ClientLevel l, Spell s, Vec3 p, double size, double time) {
      double progress = Math.clamp((size - 5.0) / 3.0, 0.0, 1.0);
      double radius = 2.2 + progress * 4.0;
      Vec3 heart = p.add(0.0, 3.0 + progress * 2.0, 0.0);
      int rgb = s.theme.color;
      switch (s) {
         case THUNDER_OF_THE_HEAVENS:
            for (int arm = 0; arm < 8; arm++) {
               double a = (double)arm * Math.PI / 4.0 + time * 0.12;
               Vec3 last = heart;

               for (int ix = 1; ix <= 18; ix++) {
                  double t = (double)ix / 18.0;
                  Vec3 q = heart.add(Math.cos(a) * t * radius, Math.sin(t * 14.0 + (double)arm) * 0.3 + t * 3.0, Math.sin(a) * t * radius);
                  stroke(l, last, q, 4, 11629055);
                  last = q;
               }
            }
            break;
         case ARCANE_CREATION:
         case CONFECTION_APOCALYPSE:
            for (int star = 0; star < 7; star++) {
               double a = (double)star * Math.PI * 2.0 / 7.0 + time * 0.22;
               Vec3 center = heart.add(Math.cos(a) * radius, Math.sin(a * 2.0) * 1.5, Math.sin(a) * radius);

               for (int ray = 0; ray < 5; ray++) {
                  double b = (double)ray * Math.PI * 2.0 / 5.0 + time;
                  Vec3 q = center.add(Math.cos(b) * 0.8, Math.sin(b) * 0.8, 0.0);
                  stroke(l, center, q, 8, s == Spell.ARCANE_CREATION ? 12618495 : (ray % 2 == 0 ? 16748249 : 10158044));
               }
            }
            break;
         case CRIMSON_APOTHEOSIS:
         case EVENTIDE_HORIZON:
            for (int ix = 0; ix < 256; ix++) {
               double a = (double)ix * Math.PI / 128.0 + time * 0.15;
               double r = radius * (0.7 + 0.2 * Math.sin(a * 7.0 + time));
               Vec3 q = s == Spell.CRIMSON_APOTHEOSIS
                  ? heart.add(Math.cos(a) * r, Math.sin(a) * r, 0.0)
                  : heart.add(Math.cos(a) * r * 1.3, Math.sin(a * 2.0) * 0.6, Math.sin(a) * r);
               spark(l, q, q.subtract(heart).scale(-0.03), rgb, 20, 1.0F);
            }
            break;
         case SOLAR_SUPERNOVA:
         case TREMORZILLA_CATACLYSM:
            int c = s == Spell.SOLAR_SUPERNOVA ? 16761671 : 11623167;

            for (int ring = 0; ring < 4; ring++) {
               for (int ix = 0; ix < 76; ix++) {
                  double a = (double)ix * Math.PI * 2.0 / 76.0 + time * (double)(ring % 2 == 0 ? 1 : -1);
                  double r = radius * (0.5 + (double)ring * 0.18);
                  Vec3 q = heart.add(Math.cos(a) * r, Math.sin(a) * (ring == 0 ? r : 0.3) + ((double)ring - 1.5) * 0.8, Math.sin(a) * r);
                  spark(l, q, q.subtract(heart).normalize().scale(0.03), ix % 5 == 0 ? 16771317 : c, 18, 1.0F);
               }
            }
            break;
         case ABSOLUTE_GLACIATION:
         case YETI_WORLD_HEART:
            for (int arm = 0; arm < 8; arm++) {
               double a = (double)arm * Math.PI / 4.0;
               Vec3 base = p.add(Math.cos(a) * radius * 0.7, 0.2, Math.sin(a) * radius * 0.7);
               Vec3 tip = base.add(Math.cos(a) * 1.2, 4.0 + progress * 4.0, Math.sin(a) * 1.2);
               stroke(l, base, tip, 26, 12185087);
               stroke(l, base.add(-0.7, 0.0, 0.0), tip, 20, 8108799);

               for (int ix = 0; ix < 8; ix++) {
                  NativeSpellEffects.point(
                     l,
                     ParticleTypes.SNOWFLAKE,
                     tip.add(Math.sin((double)ix + time), (double)(-ix) * 0.25, Math.cos((double)ix + time)),
                     new Vec3(0.0, -0.06, 0.0),
                     24,
                     false
                  );
               }
            }
            break;
         case VERDANT_AWAKENING:
         case PRIMORDIAL_SOVEREIGN:
            for (int arm = 0; arm < 7; arm++) {
               for (int ix = 0; ix < 40; ix++) {
                  double t = (double)ix / 39.0;
                  double a = (double)arm * Math.PI * 2.0 / 7.0 + t * 5.0 + time * 0.2;
                  double r = radius * (1.0 - t * 0.65);
                  Vec3 q = p.add(Math.cos(a) * r, t * 9.0, Math.sin(a) * r);
                  spark(l, q, new Vec3(0.0, 0.05, 0.0), s == Spell.VERDANT_AWAKENING ? 10286197 : 16759913, 24, 0.9F);
               }
            }
            break;
         case HEAVENS_COVENANT:
         case DRAGON_PRIMORDIAL_INFERNO:
            boolean holy = s == Spell.HEAVENS_COVENANT;

            for (int wing : new int[]{-1, 1}) {
               for (int feather = 0; feather < 10; feather++) {
                  Vec3 from = heart.add((double)wing * 0.5, 0.0, 0.0);
                  Vec3 tip = heart.add(
                     (double)wing * (3.0 + (double)feather * 0.42) * (0.6 + progress * 0.4),
                     3.0 - (double)feather * 0.2,
                     Math.sin(time * 0.4) * (double)feather * 0.16
                  );
                  stroke(l, from, tip, 25, holy ? 16771241 : 16743481);
                  if (!holy) {
                     NativeSpellEffects.point(l, ParticleTypes.FLAME, tip, new Vec3(0.0, 0.04, 0.0), 20, false);
                  }
               }
            }
            break;
         case SOUL_ASCENDANCY:
         case FORSAKEN_ECLIPSE:
            for (int soul = 0; soul < 9; soul++) {
               double a = (double)soul * Math.PI * 2.0 / 9.0 + time * 0.4;
               Vec3 q = heart.add(Math.cos(a) * radius, Math.sin(a * 2.0 + time) * 2.0, Math.sin(a) * radius);

               for (int ix = 0; ix < 26; ix++) {
                  double t = (double)ix / 25.0;
                  spark(
                     l,
                     q.add(Math.sin(t * 5.0 + time) * t, -t * 3.0, Math.cos(t * 5.0 + time) * t),
                     new Vec3(0.0, 0.07, 0.0),
                     s == Spell.SOUL_ASCENDANCY ? 7596021 : 10711263,
                     22,
                     0.9F
                  );
               }
            }
            break;
         case PLANETARY_POLARITY:
            for (int sign : new int[]{-1, 1}) {
               for (int ix = 0; ix < 150; ix++) {
                  double t = (double)ix / 149.0;
                  double a = t * Math.PI * 9.0 + time * (double)sign;
                  Vec3 q = heart.add((double)sign * radius + Math.cos(a) * 1.1, Math.sin(a) * 1.1, (t - 0.5) * 5.0);
                  spark(l, q, new Vec3((double)(-sign) * 0.06, 0.0, 0.0), sign < 0 ? 16729449 : 5614335, 18, 0.9F);
               }
            }
            break;
         case ABYSSAL_WORLD_TIDE:
         case KRAKEN_WORLD_DROWN:
            for (int arm = 0; arm < 8; arm++) {
               for (int i = 0; i < 38; i++) {
                  double t = (double)i / 37.0;
                  double a = (double)arm * Math.PI / 4.0 + t * 1.4 + time * 0.15;
                  double r = radius * (0.5 + 0.5 * Math.sin(t * Math.PI));
                  Vec3 q = p.add(Math.cos(a) * r, t * 8.0, Math.sin(a) * r);
                  spark(l, q, new Vec3(0.0, 0.05, 0.0), s == Spell.ABYSSAL_WORLD_TIDE ? 7466475 : 6916061, 24, 0.95F);
                  if (i % 6 == 0) {
                     NativeSpellEffects.point(l, ParticleTypes.SPLASH, q, new Vec3(0.0, 0.1, 0.0), 14, false);
                  }
               }
            }
            break;
         case HELVAR_FINAL_EDICT:
            for (int sword = 0; sword < 5; sword++) {
               double a = (double)sword * Math.PI * 2.0 / 5.0 + time * 0.08;
               Vec3 hilt = heart.add(Math.cos(a) * radius, 1.0, Math.sin(a) * radius);
               Vec3 tip = hilt.add(0.0, -4.0, 0.0);
               stroke(l, hilt, tip, 36, 16752210);
               stroke(l, hilt.add(-0.65, -0.4, 0.0), hilt.add(0.65, -0.4, 0.0), 16, 16764564);
            }
            break;
         case SANDWORM_WORLD_DEVOURER:
            for (int arm = 0; arm < 7; arm++) {
               for (int i = 0; i < 44; i++) {
                  double t = (double)i / 43.0;
                  double a = (double)arm * Math.PI * 2.0 / 7.0 + t * 11.0 + time * 0.7;
                  double r = radius * (1.0 - t * 0.65);
                  spark(l, p.add(Math.cos(a) * r, t * 10.0, Math.sin(a) * r), new Vec3(-Math.sin(a) * 0.08, 0.07, Math.cos(a) * 0.08), 15385991, 24, 0.95F);
               }
            }
      }
   }

   private static void stroke(ClientLevel l, Vec3 from, Vec3 to, int count, int rgb) {
      for (int i = 0; i <= count; i++) {
         spark(l, from.lerp(to, (double)i / (double)count), Vec3.ZERO, rgb, 16, 0.75F);
      }
   }

   private static void spark(ClientLevel l, Vec3 p, Vec3 v, int rgb, int life, float scale) {
      NativeSpellEffects.tinted(l, ParticleTypes.END_ROD, p, v, life, rgb, scale * 1.8F);
   }

   private SpellSpectacle() {
   }

   private static record Front(Spell spell, Vec3 center, double radius, long born, boolean god) {
   }
}

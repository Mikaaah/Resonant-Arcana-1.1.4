package nl.townsandkingdoms.resonantarcana;

import java.util.*;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

/** Status state expires with the native effects; no AI flags or heals survive their lifetime. */
@EventBusSubscriber(modid = ResonantArcana.ID)
final class BurstStatus {
    private static final String LOCK = "resonant_arcana_petrified";
    private record Mark(LivingEntity target, ServerPlayer owner, ServerLevel level, float healing, long expires) {}
    private static final Map<UUID, Mark> MARKS = new HashMap<>();
    private static final Map<UUID, Long> NEXT_HEAL = new HashMap<>();
    private static boolean healing;
    private BurstStatus() {}

    static void petrify(LivingEntity entity, int ticks) {
        entity.addEffect(new MobEffectInstance(ArcanaEffects.PETRIFIED, ticks, 0));
        update(entity);
    }
    static boolean petrified(LivingEntity entity) { return entity.hasEffect(ArcanaEffects.PETRIFIED); }
    static void mark(LivingEntity target, SpellEngine.Cast c) {
        MARKS.put(target.getUUID(), new Mark(target, c.caster, c.level,
            4 * SpellActions.power(c), c.level.getGameTime() + c.duration));
    }
    static void tick() {
        MARKS.values().removeIf(mark -> !mark.target.isAlive() || mark.target.level() != mark.level
            || mark.level.getGameTime() >= mark.expires || !mark.target.hasEffect(ArcanaEffects.FORTUNE_TALISMAN));
        if (MARKS.isEmpty()) NEXT_HEAL.clear();
        else for (Mark mark : MARKS.values()) if (mark.level.getGameTime() % 10 == 0)
            SpellVisuals.send(mark.level, Spell.PRESERVER_OF_FORTUNE, SpellVisuals.Stage.LINK,
                mark.target.getEyePosition().add(0,.4,0), mark.target.getEyePosition().add(0,1.4,0), .7);
    }
    static void clear() {
        MARKS.values().forEach(mark -> mark.target.removeEffect(ArcanaEffects.FORTUNE_TALISMAN));
        MARKS.clear(); NEXT_HEAL.clear();
    }
    @SubscribeEvent
    static void beforeTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity entity && !entity.level().isClientSide()) update(entity);
    }
    @SubscribeEvent
    static void afterTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity && !entity.level().isClientSide()) update(entity);
    }
    @SubscribeEvent
    static void playerBeforeTick(PlayerTickEvent.Pre event) {
        if (!event.getEntity().level().isClientSide()) update(event.getEntity());
    }
    @SubscribeEvent
    static void playerAfterTick(PlayerTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide()) update(event.getEntity());
    }
    private static void update(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        if (petrified(entity)) {
            String dimension = entity.level().dimension().location().toString();
            if (!data.contains(LOCK) || !data.getCompound(LOCK).getString("dimension").equals(dimension)) {
                CompoundTag lock = new CompoundTag();
                lock.putDouble("x", entity.getX()); lock.putDouble("y", entity.getY()); lock.putDouble("z", entity.getZ());
                lock.putString("dimension", dimension);
                lock.putBoolean("noAi", data.contains(LOCK) ? data.getCompound(LOCK).getBoolean("noAi") : entity instanceof Mob mob && mob.isNoAi());
                data.put(LOCK, lock);
                if (entity instanceof ServerPlayer player && MagicData.getPlayerMagicData(player).isCasting()) Utils.serverSideCancelCast(player);
            }
            CompoundTag lock = data.getCompound(LOCK);
            entity.setPos(lock.getDouble("x"), lock.getDouble("y"), lock.getDouble("z"));
            entity.setDeltaMovement(Vec3.ZERO);
            entity.fallDistance = 0;
            entity.hurtMarked = true;
            entity.stopUsingItem();
            if (entity instanceof Mob mob) { mob.setNoAi(true); mob.getNavigation().stop(); }
        } else if (data.contains(LOCK)) {
            if (entity instanceof Mob mob) mob.setNoAi(data.getCompound(LOCK).getBoolean("noAi"));
            data.remove(LOCK);
        }
    }
    @SubscribeEvent
    static void attack(AttackEntityEvent event) { if (petrified(event.getEntity())) event.setCanceled(true); }
    @SubscribeEvent
    static void cast(SpellPreCastEvent event) { if (petrified(event.getEntity())) event.setCanceled(true); }
    @SubscribeEvent
    static void incoming(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker && petrified(attacker)
            && event.getSource().getDirectEntity() == attacker) event.setCanceled(true);
    }
    @SubscribeEvent
    static void damaged(LivingDamageEvent.Post event) {
        if (healing || event.getNewDamage() <= 0 || event.getEntity().level().isClientSide()) return;
        Mark mark = MARKS.get(event.getEntity().getUUID());
        if (mark == null || !mark.target.hasEffect(ArcanaEffects.FORTUNE_TALISMAN)
            || mark.level.getGameTime() >= mark.expires) return;
        // Direct player attacks and spell hits qualify; autonomous projectile/DoT loops cannot farm healing.
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)
            || event.getSource().getDirectEntity() != attacker || !attacker.isAlive()) return;
        ServerPlayer owner = mark.owner;
        if (owner.isRemoved() || !owner.isAlive() || owner.serverLevel() != mark.level || attacker.serverLevel() != mark.level
            || !SpellEngine.healingAlly(owner, attacker)) return;
        long now = mark.level.getGameTime();
        if (NEXT_HEAL.getOrDefault(attacker.getUUID(), Long.MIN_VALUE) > now) return;
        NEXT_HEAL.put(attacker.getUUID(), now + 20);
        healing = true;
        try { attacker.heal((float)(mark.healing * ArcanaConfig.HEALING.get())); }
        finally { healing = false; }
    }
}

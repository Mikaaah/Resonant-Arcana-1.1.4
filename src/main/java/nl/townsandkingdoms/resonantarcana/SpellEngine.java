package nl.townsandkingdoms.resonantarcana;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

/** Server-authoritative, bounded spell simulation. No block edits and no Bukkit code. */
public final class SpellEngine {
    private static final List<Cast> ACTIVE=new ArrayList<>();
    private static final Map<UUID,Map<String,Long>> COOLDOWNS=new HashMap<>();
    private static final Map<UUID,Long> LAST_ACTION=new HashMap<>();
    private static long ticks;
    private static int visualPoints;
    private SpellEngine() {}
    static boolean activeFor(ServerPlayer player,Spell s){return ACTIVE.stream().anyMatch(c->c.owner.equals(player.getUUID())&&c.spell==s);}
    public static int activeCount(){return ACTIVE.size();}
    public static void clear(){ACTIVE.forEach(SpellEngine::cleanup);AdvancedSpells.clear();ExpansionSpells.clear();GodSpells.clear();OrbitalRay.clear();ACTIVE.clear();COOLDOWNS.clear();LAST_ACTION.clear();ticks=0;visualPoints=0;SpellVisuals.tick();}
    public static void action(ServerPlayer player,int action) {
        if(action<0||!player.isAlive()||player.isSpectator())return;
        ItemStack stack=player.getMainHandItem();
        if(!(stack.getItem() instanceof WandItem wand))return;
        if(action>=3&&action-3>=wand.spells.size())return;
        if(LAST_ACTION.getOrDefault(player.getUUID(),Long.MIN_VALUE)==ticks)return;
        LAST_ACTION.put(player.getUUID(),ticks);
        if(action==0){if(ArcanaConfig.WAND_CASTING.get())IronsCompat.castWand(player,wand.selected(stack),stack);else message(player,SpellText.text("message.use_your_spellbook_to_cast","Use your spellbook to cast"));}
        else if(action==2&&IronsCompat.learnOffhand(player,wand,stack))return;
        else if(action>=3){if(action-3<wand.spells.size()){IronsCompat.cancel(player);wand.select(stack,action-3);wand.announce(player,stack);}}
        else {IronsCompat.cancel(player);wand.cycle(player,stack,action==1?1:-1);}
    }
    public static boolean cast(ServerPlayer player,Spell spell) {
        return cast(player,spell,null);
    }
    record BookCast(float power,net.minecraft.world.damagesource.DamageSource source,int cooldown,boolean prepared) {}
    static boolean castFromSpellbook(ServerPlayer player,Spell spell,float power,net.minecraft.world.damagesource.DamageSource source,int cooldown,boolean prepared){
        return cast(player,spell,new BookCast(Math.clamp(power,0,10),source,Math.max(1,cooldown),prepared));
    }
    static boolean canStart(ServerPlayer player,Spell spell){
        if(!spell.available()||!player.isAlive()||player.isSpectator()||ACTIVE.size()>=ArcanaConfig.MAX_ACTIVE.get()||ACTIVE.stream().filter(c->c.owner.equals(player.getUUID())).count()>=ArcanaConfig.PER_PLAYER_LIMIT.get())return false;
        if(COOLDOWNS.getOrDefault(player.getUUID(),Map.of()).getOrDefault(spell.cooldownKey(),0L)>ticks)return false;
        Vec3 start=player.getEyePosition();
        if(!SpellActions.canStart(player,spell))return false;
        return !requiresTarget(spell)||target(player,start,clipLoaded(player.serverLevel(),start,player.getLookAngle(),spell.range,player),0.5)!=null;
    }
    static void bookCharge(ServerPlayer player,Spell spell,double progress,int elapsed){
        try(var visibilityScope=SpellParticleScope.enter(spell)){
        if(TacticalAudio.handles(spell)&&elapsed%4==0)SpellLoops.sync(player,spell,true,false,0,progress);
        Vec3 dir=player.getLookAngle().normalize(),eye=player.getEyePosition();
        if(TacticalAudio.handles(spell)){if(elapsed%4==0){Vec3 focus=clipLoaded(player.serverLevel(),eye,dir,1.7,player);SpellVisuals.send(player.serverLevel(),spell,SpellVisuals.Stage.CHARGE,focus,focus.add(dir),.5+progress);}return;}
        if(spell==Spell.ORBITAL_RAY){OrbitalRay.charge(player,progress,elapsed);return;}
        if(GodSpells.handles(spell)){GodSpells.charge(player,spell,progress,elapsed);return;}
        if(CavesCompat.optional(spell)){CavesCompat.charge(player,spell,progress,elapsed);return;}
        if(spell==Spell.I_AM_ATOMIC){AtomicRitual.charge(player,progress,elapsed);return;}
        if(spell==Spell.DOOM_BEAM){
            if(elapsed%4==0){
                Vec3 focus=clipLoaded(player.serverLevel(),eye,dir,3,player);
                BeamRitual.sync(player,1,progress,eye.distanceTo(focus));
                SpellVisuals.send(player.serverLevel(),spell,SpellVisuals.Stage.RITUAL_TARGET,player.position().add(0,.06,0),player.position().add(dir),3.5+progress);
            }
            return;
        }
        if(spell==Spell.EXPLOSION)ExplosionRitual.charge(player,progress,elapsed);
        if(spell!=Spell.SOUL_CANNON&&spell!=Spell.DOOM&&spell.kind!=Spell.Kind.CATACLYSM)SpellAudio.charging(player,spell,progress,elapsed);
        Vec3 center=clipLoaded(player.serverLevel(),eye,dir,spell==Spell.DOOM?3:2,player).add(0,spell==Spell.DOOM?1.8:-0.18,0);
        if(spell!=Spell.DOOM&&spell.kind!=Spell.Kind.CATACLYSM&&elapsed%8==0)SpellVisuals.send(player.serverLevel(),spell,SpellVisuals.Stage.RITUAL_TARGET,player.position().add(0,.06,0),player.position().add(0,1.06,0),2.5+progress);
        if(BossesRiseCompat.optional(spell)){
            if(elapsed%4==0)BossesRiseBridge.castCue(player.serverLevel(),spell,center);
            return;
        }
        if(spell==Spell.DOOM||spell.kind==Spell.Kind.CATACLYSM){
            Vec3 feet=player.position().add(0,.04,0);
            if(elapsed%4==0)SpellVisuals.send(player.serverLevel(),spell,SpellVisuals.Stage.CHARGE,feet,feet.add(dir),5+progress*2);
            SpellAudio.ritualPulse(player,spell,feet,progress,elapsed);
            return;
        }
        if(elapsed%3==0)SpellVisuals.send(player.serverLevel(),spell,SpellVisuals.Stage.CHARGE,center,center.add(dir),0.45+progress);
        for(int i=0;i<3;i++){
            double angle=elapsed*.65+i*Math.PI*2/3;Vec3 offset=spell==Spell.SOUL_CANNON?new Vec3(Math.cos(angle)*1.6,Math.sin(angle)*1.1,Math.sin(angle*.7)):SpellParticles.chargeOffset(spell.theme,angle,progress);
            Vec3 pos=center.add(offset),velocity=offset.scale(-.12);
            if(spell.theme==Spell.Theme.SOUL||spell.theme==Spell.Theme.DARK||spell.theme==Spell.Theme.GRAVITY)
                SpellParticles.send(player.serverLevel(),ParticleTypes.REVERSE_PORTAL,pos.x,pos.y,pos.z,0,velocity.x,velocity.y,velocity.z,1);
            else trail(player.serverLevel(),pos,spell.theme,.025);
        }
        if(spell==Spell.SOUL_CANNON){
            if(elapsed%2==0)SpellParticles.cannonCharge(player.serverLevel(),center,dir,progress,elapsed);
            if(elapsed%8==0)SpellAudio.cannonCharge(player,center,(float)progress);
        }

        }
    }
    private static boolean cast(ServerPlayer player,Spell spell,BookCast book) {
        try(var visibilityScope=SpellParticleScope.enter(spell)){
        if(!SpellActions.canStart(player,spell))return false;
        if(!spell.available()||!player.isAlive()||player.isSpectator())return false;
        var cooldowns=COOLDOWNS.computeIfAbsent(player.getUUID(),id->new HashMap<>());
        long left=cooldowns.getOrDefault(spell.cooldownKey(),0L)-ticks;
        if(left>0){message(player,SpellText.text("message.cooldown","%s: %ss",spell.displayName(),SpellText.number(left/20.0)));return false;}
        if(ACTIVE.size()>=ArcanaConfig.MAX_ACTIVE.get()||ACTIVE.stream().filter(c->c.owner.equals(player.getUUID())).count()>=ArcanaConfig.PER_PLAYER_LIMIT.get()){message(player,SpellText.text("message.too_many_active_spells","Too many active spells"));return false;}
        ServerLevel level=player.serverLevel();
        Vec3 start=player.getEyePosition(), direction=player.getLookAngle().normalize();
        Vec3 aim=clipLoaded(level,start,direction,spell.range,player);
        if(spell==Spell.EXPLOSION&&book!=null&&book.prepared()){
            var locked=ExplosionRitual.locked(player);
            if(locked==null||!locked.valid(player)){message(player,SpellText.text("message.explosion_s_locked_target_is_no_longer_reachable","Explosion's locked target is no longer reachable"));return false;}
            aim=locked.point();
        }else if(spell==Spell.EXPLOSION){
            var marked=target(player,start,aim,.5);if(marked!=null)aim=marked.getBoundingBox().getCenter();
        }
        LivingEntity target=spell==Spell.EXPLOSION?null:target(player,start,aim,0.5);
        if(requiresTarget(spell)&&target==null){message(player,SpellText.text("message.aim_at_a_living_target_within_range","Aim at a living target within range"));return false;}
        Cast cast=new Cast(player,spell,start,direction,aim,target);
        cast.book=book;ExpansionSpells.focus(cast);
        boolean timed=true;
        Vec3 side=direction.cross(new Vec3(0,1,0));if(side.lengthSqr()<.01)side=new Vec3(1,0,0);side=side.normalize();
        Vec3 muzzle=clipLoaded(level,start,direction,1.5,player).add(side.scale(.35)).add(0,-.3,0);
        if(BossesRiseCompat.optional(spell))BossesRiseBridge.castCue(level,spell,muzzle);
        else for(int i=0;i<9;i++){
            double t=i/8.0,a=t*Math.PI*1.35;
            trail(level,muzzle.add(side.scale(Math.cos(a)*.35)).add(0,Math.sin(a)*.25,0).add(direction.scale(t*.6)),spell.theme,.02);
        }
        if(spell.theme==Spell.Theme.SOUL)SpellParticles.send(level,ParticleTypes.SCULK_CHARGE_POP,start.x,start.y,start.z,6,0.15,0.15,0.15,0.02);
        if(!RecordedSpells.handles(spell)&&(book==null||!book.prepared()))SpellAudio.cast(player,spell);
        SpellVisuals.send(level,spell,SpellVisuals.Stage.CAST,muzzle,muzzle.add(direction),0.65);

        if(SpellActions.handles(spell.kind))timed=SpellActions.begin(cast);else switch(spell.kind){
            case TARGET, CONFUSE, STUN, LAUNCH, PUSH, PULL, SIPHON -> {
                timed=false;
                if(target==null){message(player,SpellText.text("message.aim_at_a_living_target_within_range","Aim at a living target within range"));return false;}
                if(spell.kind!=Spell.Kind.SIPHON)SpellVisuals.send(level,spell,SpellVisuals.Stage.LINK,start,target.getBoundingBox().getCenter(),1);
                if(spell.kind==Spell.Kind.CONFUSE||spell.kind==Spell.Kind.STUN)SpellVisuals.send(level,spell,SpellVisuals.Stage.AREA,target.getBoundingBox().getCenter(),target.getBoundingBox().getCenter(),0.9);
                hit(cast,target,target.position(),true);
                burst(cast,target.getBoundingBox().getCenter());
            }
            case BURST -> {timed=false;Vec3 loc=target!=null?target.getBoundingBox().getCenter():aim;area(cast,loc,spell.radius,true);burst(cast,loc);}
            case LIGHTNING,SMITE -> {
                timed=false;Vec3 loc=target!=null?target.position():aim;
                lightning(cast,loc);area(cast,loc,spell.radius,true);
            }
            case CHAIN -> {timed=false;chain(cast,target);}
            case HEAL -> {timed=false;sanctuary(cast);}
            case FROSTFRONT,FANGS -> {
                cast.pos=spell.kind==Spell.Kind.FROSTFRONT?BeamRitual.origin(player):player.position().add(0,.2,0);
                cast.travel=new Vec3(direction.x,0,direction.z).normalize();
                if(cast.travel.lengthSqr()<.01)cast.travel=Vec3.directionFromRotation(0,player.getYRot());
                cast.duration=24;
            }
            case DOOM -> {cast.pos=target!=null?target.getBoundingBox().getCenter():aim.subtract(direction.scale(.08));cast.duration=96;}
            case METEOR -> {
                cast.anchor=target!=null?target.getBoundingBox().getCenter():aim.subtract(direction.scale(.08));
                // Start on the reachable side of the ceiling; no chunks or blocks are changed.
                cast.pos=clipLoaded(level,cast.anchor,new Vec3(0,1,0),16,player).add(0,-.08,0);cast.duration=40;
            }
            case RAIL -> {cast.duration=16;cast.pos=BeamRitual.origin(player);}
            case COLLAPSE -> {cast.pos=target!=null?target.getBoundingBox().getCenter():aim.subtract(direction.scale(.08));cast.duration=36;}
            case STORM, WELL, FIELD -> {cast.pos=target!=null?target.position():aim;cast.duration=96;}
            case AURA -> {cast.pos=player.position();cast.duration=60;}
            case LEAP -> {SpellVisuals.send(level,spell,SpellVisuals.Stage.LINK,start.subtract(direction.scale(3)),start,1);timed=false;player.setDeltaMovement(direction.scale(1.15).add(0,0.4,0));player.hurtMarked=true;trail(level,start,spell.theme,0.25);}
            case CAPTURE, CHOKE -> {cast.anchor=target.position();cast.duration=SpellActions.supportDuration(cast,60);}
            case PULSE -> {cast.pos=player.position().add(0,0.5,0);cast.duration=14;}
            case WARD -> cast.duration=SpellActions.supportDuration(cast,spell.duration());
            case TWISTER -> {cast.pos=target!=null?target.position():aim;cast.duration=40;}
            case CHANNEL -> cast.duration=spell.duration();
            case SONIC -> cast.duration=30;
            case CANNON -> cast.duration=80;
            case MARK -> {cast.pos=target.position();cast.duration=40;}
            case SUSPEND -> {cast.anchor=target.position();cast.pos=target.position();cast.duration=40;}
            case VOLLEY -> cast.duration=44;
            case LANCE -> {timed=false;beam(cast,BeamRitual.origin(player),direction);}
            default -> cast.duration=(int)Math.ceil(spell.range/(spell.kind==Spell.Kind.COMET?0.9:1.1))+2;
        }
        if(book!=null&&book.prepared())cast.age=spell.kind==Spell.Kind.CANNON?39:spell.kind==Spell.Kind.SONIC?19:0;
        if(timed)ACTIVE.add(cast);
        int cooldown=book!=null?book.cooldown():Math.max(1,(int)Math.ceil(spell.cooldown*ArcanaConfig.COOLDOWNS.get()));
        // Iron's owns individual spell cooldowns; keep only cross-spell recovery groups here.
        if(spell!=Spell.RHO_AIAS&&(book==null||!spell.cooldownKey().equals(spell.name())))cooldowns.put(spell.cooldownKey(),ticks+cooldown);
        message(player,spell.displayName());return true;

        }
    }
    private static boolean requiresTarget(Spell spell){return switch(spell.kind){case TARGET,CONFUSE,STUN,CAPTURE,LAUNCH,PUSH,PULL,CHOKE,CHAIN,SEEKER,SIPHON,MARK,SUSPEND,TETHER,EXECUTE,EXILE->true;default->false;};}
    private static void cleanup(Cast c){RhoAias.cleanup(c);ExpansionSpells.cleanup(c);SpellLoops.stop(c.caster,c.spell,true);if(c.spell==Spell.TREMORZILLA_BREATH||c.spell==Spell.TREMORZILLA_CATACLYSM)SpellAudio.world(c.level,c.pos,"alexscaves:tremorzilla_beam_end",.8f,1);GodSpells.cleanup(c);OrbitalRay.cleanup(c);VoidDomain.cleanup(c);BossesRiseCompat.cleanup(c);AdvancedSpells.cleanup(c);SpellCentrepieces.cleanup(c);SpecialSpells.cleanup(c);}
    public static void tick(){
        ticks++;visualPoints=0;SpellVisuals.tick();
        VoidExile.tick(net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer());
        for(Iterator<Cast> it=ACTIVE.iterator();it.hasNext();){Cast c=it.next();
            ServerPlayer owner=c.level.getServer().getPlayerList().getPlayer(c.owner);
            // Fake players are only allowed through the explicitly gated integration fixture.
            if(owner==null&&IntegrationChecks.enabled())owner=c.testOwner;
            if(owner==null||!owner.isAlive()||owner.isSpectator()||owner.serverLevel()!=c.level||c.age++>=c.duration||!c.level.hasChunkAt(BlockPos.containing(c.pos))){cleanup(c);it.remove();continue;}
            c.caster=owner;
            if(!advance(c)){cleanup(c);it.remove();}
        }
        if(ticks%200==0){
            COOLDOWNS.values().forEach(map->map.entrySet().removeIf(e->e.getValue()<=ticks));
            COOLDOWNS.entrySet().removeIf(e->e.getValue().isEmpty());
            LAST_ACTION.entrySet().removeIf(e->ticks-e.getValue()>200);
        }
    }
    private static boolean advance(Cast c){
        try(var visibilityScope=SpellParticleScope.enter(c.spell)){
        // Sustained cues are bounded to one per cast per second.
        if(!TacticalAudio.handles(c.spell)&&!GodSpells.handles(c.spell)&&!CavesCompat.optional(c.spell)&&!RecordedSpells.handles(c.spell)&&!BossesRiseCompat.optional(c.spell)&&c.spell.kind!=Spell.Kind.WARCRY&&c.spell.kind!=Spell.Kind.CANNON&&c.spell.kind!=Spell.Kind.DOOM&&c.spell.kind!=Spell.Kind.CATACLYSM&&c.age%20==1)SpellAudio.world(c.level,c.pos,SpellAudio.sustainId(c.spell),0.55f,1.0f);
        if(SpellActions.handles(c.spell.kind))return SpellActions.tick(c);
        switch(c.spell.kind){
            case DOOM -> {
                double radius=Math.min(c.spell.radius,2+c.age*.25);
                if(c.age%2==1)SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.AREA,c.pos.add(0,5,0),c.pos.add(0,5,0),radius);
                SpellAudio.doom(c.caster,c.pos,c.age/90.0,c.age);
                for(var victim:near(c.caster,c.pos,radius))if(visible(c,c.pos,victim.getBoundingBox().getCenter())){
                    Vec3 pull=c.pos.subtract(victim.getBoundingBox().getCenter());
                    victim.setDeltaMovement(pull.normalize().scale(Math.min(.5,pull.length()*.13)));victim.hurtMarked=true;
                    if(c.age>=40&&pull.length()<1.8)hit(c,victim,c.pos,true);
                }
                if(c.age==90){
                    // Original native collapse is owned by impactFeedback below.
                    area(c,c.pos,c.spell.radius,true);
                    SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.IMPACT,c.pos.add(0,5,0),c.pos.add(0,5,0),12);
                    impactFeedback(c,c.pos);return false;
                }
            }
            case FROSTFRONT -> {
                double distance=c.age*1.5,width=Math.min(c.spell.radius,distance/3);
                Vec3 requested=c.pos.add(c.travel.scale(distance));Vec3 end=clipLoaded(c.level,c.pos,c.travel,Math.min(distance,c.spell.range),c.caster);
                if(c.age%2==1)SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.FLIGHT,end,end.add(c.travel),Math.max(1,width));
                for(var victim:near(c.caster,c.pos,c.spell.range)){
                    Vec3 delta=victim.getBoundingBox().getCenter().subtract(c.pos);double along=delta.dot(c.travel);
                    double across=delta.subtract(c.travel.scale(along)).horizontalDistance();
                    if(Math.abs(along-distance)<=2&&across<=width+1&&Math.abs(delta.y)<4&&visible(c,c.pos,victim.getBoundingBox().getCenter())){
                        hit(c,victim,end,true);effect(victim,MobEffects.MOVEMENT_SLOWDOWN,80,2);victim.setTicksFrozen(Math.max(160,victim.getTicksFrozen()));
                    }
                }
                if(distance>=c.spell.range||end.distanceTo(requested)>.2){burst(c,end);return false;}
            }
            case FANGS -> {
                if(c.age%3!=1)return true;
                double distance=2+(c.age-1)/3*3;
                Vec3 main=clipLoaded(c.level,c.pos,c.travel,Math.min(distance,c.spell.range),c.caster);
                Vec3 side=c.travel.cross(new Vec3(0,1,0)).normalize();
                for(int lane=-1;lane<=1;lane++){
                    Vec3 end=main.add(side.scale(lane*1.7));
                    if(!c.level.hasChunkAt(BlockPos.containing(end))||!visible(c,c.pos,end))continue;
                    SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.AREA,end,end.add(c.travel),1.3);
                    area(c,end,c.spell.radius,true);
                }
                if(c.age%6==1)SpellAudio.world(c.level,main,"entity.evoker_fangs.attack",1,.7f);
                if(distance>=c.spell.range||main.distanceTo(c.pos)<distance-.2)return false;
            }
            case METEOR -> {
                if(c.age<=12){
                    if(c.age%3==1)SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.AREA,c.anchor,c.anchor,c.spell.radius);
                    return true;
                }
                Vec3 to=c.anchor.subtract(c.pos);double remaining=to.length();
                Vec3 requested=remaining<=2?c.anchor:c.pos.add(to.normalize().scale(2));
                if(!c.level.hasChunkAt(BlockPos.containing(requested)))return false;
                var collision=c.level.clip(new ClipContext(c.pos,requested,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,c.caster));
                Vec3 next=collision.getLocation();
                boolean blocked=collision.getType()!=HitResult.Type.MISS;
                boolean stop=remaining<=2||blocked;
                SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.FLIGHT,next,next.add(0,-1,0),2.2);
                c.pos=next;
                if(stop){if(blocked)c.pos=c.pos.add(0,.05,0);area(c,c.pos,c.spell.radius,true);burst(c,c.pos);return false;}
            }
            case RAIL -> {
                if(c.age==1){
                    c.anchor=clipLoaded(c.level,c.pos,c.dir,c.spell.range,c.caster);
                    beam(c,c.pos,c.dir);
                }else if(c.age==5||c.age==10){
                    SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.LINK,c.pos,c.anchor,c.age==5?.7:.35);
                }
            }
            case COLLAPSE -> {
                if(c.age<=30){
                    if(c.age%4==1)SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.AREA,c.pos,c.pos,c.spell.radius*(1-c.age/45.0));
                    for(var victim:near(c.caster,c.pos,c.spell.radius))if(visible(c,c.pos,victim.getBoundingBox().getCenter())){
                        Vec3 pull=c.pos.subtract(victim.getBoundingBox().getCenter());
                        victim.setDeltaMovement(pull.normalize().scale(Math.min(.32,pull.length()*.12)));victim.hurtMarked=true;
                    }
                    return true;
                }
                area(c,c.pos,c.spell.radius,true);burst(c,c.pos);return false;
            }
            case BOLT,COMET,WAVE,SEEKER -> {
                ExpansionSpells.route(c);
                if(c.spell.kind==Spell.Kind.SEEKER){
                    if(c.target==null||!c.target.isAlive()||c.target.level()!=c.level||!allowed(c.caster,c.target))return false;
                    Vec3 desired=c.target.getBoundingBox().getCenter().subtract(c.pos).normalize();
                    c.travel=c.travel.scale(0.65).add(desired.scale(0.35)).normalize();
                }
                Vec3 next=c.pos.add(c.travel.scale(c.spell.kind==Spell.Kind.COMET?0.9:1.1));
                if(!c.level.hasChunkAt(BlockPos.containing(next)))return false;
                var block=c.level.clip(new ClipContext(c.pos,next,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,c.caster));
                Vec3 end=block.getLocation();
                if(c.spell.kind==Spell.Kind.WAVE){
                    for(var entity:near(c.caster,c.pos.add(end).scale(0.5),c.spell.radius+1))
                        if(entity.getBoundingBox().inflate(c.spell.radius).clip(c.pos,end).isPresent()||entity.getBoundingBox().inflate(c.spell.radius).contains(c.pos)) { if(visible(c,c.pos,entity.getBoundingBox().getCenter()))hit(c,entity,end,true); }
                    waveVisual(c,end);
                } else {
                    LivingEntity victim=target(c.caster,c.pos,end,c.spell.kind==Spell.Kind.COMET?0.65:0.3);
                    if(victim!=null){Vec3 impact=victim.getBoundingBox().getCenter();area(c,impact,c.spell.radius,true);burst(c,impact);return false;}
                    for(int s=0;s<3;s++){
                        Vec3 point=c.pos.lerp(end,s/3.0);trail(c.level,point,c.spell.theme,0.06);
                        if(c.spell.kind==Spell.Kind.COMET){
                            Vec3 side=c.travel.cross(new Vec3(0,1,0));if(side.lengthSqr()<0.01)side=new Vec3(1,0,0);side=side.normalize();
                            Vec3 up=side.cross(c.travel).normalize();double angle=c.age*0.7+s*0.25;
                            Vec3 spiral=side.scale(Math.cos(angle)*0.35).add(up.scale(Math.sin(angle)*0.35));
                            trail(c.level,point.add(spiral),c.spell.theme,0.015);trail(c.level,point.subtract(spiral),c.spell.theme,0.015);
                        }
                    }
                }
                if(c.age%2==1)SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.FLIGHT,end,end.add(c.travel),c.spell.kind==Spell.Kind.WAVE?c.spell.radius:c.spell.kind==Spell.Kind.COMET?1.15:0.65);
                c.pos=end;
                if(block.getType()!=HitResult.Type.MISS){if(c.spell.kind!=Spell.Kind.WAVE){area(c,end,c.spell.radius,true);burst(c,end);}return false;}
            }
            case PULSE -> {
                double radius=c.spell.radius*c.age/c.duration;
                if(c.age%2==1)SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.AREA,c.pos,c.pos,radius);
                ring(c.level,c.pos,radius,c.spell.theme,c.age*0.1);
                for(var victim:near(c.caster,c.pos,radius+1)){
                    double d=victim.position().distanceTo(c.pos);
                    if(Math.abs(d-radius)<1.5&&visible(c,c.pos,victim.getBoundingBox().getCenter()))hit(c,victim,c.pos,true);
                }
            }
            case CAPTURE, CHOKE -> {
                LivingEntity victim=c.target;
                if(victim==null||!victim.isAlive()||victim.level()!=c.level||!allowed(c.caster,victim)||victim.position().distanceToSqr(c.anchor)>16||victim.distanceToSqr(c.caster)>(c.spell.range+4)*(c.spell.range+4)||!visible(c,c.caster.getEyePosition(),victim.getBoundingBox().getCenter()))return false;
                // Do not teleport a target back across a wall or a large external movement.
                if(c.age==1)impactFeedback(c,victim.getBoundingBox().getCenter());
                victim.setDeltaMovement(Vec3.ZERO);victim.hurtMarked=true;
                effect(victim,MobEffects.MOVEMENT_SLOWDOWN,5,10);effect(victim,MobEffects.WEAKNESS,5,2);
                if(c.spell.kind==Spell.Kind.CHOKE&&c.age%20==1&&c.age<=c.spell.duration()){c.hit.clear();hit(c,victim,c.anchor,true);}
                if(c.age%4==1)SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.AREA,victim.getBoundingBox().getCenter(),victim.getBoundingBox().getCenter(),0.9);
                if(c.age%4==0)ring(c.level,victim.position().add(0,0.2+c.age%12*0.12,0),0.8,c.spell.theme,c.age*0.2);
            }
            case MARK, SUSPEND -> {
                LivingEntity victim=c.target;
                if(victim==null||!victim.isAlive()||victim.level()!=c.level||!allowed(c.caster,victim)||victim.distanceToSqr(c.caster)>(c.spell.range+4)*(c.spell.range+4)||!visible(c,c.caster.getEyePosition(),victim.getBoundingBox().getCenter()))return false;
                c.pos=victim.getBoundingBox().getCenter();
                if(c.spell.kind==Spell.Kind.SUSPEND){
                    if(victim.position().distanceToSqr(c.anchor)>16)return false;
                    victim.setDeltaMovement(victim.getDeltaMovement().multiply(0.5,0,0.5).add(0,victim.getY()<c.anchor.y+2.5?0.08:0,0));victim.hurtMarked=true;
                    effect(victim,MobEffects.MOVEMENT_SLOWDOWN,5,2);effect(victim,MobEffects.SLOW_FALLING,40,0);
                    if(c.age==1)hit(c,victim,c.pos,true);
                }
                if(c.age%4==1)SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.AREA,c.pos,c.pos,0.9);
                if(c.age%4==0)ring(c.level,c.pos,0.7,c.spell.theme,c.age*0.3);
                if(c.spell.kind==Spell.Kind.MARK&&c.age==40){area(c,c.pos,c.spell.radius,true);burst(c,c.pos);return false;}
            }
            case VOLLEY -> {
                if((c.age-1)%c.spell.interval()==0&&c.age<=1+(c.spell.pulses()-1)*c.spell.interval()){
                    c.bolts.add(new VolleyBolt(c.caster.getEyePosition(),c.caster.getLookAngle().normalize()));
                    SpellAudio.world(c.level,c.caster.position(),SpellAudio.castId(c.spell.theme),0.45f,1.1f+c.age*0.015f);
                }
                for(var it=c.bolts.iterator();it.hasNext();){
                    VolleyBolt b=it.next();Vec3 next=b.pos.add(b.dir.scale(1.1));
                    if(b.distance>=c.spell.range||!c.level.hasChunkAt(BlockPos.containing(next))){it.remove();continue;}
                    var block=c.level.clip(new ClipContext(b.pos,next,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,c.caster));Vec3 end=block.getLocation();
                    LivingEntity victim=target(c.caster,b.pos,end,0.3);
                    if(victim!=null){c.hit.clear();area(c,victim.getBoundingBox().getCenter(),c.spell.radius,true);burst(c,victim.getBoundingBox().getCenter());it.remove();continue;}
                    for(int i=0;i<3;i++)trail(c.level,b.pos.lerp(end,i/3.0),c.spell.theme,0.035);
                    if(c.age%2==1)SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.FLIGHT,end,end.add(b.dir),0.6);
                    b.pos=end;b.distance+=1.1;
                    if(block.getType()!=HitResult.Type.MISS)it.remove();
                }
                if(c.age>=1+(c.spell.pulses()-1)*c.spell.interval()&&c.bolts.isEmpty())return false;
            }
            case WELL, STORM, AURA, FIELD -> {
                if(c.spell.kind==Spell.Kind.AURA)c.pos=c.caster.position().add(0,0.3,0);
                if(c.age%4==1){
                    SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.AREA,c.pos,c.pos,c.spell.radius);
                    ring(c.level,c.pos.add(0,0.15,0),c.spell.radius,c.spell.theme,c.age*0.15);
                    ring(c.level,c.pos.add(0,1.0,0),c.spell.radius*0.45,c.spell.theme,-c.age*0.2);
                }
                if(c.age%20==1){
                    c.hit.clear();area(c,c.pos,c.spell.radius,true);
                    if(c.spell.kind==Spell.Kind.STORM){
                        Vec3 flash=c.pos.add(Math.sin(c.age*2.4)*c.spell.radius*0.65,0,Math.cos(c.age*1.7)*c.spell.radius*0.65);
                        SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.STRIKE,flash.add(0,10,0),flash,1);
                        var victims=near(c.caster,c.pos,c.spell.radius);
                        int bolts=0;for(var victim:victims)if(visible(c,c.pos,victim.getBoundingBox().getCenter())&&bolts++<3)lightning(c,victim.position());
                    }
                }
                if(c.spell.kind==Spell.Kind.WELL)for(var victim:near(c.caster,c.pos,c.spell.radius))if(visible(c,c.pos,victim.getBoundingBox().getCenter())){
                    Vec3 delta=c.pos.subtract(victim.position()).multiply(1,0,1);
                    Vec3 pull=delta.normalize().scale(Math.min(0.18,delta.length()));
                    victim.setDeltaMovement(pull.add(0,victim.getDeltaMovement().y*0.7,0));victim.hurtMarked=true;
                }
            }
            case TWISTER -> {
                if(c.age%4==1)SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.AREA,c.pos,c.pos,c.spell.radius);
                if(c.age%2==0)for(int i=0;i<3;i++)ring(c.level,c.pos.add(0,i*0.65,0),c.spell.radius*(1-i*0.22),c.spell.theme,c.age*0.35+i);
                if(c.age%10==1){c.hit.clear();area(c,c.pos,c.spell.radius,true);}
            }
            case CANNON -> {return cannon(c);}
            case SONIC -> {
                if(c.age<=20){c.pos=c.caster.getEyePosition();c.travel=c.caster.getLookAngle().normalize();}
                if(c.age<20){
                    if(c.age%3==1)SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.CHARGE,c.pos.add(c.travel.scale(1.5)),c.pos.add(c.travel.scale(2.5)),.4+c.age*.025);
                    return true;
                }
                if(c.age==20)SpellAudio.scream(c.caster,c.pos);
                double radius=(c.age-19)*1.5;
                Vec3 front=c.pos.add(c.travel.scale(radius));
                SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.FLIGHT,front,front.add(c.travel),radius*.95);
                for(var victim:near(c.caster,c.pos,c.spell.range)){
                    Vec3 delta=victim.getBoundingBox().getCenter().subtract(c.pos);double length=delta.length();
                    if(length<=c.spell.range&&Math.abs(length-radius)<2&&delta.normalize().dot(c.travel)>=.342&&visible(c,c.pos,victim.getBoundingBox().getCenter())){
                        hit(c,victim,c.pos,true);Vec3 push=delta.normalize().scale(.8);victim.push(push.x,.2,push.z);victim.hurtMarked=true;
                    }
                }
                SpellParticles.send(c.level,ParticleTypes.SONIC_BOOM,front.x,front.y,front.z,1,0,0,0,0);
                if(radius>=c.spell.range){SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.IMPACT,front,front.add(c.travel),5);return false;}
            }

            case CHANNEL -> {
                if((c.age-1)%c.spell.interval()!=0)return true;
                c.hit.clear();Vec3 eye=BeamRitual.origin(c.caster),dir=c.caster.getLookAngle();
                Vec3 end=clipLoaded(c.level,eye,dir,c.spell.range,c.caster);
                SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.LINK,eye,end,1);
                double length=eye.distanceTo(end);
                for(double d=0;d<=length;d+=0.6){Vec3 p=eye.add(dir.scale(d));trail(c.level,p,c.spell.theme,0.08+d*0.015);}
                for(var victim:lineTargets(c,eye,end,c.spell.radius))
                    if(visible(c,eye,victim.getBoundingBox().getCenter()))hit(c,victim,eye,true);
            }
            case WARD -> {
                c.pos=c.caster.position().add(0,1,0);
                if(c.age%3==1)SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.AREA,c.pos,c.pos,c.spell.radius);
                if(c.age%4==1)ring(c.level,c.pos,c.spell.radius,c.spell.theme,c.age*0.15);
                for(var p:c.level.getEntitiesOfClass(Projectile.class,new AABB(c.pos,c.pos).inflate(c.spell.radius),p->p.getOwner()!=c.caster)) {
                    if(p.getOwner()!=null&&!allowed(c.caster,p.getOwner()))continue;
                    if(p.getDeltaMovement().dot(p.position().subtract(c.pos))<0){
                        p.setDeltaMovement(p.position().subtract(c.pos).normalize().scale(Math.max(0.5,p.getDeltaMovement().length())));p.hurtMarked=true;impactFeedback(c,p.position());
                    }
                }
            }
            default -> {return false;}
        }
        return true;

        }
    }
    /** Charge follows the caster; the released projectile keeps its direction. */
    private static boolean cannon(Cast c){
        if(c.impactAge>0){
            if(c.age-c.impactAge==4){SpellAudio.cannon(c.caster,c.pos,"tail");return false;}
            return true;
        }
        if(c.age<=40){
            if(c.book==null&&!(c.caster.getMainHandItem().getItem() instanceof WandItem))return false;
            c.travel=c.caster.getLookAngle().normalize();
            // Keep the forward offset while moving the source below the first-person camera.
            c.pos=clipLoaded(c.level,BeamRitual.body(c.caster,1),c.travel,BeamRitual.FORWARD,c.caster);
            double progress=c.age/40.0;
            if(c.age<40){
                if(c.age%2==0)SpellParticles.cannonCharge(c.level,c.pos,c.travel,progress,c.age);
                if(c.age%3==1)SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.CHARGE,c.pos,c.pos.add(c.travel),0.6+progress*0.9);
                // Directional count-zero packets pull real vanilla void motes towards the muzzle.
                for(int i=0;i<4;i++){
                    double angle=c.age*0.57+i*Math.PI/2,r=2.8-progress*1.2;
                    Vec3 offset=new Vec3(Math.cos(angle)*r,Math.sin(angle*1.7)*r*0.65,Math.sin(angle)*r);
                    Vec3 p=c.pos.add(offset),v=offset.scale(-0.12);
                    SpellParticles.send(c.level,ParticleTypes.REVERSE_PORTAL,p.x,p.y,p.z,0,v.x,v.y,v.z,1);
                    if(i==0)trail(c.level,p,c.spell.theme,0.025);
                }
                if(c.age%8==0)SpellAudio.cannonCharge(c.caster,c.pos,(float)progress);
                return true;
            }
            SpellAudio.cannon(c.caster,c.pos,"release");
            SpellParticles.send(c.level,ParticleTypes.SONIC_BOOM,c.pos.x,c.pos.y,c.pos.z,1,0,0,0,0);
        }
        Vec3 start=c.pos;
        Vec3 end=clipLoaded(c.level,start,c.travel,Math.min(2,c.spell.range-c.distance),c.caster);
        boolean blocked=end.distanceToSqr(start)<3.999;
        LivingEntity victim=target(c.caster,start,end,1.1);
        if(victim!=null){
            // Clip the expanded hitbox on the reachable segment, never move through the wall to its centre.
            end=victim.getBoundingBox().inflate(1.1).clip(start,end).orElse(start);
        }
        c.distance+=start.distanceTo(end);c.pos=end;
        SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.FLIGHT,end,end.add(c.travel),1.8);
        for(int i=0;i<3;i++){
            Vec3 p=start.lerp(end,i/3.0);trail(c.level,p,c.spell.theme,0.18);
        }
        SpellParticles.send(c.level,ParticleTypes.SONIC_BOOM,end.x,end.y,end.z,1,0,0,0,0);
        if(blocked||victim!=null||c.distance>=c.spell.range-0.01){
            // An impact just outside the surface keeps sight checks on the projectile's side of a wall.
            Vec3 impact=end.subtract(c.travel.scale(0.05));
            c.pos=impact;area(c,impact,c.spell.radius,true);
            if(victim!=null&&visible(c,impact,victim.getBoundingBox().getCenter()))hit(c,victim,impact,true);
            SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.IMPACT,impact,impact,6.5);
            cannonSparks(c,impact,3.2f);
            for(int i=0;i<3;i++)ring(c.level,impact.add(0,i*0.45,0),2+i*1.6,c.spell.theme,i);
            SpellParticles.send(c.level,ParticleTypes.SCULK_CHARGE_POP,impact.x,impact.y,impact.z,48,1.8,1.8,1.8,0.1);
            SpellParticles.send(c.level,new BlockParticleOption(ParticleTypes.BLOCK,Blocks.AMETHYST_BLOCK.defaultBlockState()),impact.x,impact.y,impact.z,48,1.3,1.3,1.3,0.3);
            SpellParticles.send(c.level,new ShriekParticleOption(0),impact.x,impact.y,impact.z,1,0,0,0,0);
            SpellAudio.cannon(c.caster,impact,"impact");c.impactAge=c.age;
        }else if(c.distance+0.001>=c.nextBurst){
            SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.TRAIL_BURST,end,end,1.5);
            cannonSparks(c,end,0.9f);SpellAudio.cannon(c.caster,end,"burst");c.nextBurst+=4;
        }
        return true;
    }
    private static void cannonSparks(Cast c,Vec3 pos,float scale){
        PacketDistributor.sendToPlayersNear(c.level,null,pos.x,pos.y,pos.z,96,new WandNetwork.Burst(pos.x,pos.y,pos.z,c.spell.theme.color,scale));
    }
    private static void beam(Cast c,Vec3 start,Vec3 dir){
        Vec3 end=clipLoaded(c.level,start,dir,c.spell.range,c.caster);double length=start.distanceTo(end);
        SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.LINK,start,end,1);
        for(double d=0;d<=length;d+=0.5){Vec3 point=start.add(dir.scale(d));trail(c.level,point,c.spell.theme,0.04);
        }
        for(var victim:lineTargets(c,start,end,c.spell.radius))
            if(visible(c,start,victim.getBoundingBox().getCenter()))hit(c,victim,start,true);

        burst(c,end);
    }
    static boolean healingAlly(ServerPlayer caster,LivingEntity entity){
        if(entity instanceof BossSpellVisual||!entity.isAlive()||entity.isSpectator())return false;
        if(entity==caster)return true;
        if(entity instanceof TamableAnimal pet)return caster.getUUID().equals(pet.getOwnerUUID());
        return caster.getTeam()!=null&&caster.getTeam()==entity.getTeam()
            ||entity instanceof net.minecraft.world.entity.player.Player&&ArcanaConfig.TEAMLESS_HEALING.get()&&caster.getTeam()==null&&entity.getTeam()==null;
    }
    private static void sanctuary(Cast c){
        Vec3 center=c.caster.position().add(0,.5,0);float amount=12*(c.book==null?1:c.book.power());
        var allies=c.level.getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(c.spell.radius),e->healingAlly(c.caster,e));
        if(!allies.contains(c.caster))allies.add(c.caster);
        for(var ally:allies)if(ally.getBoundingBox().getCenter().distanceToSqr(center)<c.spell.radius*c.spell.radius&&visible(c,c.caster.getEyePosition(),ally.getBoundingBox().getCenter())){
            ally.heal((float)(amount*ArcanaConfig.HEALING.get()));
            for(var debuff:List.of(MobEffects.POISON,MobEffects.WITHER,MobEffects.BLINDNESS,MobEffects.DARKNESS))ally.removeEffect(debuff);
            SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.IMPACT,ally.getBoundingBox().getCenter(),ally.getBoundingBox().getCenter(),1.4);
        }
        SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.AREA,center,center,c.spell.radius);SpellAudio.impact(c.caster,center,c.spell);
    }
    static void area(Cast c,Vec3 pos,double radius,boolean once){
        for(LivingEntity entity:near(c.caster,pos,radius))if(entity.getBoundingBox().getCenter().distanceToSqr(pos)<=radius*radius&&visible(c,pos,entity.getBoundingBox().getCenter()))hit(c,entity,pos,once);
    }
    public static boolean allowed(ServerPlayer caster,Entity entity){return entity.isAlive()&&allegianceAllows(caster,entity);}
    static boolean allegianceAllows(ServerPlayer caster,Entity entity){
        if(entity instanceof BossSpellVisual)return false;
        if(entity==caster||entity.isSpectator()||entity instanceof ArmorStand)return false;
        if(entity instanceof net.minecraft.world.entity.player.Player other&&(!ArcanaConfig.PVP.get()||!caster.server.isPvpAllowed()||!caster.canHarmPlayer(other)))return false;
        if(entity instanceof TamableAnimal tame&&caster.getUUID().equals(tame.getOwnerUUID()))return false;
        var team=caster.getTeam();
        return team==null||team.isAllowFriendlyFire()||entity.getTeam()!=team;
    }
    static List<LivingEntity> lineTargets(Cast c,Vec3 start,Vec3 end,double radius){
        return c.level.getEntitiesOfClass(LivingEntity.class,new AABB(start,end).inflate(radius+1),e->allowed(c.caster,e)&&(e.getBoundingBox().inflate(radius).contains(start)||e.getBoundingBox().inflate(radius).clip(start,end).isPresent()));
    }
    static List<LivingEntity> near(ServerPlayer player,Vec3 pos,double radius){
        return player.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(pos,pos).inflate(radius),e->allowed(player,e)&&e.getBoundingBox().getCenter().distanceToSqr(pos)<(radius+1)*(radius+1));
    }
    static LivingEntity target(ServerPlayer caster,Vec3 start,Vec3 end,double margin){
        LivingEntity result=null;double distance=start.distanceToSqr(end)+1;
        for(var entity:caster.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(start,end).inflate(margin+1),e->allowed(caster,e))){
            AABB box=entity.getBoundingBox().inflate(margin);var point=box.clip(start,end);
            double d=box.contains(start)?0:point.map(start::distanceToSqr).orElse(Double.MAX_VALUE);
            if(d<distance&&caster.serverLevel().clip(new ClipContext(start,entity.getBoundingBox().getCenter(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,caster)).getType()==HitResult.Type.MISS){distance=d;result=entity;}
        }
        return result;
    }
    static Vec3 clipLoaded(ServerLevel level,Vec3 start,Vec3 dir,double range,Entity caster){
        Vec3 end=start;double travelled=0;
        for(double d=.5;d<=range;d+=.5){Vec3 p=start.add(dir.scale(d));if(!level.hasChunkAt(BlockPos.containing(p)))break;end=p;travelled=d;}
        if(range-travelled<.5001){Vec3 exact=start.add(dir.scale(range));if(level.hasChunkAt(BlockPos.containing(exact)))end=exact;}
        return level.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,caster)).getLocation();
    }
    static boolean visible(Cast c,Vec3 start,Vec3 end){return c.level.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,c.caster)).getType()==HitResult.Type.MISS;}
    static void hit(Cast c,LivingEntity victim,Vec3 from,boolean once){
        try(var visibilityScope=SpellParticleScope.enter(c.spell)){
        if(!allowed(c.caster,victim)||(once&&!c.hit.add(victim.getUUID())))return;
        if(c.spell.damage>0){
            float before=victim.getHealth();
            float multiplier=1;
            if(c.spell.kind==Spell.Kind.EXECUTE){
                boolean exploit=c.spell==Spell.CRIMSON_HARVEST?victim.getHealth()<=victim.getMaxHealth()*.35f:c.spell==Spell.MEMORY_FRACTURE?victim.hasEffect(MobEffects.DARKNESS):victim.getTicksFrozen()>=140||(victim.getEffect(MobEffects.MOVEMENT_SLOWDOWN)!=null&&victim.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier()>=2);
                if(exploit)multiplier=1.5f;
            }
            boolean hurt=victim.hurt(c.book==null?c.level.damageSources().indirectMagic(c.caster,c.caster):c.book.source(),(float)((c.spell.damage+c.weaponBonus)*multiplier*ArcanaConfig.DAMAGE.get()*(c.book==null?1:c.book.power())));
            if(hurt){MartialSpells.drain(c,Math.max(0,before-victim.getHealth()));impactFeedback(c,victim.getBoundingBox().getCenter());if(c.spell==Spell.CRIMSON_HARVEST&&!victim.isAlive())SpellActions.heal(c,c.caster,2);}
            if(c.spell.kind==Spell.Kind.SIPHON&&hurt&&victim.getHealth()<before){
                c.caster.heal((float)(Math.min(2.0f*SpellActions.power(c),(before-victim.getHealth())*0.4f)*ArcanaConfig.HEALING.get()));
                Vec3 fromSoul=victim.getBoundingBox().getCenter(),toSoul=c.caster.getEyePosition();
                SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.LINK,fromSoul,toSoul,1);
                for(int i=0;i<=20;i++)trail(c.level,fromSoul.lerp(toSoul,i/20.0),c.spell.theme,0.035);
            }
        }
        if(c.spell.damage==0)impactFeedback(c,victim.getBoundingBox().getCenter());
        if(c.spell==Spell.WITHERING_HEX)effect(victim,MobEffects.WITHER,60,0);
        if(c.spell==Spell.SOUL_PRISON)effect(victim,MobEffects.DARKNESS,60,0);
        if(c.spell.theme==Spell.Theme.ICE&&!GodSpells.handles(c.spell)&&!CavesCompat.optional(c.spell)){effect(victim,MobEffects.MOVEMENT_SLOWDOWN,40,0);victim.setTicksFrozen(Math.min(240,victim.getTicksFrozen()+40));}
        if(c.spell==Spell.BRAMBLE_RING)effect(victim,MobEffects.MOVEMENT_SLOWDOWN,40,1);
        if(c.spell.kind==Spell.Kind.DOMAIN){effect(victim,MobEffects.MOVEMENT_SLOWDOWN,50,3);victim.setTicksFrozen(Math.max(160,victim.getTicksFrozen()));}
        if(c.spell.theme==Spell.Theme.FIRE&&!GodSpells.handles(c.spell)&&!CavesCompat.optional(c.spell))victim.igniteForSeconds(3);
        if(c.spell.theme==Spell.Theme.DARK&&!GodSpells.handles(c.spell)&&!CavesCompat.optional(c.spell)){effect(victim,MobEffects.WEAKNESS,60,0);if(c.spell==Spell.NIGHTFALL)effect(victim,MobEffects.DARKNESS,60,0);}
        if(c.spell.theme==Spell.Theme.POISON&&!GodSpells.handles(c.spell)&&!BossesRiseCompat.optional(c.spell)&&!CavesCompat.optional(c.spell))effect(victim,MobEffects.POISON,80,0);
        switch(c.spell.kind){
            case CONFUSE->{effect(victim,MobEffects.CONFUSION,100,0);effect(victim,MobEffects.BLINDNESS,40,0);}
            case STUN->{effect(victim,MobEffects.MOVEMENT_SLOWDOWN,60,5);effect(victim,MobEffects.WEAKNESS,60,1);}
            case PUSH,PULL->{Vec3 d=victim.position().subtract(c.caster.position()).multiply(1,0,1).normalize().scale(c.spell.kind==Spell.Kind.PULL?-0.9:1.3);victim.setDeltaMovement(d.add(0,0.25,0));victim.hurtMarked=true;}
            case LAUNCH->{victim.setDeltaMovement(victim.getDeltaMovement().add(0,1.1,0));victim.hurtMarked=true;}
            case SWEEP,CANNON,COMET,PULSE,BURST,TWISTER,SMITE->{Vec3 delta=victim.position().subtract(from).multiply(1,0,1).normalize().scale(0.65);victim.push(delta.x,0.3,delta.z);victim.hurtMarked=true;}
            default->{}
        }

        }
    }
    static void effect(LivingEntity e,net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,int ticks,int amp){e.addEffect(new MobEffectInstance(effect,ticks,amp));}
    public static void switchEffect(ServerPlayer player,Spell.Theme theme){
        trail(player.serverLevel(),player.getEyePosition().add(player.getLookAngle().scale(0.6)),theme,0.15);
        player.serverLevel().playSound(null,player.blockPosition(),SoundEvents.UI_BUTTON_CLICK.value(),SoundSource.PLAYERS,0.35F,1.4F);
    }
    static void trail(ServerLevel level,Vec3 pos,Spell.Theme theme,double spread){
        if(visualPoints++>=ArcanaConfig.VISUAL_BUDGET.get())return;
        int color=theme.color;Vector3f rgb=new Vector3f(((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f);
        if(theme==Spell.Theme.BLOOD){
            SpellParticles.send(level,new DustColorTransitionOptions(rgb,new Vector3f(0.19f,0.005f,0.025f),1.25f),pos.x,pos.y,pos.z,ArcanaConfig.PARTICLES.get(),spread,spread,spread,0);
            // Real blood droplets from Iron's, rather than redstone block debris.
            SpellParticles.send(level,SpellParticles.particle(Spell.Theme.BLOOD),pos.x,pos.y,pos.z,1,spread,spread,spread,0.015);
        } else if(theme==Spell.Theme.SOUL){
            SpellParticles.send(level,new DustColorTransitionOptions(rgb,new Vector3f(0.025f,0.12f,0.17f),1.15f),pos.x,pos.y,pos.z,ArcanaConfig.PARTICLES.get(),spread,spread,spread,0);
            SpellParticles.send(level,ParticleTypes.SCULK_SOUL,pos.x,pos.y,pos.z,1,spread,spread,spread,0.012);
        } else {
            SpellParticles.send(level,new DustParticleOptions(rgb,1.1f),pos.x,pos.y,pos.z,ArcanaConfig.PARTICLES.get(),spread,spread,spread,0);
            var accent=switch(theme){case FIRE->ParticleTypes.FLAME;case DARK->ParticleTypes.SOUL;case GRAVITY->ParticleTypes.REVERSE_PORTAL;case POISON->ParticleTypes.SPORE_BLOSSOM_AIR;case THUNDER->ParticleTypes.ELECTRIC_SPARK;default->ParticleTypes.ENCHANT;};
            SpellParticles.send(level,accent,pos.x,pos.y,pos.z,1,spread,spread,spread,0.005);
        }
    }

    private static void waveVisual(Cast c,Vec3 center){
        Vec3 side=c.dir.cross(new Vec3(0,1,0));if(side.lengthSqr()<0.01)side=new Vec3(1,0,0);side=side.normalize();
        for(int i=-4;i<=4;i++){double t=i/4.0;Vec3 p=center.add(side.scale(t*c.spell.radius)).add(0,(1-t*t)*0.3,0);trail(c.level,p,c.spell.theme,0.05);}
    }
    static void ring(ServerLevel level,Vec3 center,double radius,Spell.Theme theme,double offset){
        int count=Math.min(48,Math.max(12,(int)(radius*10)));
        for(int i=0;i<count;i++){double a=i*Math.PI*2/count+offset;trail(level,center.add(Math.cos(a)*radius,0,Math.sin(a)*radius),theme,0.015);}
    }
    static void impactFeedback(Cast c,Vec3 pos){
        if(c.spell.kind==Spell.Kind.CANNON||RecordedSpells.handles(c.spell))return;
        // One layered hit per cast every eight ticks, including area and chain attacks.
        if(ticks-c.lastImpactTick<8)return;
        if(c.spell.kind!=Spell.Kind.METEOR&&c.spell.kind!=Spell.Kind.RAIL&&c.spell.kind!=Spell.Kind.COLLAPSE&&c.spell.kind!=Spell.Kind.DOOM)SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.HIT,pos,pos,0.65);
        c.lastImpactTick=ticks;if(!GodSpells.handles(c.spell))SpellAudio.impact(c.caster,pos,c.spell);
    }
    static void burst(Cast c,Vec3 pos){
        try(var visibilityScope=SpellParticleScope.enter(c.spell)){
        ServerLevel level=c.level;Spell.Theme theme=c.spell.theme;
        SpellVisuals.send(level,c.spell,SpellVisuals.Stage.IMPACT,pos,pos,Math.clamp(c.spell.radius,1.2,5));
        if(theme==Spell.Theme.SOUL)PacketDistributor.sendToPlayersNear(level,null,pos.x,pos.y,pos.z,64,new WandNetwork.Burst(pos.x,pos.y,pos.z,theme.color));
        for(int i=0;i<12;i++){double a=i*Math.PI/6;trail(level,pos.add(Math.cos(a)*0.4,0.25,Math.sin(a)*0.4),theme,0.12);}
        if(theme==Spell.Theme.SOUL)ring(level,pos,1.25,theme,0);
        if(theme==Spell.Theme.BLOOD)SpellParticles.send(level,SpellParticles.particle(Spell.Theme.BLOOD),pos.x,pos.y,pos.z,24,0.3,0.3,0.3,0.18);
        if(theme==Spell.Theme.SOUL){
            SpellParticles.send(level,new BlockParticleOption(ParticleTypes.BLOCK,Blocks.AMETHYST_BLOCK.defaultBlockState()),pos.x,pos.y,pos.z,20,0.25,0.25,0.25,0.15);
            SpellParticles.send(level,ParticleTypes.SCULK_CHARGE_POP,pos.x,pos.y,pos.z,8,0.3,0.3,0.3,0.025);
            SpellParticles.send(level,new ShriekParticleOption(0),pos.x,pos.y,pos.z,1,0,0,0,0);
        }
        impactFeedback(c,pos);

        }
    }

    private static void lightning(Cast c,Vec3 pos){
        ServerLevel level=c.level;Spell.Theme theme=c.spell.theme;
        Vec3 last=pos.add(0,16,0);
        SpellVisuals.send(level,c.spell,SpellVisuals.Stage.STRIKE,last,pos,1);
        for(int i=1;i<=32;i++){Vec3 next=pos.add(Math.sin(i*2.31)*0.55,16-i*0.5,Math.cos(i*1.7)*0.55);for(int j=0;j<3;j++)trail(level,last.lerp(next,j/3.0),theme,0.02);last=next;}
        burst(c,pos);
    }
    private static void chain(Cast c,LivingEntity first){
        LivingEntity victim=first;Vec3 from=BeamRitual.origin(c.caster);
        for(int jump=0;jump<5&&victim!=null;jump++){
            Vec3 end=victim.getBoundingBox().getCenter();
            SpellVisuals.send(c.level,c.spell,SpellVisuals.Stage.LINK,from,end,1);
            double length=from.distanceTo(end);int steps=Math.min(96,Math.max(2,(int)(length*4)));
            for(int i=0;i<=steps;i++){double t=i/(double)steps;trail(c.level,from.lerp(end,t).add(Math.sin(i*2.4)*0.12,Math.cos(i*1.7)*0.12,0),c.spell.theme,0.015);}
            hit(c,victim,end,true);burst(c,end);from=end;
            LivingEntity next=null;double closest=c.spell.radius*c.spell.radius;
            for(var candidate:near(c.caster,end,c.spell.radius)){
                double d=candidate.getBoundingBox().getCenter().distanceToSqr(end);
                if(!c.hit.contains(candidate.getUUID())&&d<closest&&visible(c,end,candidate.getBoundingBox().getCenter())){next=candidate;closest=d;}
            }
            victim=next;
        }
    }
    private static void message(ServerPlayer player,Component text){player.displayClientMessage(text,true);}
    private static final class VolleyBolt {
        Vec3 pos;final Vec3 dir;double distance;
        VolleyBolt(Vec3 pos,Vec3 dir){this.pos=pos;this.dir=dir;}
    }
    static final class Cast {
        final UUID owner;final ServerLevel level;final Spell spell;final Vec3 dir;final LivingEntity target;final Set<UUID> hit=new HashSet<>();final List<VolleyBolt> bolts=new ArrayList<>();
        final List<SpellActions.Shot> shots=new ArrayList<>();
        final List<Entity> centrepieces=new ArrayList<>();
        final List<Entity> bossVisuals=new ArrayList<>();final List<Vec3> eruptionPoints=new ArrayList<>();
        final List<java.util.UUID> taunted=new ArrayList<>();float weaponBonus,drained;
        AdvancedSpells.State advanced;GodSpells.State god;AngelScream.State scream;ExpansionSpells.State expansion;ExpansionSpells.PrismRoute prism;
        BossesRiseCompat.State bossState;CavesSpells.State cave;
        BookCast book;final ServerPlayer testOwner;ServerPlayer caster;Vec3 pos,anchor,travel;int age,duration,impactAge;double distance,nextBurst=4;long lastImpactTick=-1000;
        Cast(ServerPlayer player,Spell spell,Vec3 start,Vec3 dir,Vec3 aim,LivingEntity target){
            owner=player.getUUID();level=player.serverLevel();caster=player;testOwner=player;this.spell=spell;pos=start;this.dir=dir;travel=dir;anchor=aim;this.target=target;
        }
    }
}

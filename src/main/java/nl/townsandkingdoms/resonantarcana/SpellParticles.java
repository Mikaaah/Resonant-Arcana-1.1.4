package nl.townsandkingdoms.resonantarcana;

import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Bounded native particle helpers for vanilla and Iron's effects. */
final class SpellParticles {
    private static int remaining;
    private static long frame;
    private SpellParticles() {}
    static void send(ServerLevel level, ParticleOptions particle, double x, double y, double z, int count, double dx, double dy, double dz, double speed) {
        var packet = new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket(particle, false, x, y, z, (float)dx, (float)dy, (float)dz, (float)speed, count);
        var message = new WandNetwork.Particles(packet, SpellParticleScope.currentId());
        Vec3 center = new Vec3(x,y,z);
        for (var player : level.players()) if (player.blockPosition().closerToCenterThan(center,32))
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,message);
    }
    // Existing callers remain compatible; exact source identity is carried by the scope.
    static void send(ServerLevel level, boolean lightning, ParticleOptions particle, double x, double y, double z, int count, double dx, double dy, double dz, double speed) {
        send(level,particle,x,y,z,count,dx,dy,dz,speed);
    }
    static void tick(){remaining=ArcanaConfig.NATIVE_BUDGET.get();frame++;}
    static void point(ServerLevel level,ParticleOptions particle,Vec3 pos,Vec3 velocity){
        if(remaining<=0)return;remaining--;
        send(level,particle,pos.x,pos.y,pos.z,0,velocity.x,velocity.y,velocity.z,1);
    }
    static void cannonCharge(ServerLevel level,Vec3 center,Vec3 direction,double progress,int age){
        Vec3 side=direction.cross(new Vec3(0,1,0));if(side.lengthSqr()<.01)side=new Vec3(1,0,0);side=side.normalize();
        Vec3 up=side.cross(direction).normalize();
        var blue=new DustColorTransitionOptions(new Vector3f(.05f,.25f,1),new Vector3f(.05f,.95f,1),1.25f);
        for(int arm=0;arm<2;arm++)for(int i=0;i<5;i++){
            double t=i/5.0,angle=age*.28+arm*Math.PI+t*1.4,radius=(1.2-.75*progress)*(1-.4*t);
            Vec3 radial=side.scale(Math.cos(angle)*radius).add(up.scale(Math.sin(angle)*radius));
            Vec3 pos=center.add(radial).subtract(direction.scale((1-t)*.9));
            point(level,blue,pos,Vec3.ZERO);
            if(i==4)point(level,ParticleRegistry.SOUL_FIRE_PARTICLE.get(),pos,radial.scale(-.045).add(direction.scale(.025)));
        }
        point(level,ParticleTypes.SCULK_CHARGE_POP,center,Vec3.ZERO);
    }
    static Vec3 chargeOffset(Spell.Theme theme,double a,double progress){
        double r=1.8-progress;
        return switch(theme){
            case HOLY -> new Vec3(Math.cos(a)*r*.7,-.8+progress,Math.sin(a)*r*.7);
            case ICE -> new Vec3(Math.cos(a)*r,-.6,Math.sin(a)*r);
            case FIRE -> new Vec3(Math.cos(a)*r,-1.2+progress,Math.sin(a)*r*.45);
            case BLOOD -> new Vec3(Math.sin(a)*.8,1.3-progress*1.5,Math.cos(a)*.35);
            case POISON -> new Vec3(Math.cos(a)*r*.7,-.7,Math.sin(a)*r*.7);
            case THUNDER -> new Vec3(Math.sin(a*3)*r,Math.cos(a*5)*.65,0);
            case DARK -> new Vec3(Math.sin(a)*.4,Math.cos(a)*r,Math.sin(a*2)*.6);
            case GRAVITY -> new Vec3(Math.cos(a)*r,Math.sin(a)*r,Math.sin(a)*r*.3);
            case SOUL -> new Vec3(Math.cos(a)*r,Math.sin(a*2)*.55,Math.sin(a)*r);
            case MAGIC -> new Vec3(Math.cos(a)*r*.7,Math.sin(a)*r*.7,-.4);
        };
    }
    static ParticleOptions particle(Spell.Theme theme){return switch(theme){
        case HOLY -> ParticleRegistry.CLEANSE_PARTICLE.get();
        case ICE -> ParticleRegistry.SNOWFLAKE_PARTICLE.get();
        case FIRE -> ParticleRegistry.EMBER_PARTICLE.get();
        case BLOOD -> ParticleRegistry.BLOOD_PARTICLE.get();
        case POISON -> ParticleRegistry.ACID_BUBBLE_PARTICLE.get();
        case THUNDER -> ParticleRegistry.ELECTRICITY_PARTICLE.get();
        case DARK -> ParticleRegistry.DRAGON_FIRE_PARTICLE.get();
        case GRAVITY -> ParticleRegistry.UNSTABLE_ENDER_PARTICLE.get();
        case SOUL -> ParticleRegistry.SOUL_FIRE_PARTICLE.get();
        case MAGIC -> ParticleRegistry.WISP_PARTICLE.get();
    };}
    static void emit(ServerLevel level,Spell spell,SpellVisuals.Stage stage,Vec3 pos,Vec3 end,double size){
        try(var visibilityScope=SpellParticleScope.enter(spell)){
        // Soul Cannon retains its existing sequence. All particle work has a global per-tick cap.
        if(GodSpells.handles(spell)||spell==Spell.ARCANE_DEVASTATOR||CavesCompat.optional(spell)||spell==Spell.VOID_DOMAIN||spell==Spell.VOID_EXILE||spell==Spell.DOOM_BEAM||spell==Spell.DOOM||spell.kind==Spell.Kind.CATACLYSM||spell==Spell.SOUL_CANNON||remaining<=0)return;
        boolean impact=stage==SpellVisuals.Stage.IMPACT;
        boolean link=stage==SpellVisuals.Stage.LINK||stage==SpellVisuals.Stage.STRIKE;
        int count=Math.min(remaining,impact?20:link?16:6);remaining-=count;
        Vec3 dir=end.subtract(pos).normalize();
        Vec3 side=dir.cross(new Vec3(0,1,0));if(side.lengthSqr()<.01)side=new Vec3(1,0,0);side=side.normalize();
        Vec3 up=side.cross(dir).normalize();
        for(int i=0;i<count;i++){
            double t=(i+.5)/count,a=t*Math.PI*2+frame*.25,r=Math.min(4,size)*(impact?.2: .35);
            Vec3 p=link?pos.lerp(end,t):pos;
            Vec3 v;
            switch(spell.theme){
                case HOLY -> {p=p.add(Math.cos(a)*r,t*.8,Math.sin(a)*r);v=new Vec3(0,.06,0);}
                case ICE -> {p=p.add(Math.cos(a)*r,.3,Math.sin(a)*r);v=new Vec3(Math.cos(a)*.05,-.02,Math.sin(a)*.05);}
                case FIRE -> {p=p.add(Math.cos(a)*r,-.2,Math.sin(a)*r);v=new Vec3(Math.cos(a)*.025,.07+t*.09,Math.sin(a)*.025);}
                case BLOOD -> {p=p.add(Math.cos(a)*r,.25,Math.sin(a)*r);v=new Vec3(Math.cos(a)*.08,.03+t*.06,Math.sin(a)*.08);}
                case POISON -> {p=p.add(Math.cos(a)*r,.15+t*.35,Math.sin(a)*r);v=new Vec3(0,.012,0);}
                case THUNDER -> {p=p.add(side.scale(Math.sin(i*2.7+frame)*r)).add(up.scale(Math.cos(i*4.1)*r));v=dir.scale(.15);}
                case DARK -> {p=p.add(Math.sin(a)*r,t*.8-.4,Math.cos(a)*r);v=new Vec3(0,.03,0);}
                case GRAVITY -> {Vec3 off=new Vec3(Math.cos(a)*r,Math.sin(a)*r,Math.sin(a*2)*r*.4);p=p.add(off);v=off.scale(-.16);}
                case SOUL -> {p=p.add(Math.cos(a)*r,Math.sin(a)*r,0);v=new Vec3(0,.025,0);}
                case MAGIC -> {p=p.add(side.scale(Math.cos(a)*r)).add(up.scale(Math.sin(a)*r));v=dir.scale(.07);}
                default -> throw new IllegalStateException();
            }
            if(spell.kind==Spell.Kind.RAIN&&stage==SpellVisuals.Stage.AREA){p=pos.add(Math.cos(a)*size*.8,3+t,Math.sin(a)*size*.8);v=new Vec3(0,-.3,0);}
            send(level,particle(spell.theme),p.x,p.y,p.z,0,v.x,v.y,v.z,1);
        }
        if(impact&&remaining>0){
            remaining--;
            int c=spell.theme.color;
            var rgb=new Vector3f(((c>>16)&255)/255f,((c>>8)&255)/255f,(c&255)/255f);
            if(spell.theme==Spell.Theme.FIRE||spell.theme==Spell.Theme.THUNDER)
                send(level,new BlastwaveParticleOptions(rgb,(float)Math.min(12,size*2)),pos.x,pos.y,pos.z,1,0,0,0,0);
            if(spell.theme==Spell.Theme.BLOOD)
                send(level,ParticleRegistry.BLOOD_GROUND_PARTICLE.get(),pos.x,pos.y-.3,pos.z,1,0,0,0,0);
            if(spell.theme==Spell.Theme.GRAVITY)
                send(level,ParticleTypes.REVERSE_PORTAL,pos.x,pos.y,pos.z,12,.8,.8,.8,.08);
        }

        }
    }
}

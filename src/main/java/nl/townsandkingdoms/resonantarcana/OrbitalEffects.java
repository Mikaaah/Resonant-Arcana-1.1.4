package nl.townsandkingdoms.resonantarcana;

import java.util.*;
import com.mojang.blaze3d.vertex.*;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.render.SpellRenderingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Caster-scoped orb and orbital beam; all geometry and clouds are purely cosmetic. */
@EventBusSubscriber(modid=ResonantArcana.ID,value=Dist.CLIENT)
final class OrbitalEffects {
    private static final class State {
        Vec3 point=Vec3.ZERO,previous=Vec3.ZERO,target=Vec3.ZERO,mark=Vec3.ZERO,top=Vec3.ZERO,blast;
        float size;SpellVisuals.Stage stage;long expires,updated,blastTime;boolean nativeCloud;
    }
    private static final Map<Integer,State> CASTS=new HashMap<>();private static ClientLevel world;
    static void handle(WandNetwork.OrbitalFocus cue){
        if(!cue.valid())return;var l=Minecraft.getInstance().level;if(l==null)return;
        if(l!=world){CASTS.clear();world=l;}if(CASTS.size()>=16&&!CASTS.containsKey(cue.caster()))return;
        var msg=cue.visual();var stage=SpellVisuals.Stage.values()[msg.stage()];var state=CASTS.computeIfAbsent(cue.caster(),id->new State());
        Vec3 p=new Vec3(msg.x(),msg.y(),msg.z()),end=new Vec3(msg.endX(),msg.endY(),msg.endZ());
        if(stage==SpellVisuals.Stage.RITUAL_END){CASTS.remove(cue.caster());return;}
        if(stage==SpellVisuals.Stage.RITUAL_TARGET){state.target=p;state.mark=p;state.top=end;return;}
        if(stage==SpellVisuals.Stage.IMPACT){state.blast=p;state.blastTime=l.getGameTime();state.nativeCloud=CavesCompat.available();if(state.nativeCloud)CavesClientEffects.mushroom(l,p);return;}
        if(stage==SpellVisuals.Stage.CHARGE||stage==SpellVisuals.Stage.FLIGHT)state.mark=end;
        state.previous=state.stage==stage?position(state,l.getGameTime()):p;state.point=p;state.target=end;state.size=msg.size();state.stage=stage;state.updated=l.getGameTime();state.expires=l.getGameTime()+9;
    }
    // Used for archive previews as well as the owned live-cast payload.
    static void visual(ClientLevel l,SpellVisuals.Stage stage,Vec3 p,Vec3 end,double size){
        try (var visibilityScope = FirstPersonEffects.scope(Spell.ORBITAL_RAY)) {if(stage==SpellVisuals.Stage.IMPACT)cloud(l,p,0,false);
        }
    }
    private static Vec3 position(State state,double now){return state.previous.lerp(state.point,Math.clamp((now-state.updated)/2,0,1));}
    static void tick(){
        try (var visibilityScope = FirstPersonEffects.scope(Spell.ORBITAL_RAY)) {
        var l=Minecraft.getInstance().level;if(l!=world){CASTS.clear();world=l;}if(l==null||Minecraft.getInstance().isPaused())return;
        var it=CASTS.entrySet().iterator();while(it.hasNext()){
            var entry=it.next();var s=entry.getValue();var owner=l.getEntity(entry.getKey());long now=l.getGameTime();
            if(owner==null||!owner.isAlive()||(s.expires<now&&(s.blast==null||now-s.blastTime>100))){it.remove();continue;}
            if(s.blast!=null)cloud(l,s.blast,(int)(now-s.blastTime),s.nativeCloud);
            if(now>s.expires)continue;
            if(now%4==0)for(int i=0;i<64;i++){double a=i*Math.PI/32;Vec3 off=new Vec3(Math.cos(a)*16,.05,Math.sin(a)*16);if(s.stage==SpellVisuals.Stage.CHARGE)chargeMote(l,s.mark.add(off),Vec3.ZERO,i,12);else mote(l,s.mark.add(off),Vec3.ZERO,i,12);}
            if(s.stage==SpellVisuals.Stage.CHARGE||s.stage==SpellVisuals.Stage.FLIGHT){
                Vec3 center=position(s,now);double time=now*.13;
                if(s.stage==SpellVisuals.Stage.CHARGE)for(int arm=0;arm<8;arm++)for(int i=0;i<16;i++){
                    double t=i/15.0,y=1-2*(arm+.5)/8,a=time+t*7+arm*2.39996,r=Math.sqrt(1-y*y)*(s.size+(1-t)*4.5);
                    Vec3 off=new Vec3(Math.cos(a)*r,y*(s.size+(1-t)*4.5),Math.sin(a)*r);
                    chargeMote(l,center.add(off),off.scale(-.09),i+arm,13);
                }
                for(int ring=0;ring<3;ring++)for(int i=0;i<32;i++){double a=i*Math.PI/16+time*(ring%2==0?1:-1),r=s.size*1.1;Vec3 q=switch(ring){case 0->new Vec3(Math.cos(a)*r,Math.sin(a)*r,0);case 1->new Vec3(0,Math.cos(a)*r,Math.sin(a)*r);default->new Vec3(Math.cos(a)*r,0,Math.sin(a)*r);};if(s.stage==SpellVisuals.Stage.CHARGE)chargeMote(l,center.add(q),Vec3.ZERO,i+ring,10);else mote(l,center.add(q),Vec3.ZERO,i+ring,10);}
            }
            if(s.stage==SpellVisuals.Stage.STRIKE&&now%2==0){
                Vec3 axis=s.target.subtract(s.point).normalize();for(int i=0;i<90;i++){double t=i/89.0,a=now*.3+t*34;Vec3 q=s.point.lerp(s.target,t).add(Math.cos(a)*1.3,0,Math.sin(a)*1.3);mote(l,q,axis.scale(.3),i,9);if(i%15==0)NativeSpellEffects.point(l,ParticleTypes.SCULK_SOUL,q,axis.scale(.08),18,false);}
            }
        }

        }
    }
    private static void mote(ClientLevel l,Vec3 p,Vec3 v,int i,int life){NativeSpellEffects.tinted(l,i%3==0?ParticleRegistry.ELECTRICITY_PARTICLE.get():ParticleRegistry.WISP_PARTICLE.get(),p,v,life,i%2==0?0x2aabc9:0xb34dff,.8f);}
    private static void chargeMote(ClientLevel l,Vec3 p,Vec3 v,int i,int life){NativeSpellEffects.tinted(l,i%3==0?ParticleRegistry.ELECTRICITY_PARTICLE.get():ParticleRegistry.WISP_PARTICLE.get(),p,v,life,i%2==0?0x9239ed:0xc281ff,.8f);}
    private static void cloud(ClientLevel l,Vec3 p,int age,boolean nativeCloud){
        if(age<0||age>100)return;double progress=age/100.0;
        if(age<24)for(int i=0;i<36;i++){double a=i*Math.PI/18,r=(age+1)*.8;Vec3 v=new Vec3(Math.cos(a),.035,Math.sin(a));mote(l,p.add(v.scale(r)),v.scale(.16),i,16);}
        if(nativeCloud||age%3!=0)return;
        double height=Math.min(16,2+age*.2),cap=2+progress*9;
        // Visible mushroom stem and curling cap, without any spawned nuclear bomb entity.
        for(int i=0;i<48;i++){
            double t=i/47.0,a=i*2.39996+age*.08;Vec3 stem=p.add(Math.cos(a)*(1+t),t*height,Math.sin(a)*(1+t));
            NativeSpellEffects.point(l,i%4==0?ParticleTypes.FLAME:ParticleTypes.LARGE_SMOKE,stem,new Vec3(0,.12,0),30,false);
            double y=1-2*(i+.5)/48,r=Math.sqrt(1-y*y);Vec3 q=p.add(Math.cos(a)*r*cap,height+y*cap*.45,Math.sin(a)*r*cap);
            NativeSpellEffects.point(l,age<24&&i%3==0?ParticleTypes.FLAME:ParticleTypes.CAMPFIRE_COSY_SMOKE,q,new Vec3(Math.cos(a)*.04,.06,Math.sin(a)*.04),32,false);
        }
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES||world==null||CASTS.isEmpty())return;
        var mc=Minecraft.getInstance();if(mc.level!=world)return;var buffers=mc.renderBuffers().bufferSource();Vec3 camera=event.getCamera().getPosition();float partial=event.getPartialTick().getGameTimeDeltaPartialTick(false);double now=world.getGameTime()+partial;
        for(var s:CASTS.values()){
            if(now>s.expires)continue;
            if(s.stage==SpellVisuals.Stage.CHARGE||s.stage==SpellVisuals.Stage.FLIGHT){
                var pose=new PoseStack();Vec3 p=position(s,now).subtract(camera);pose.translate(p.x,p.y,p.z);var type=RenderType.lightning();var v=buffers.getBuffer(type);
                sphere(v,pose.last().pose(),s.size*.75,185,60,255,145);sphere(v,pose.last().pose(),s.size*.5,195,105,255,235);sphere(v,pose.last().pose(),s.size*.28,228,180,255,255);buffers.endBatch(type);
            }else if(s.stage==SpellVisuals.Stage.STRIKE){
                Vec3 delta=s.target.subtract(s.point);float length=(float)delta.length();if(length<.01)continue;var pose=new PoseStack();Vec3 p=s.point.subtract(camera);pose.translate(p.x,p.y,p.z);pose.mulPose(new Quaternionf().rotationTo(new Vector3f(0,0,1),delta.normalize().toVector3f()));
                var type=RenderType.entityTranslucentCull(SpellRenderingHelper.BEACON);var v=buffers.getBuffer(type);float scroll=-(float)now*.1f;
                BeamRenderer.square(pose,v,.65f,0,length,215,150,255,255,scroll,1f/5);
                BeamRenderer.square(pose,v,1.2f,0,length,155,65,240,190,-scroll,1f/6);
                BeamRenderer.square(pose,v,2,0,length,170,60,255,125,scroll*.7f,1f/7);buffers.endBatch(type);
            }
        }
    }
    private static void sphere(VertexConsumer v,Matrix4f m,double radius,int red,int green,int blue,int alpha){
        for(int lat=0;lat<10;lat++)for(int lon=0;lon<20;lon++)for(int corner=0;corner<4;corner++){
            double a=-Math.PI/2+(lat+(corner==1||corner==2?1:0))*Math.PI/10,b=(lon+(corner>=2?1:0))*Math.PI/10,r=Math.cos(a)*radius;
            v.addVertex(m,(float)(Math.cos(b)*r),(float)(Math.sin(a)*radius),(float)(Math.sin(b)*r)).setColor(red,green,blue,alpha);
        }
    }
    private OrbitalEffects(){}
}

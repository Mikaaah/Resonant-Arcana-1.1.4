package nl.townsandkingdoms.resonantarcana;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.*;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Helvar's palette is black iron, red embers and warm sparks, including its expanding shockwave. */
final class HelvarEffects {
    private record Wave(ClientLevel level,Vec3 point,double radius,int age,Spell spell){}
    private static final List<Wave> WAVES=new ArrayList<>();private static ClientLevel world;
    private static final ParticleOptions RED=new DustColorTransitionOptions(new Vector3f(.85f,.035f,.01f),new Vector3f(1,.55f,.1f),1.3f);
    static boolean handles(Spell s){return s.kind==Spell.Kind.BOSS_RIFT||s.kind==Spell.Kind.BOSS_SWORD;}
    static void tick(){var level=Minecraft.getInstance().level;if(world!=level){WAVES.clear();world=level;}if(level==null||Minecraft.getInstance().isPaused())return;for(var it=WAVES.listIterator();it.hasNext();){var wave=it.next();if(wave.age()>20){it.remove();continue;}try(var scope=FirstPersonEffects.scope(wave.spell())){ring(level,wave.point(),wave.radius()*(wave.age()+1)/21.0,18);}it.set(new Wave(level,wave.point(),wave.radius(),wave.age()+1,wave.spell()));}}
    private static void ring(ClientLevel level,Vec3 point,double radius,int life){int count=(int)Math.clamp(radius*12,32,120);for(int i=0;i<count;i++){double a=i*Math.PI*2/count;NativeSpellEffects.point(level,RED,point.add(Math.cos(a)*radius,.08,Math.sin(a)*radius),Vec3.ZERO,life,true);if(i%8==0)NativeSpellEffects.point(level,ParticleTypes.ASH,point.add(Math.cos(a)*radius,.25,Math.sin(a)*radius),new Vec3(0,.08,0),life,false);}}
    static void visual(ClientLevel level,Spell spell,SpellVisuals.Stage stage,Vec3 point,Vec3 end,double size){
        if(stage==SpellVisuals.Stage.RITUAL_END)return;if(stage==SpellVisuals.Stage.RITUAL_TARGET||stage==SpellVisuals.Stage.AREA){ring(level,point,size,12);return;}
        if(stage==SpellVisuals.Stage.IMPACT||stage==SpellVisuals.Stage.HIT){if(spell.kind==Spell.Kind.BOSS_SWORD&&WAVES.size()<16)WAVES.add(new Wave(level,point,spell.radius,0,spell));for(int i=0;i<64;i++){double a=i*2.39996;Vec3 v=new Vec3(Math.cos(a)*.2,.03+(i%7)*.035,Math.sin(a)*.2);NativeSpellEffects.point(level,i%4==0?ParticleTypes.FLAME:RED,point.add(v.scale(2)),v,18,false);}return;}
        Vec3 dir=end.subtract(point).normalize();if(dir.lengthSqr()<.01)dir=new Vec3(0,0,1);double time=level.getGameTime()*.2;
        for(int i=0;i<30;i++){double t=i/30.0;Vec3 q=point.subtract(dir.scale(t*1.5)).add(Math.sin(time+i)*.15,Math.cos(time+i)*.2,0);NativeSpellEffects.point(level,i%5==0?ParticleTypes.SMOKE:RED,q,dir.scale(-.025),12,false);}
    }
    private HelvarEffects(){}
}

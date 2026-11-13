package nl.townsandkingdoms.resonantarcana;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class WandNetwork {
    private WandNetwork() {}
    /** Spell identity allows each client to choose its own visibility policy. */
    public record Particles(net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket particle, int spell) implements CustomPacketPayload {
        public Particles(net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket particle, boolean lightning) { this(particle, lightning ? Spell.LIGHTNING.ordinal() : -1); }
        public boolean lightning() { return spell >= 0 && spell < Spell.values().length && Spell.values()[spell].theme == Spell.Theme.THUNDER; }
        public static final Type<Particles> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(ResonantArcana.ID,"particles"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Particles> CODEC=StreamCodec.of((b,m)->{
            net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket.STREAM_CODEC.encode(b,m.particle());b.writeVarInt(m.spell());
        },b->new Particles(net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket.STREAM_CODEC.decode(b),b.readVarInt()));
        @Override public Type<Particles> type(){return TYPE;}
        boolean valid(){return spell>=-1&&spell<Spell.values().length&&particle!=null&&particle.getCount()>=0&&particle.getCount()<=1024
            &&Double.isFinite(particle.getX())&&Double.isFinite(particle.getY())&&Double.isFinite(particle.getZ())
            &&Math.abs(particle.getX())<=30_000_000&&Math.abs(particle.getY())<=30_000_000&&Math.abs(particle.getZ())<=30_000_000
            &&Float.isFinite(particle.getXDist())&&Float.isFinite(particle.getYDist())&&Float.isFinite(particle.getZDist())&&Float.isFinite(particle.getMaxSpeed());}
    }
    public record Action(int action) implements CustomPacketPayload {
        public static final Type<Action> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(ResonantArcana.ID,"action"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Action> CODEC=StreamCodec.of((buf,msg)->buf.writeVarInt(msg.action),buf->new Action(buf.readVarInt()));
        @Override public Type<Action> type(){return TYPE;}
    }
    public record Chant(int caster,int spell,boolean start,float pitch,float gain) implements CustomPacketPayload {
        public static final Type<Chant> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(ResonantArcana.ID,"chant"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Chant> CODEC=StreamCodec.of((b,m)->{b.writeVarInt(m.caster);b.writeVarInt(m.spell);b.writeBoolean(m.start);b.writeFloat(m.pitch);b.writeFloat(m.gain);},b->new Chant(b.readVarInt(),b.readVarInt(),b.readBoolean(),b.readFloat(),b.readFloat()));
        @Override public Type<Chant> type(){return TYPE;}
        boolean valid(){return caster>=0&&spell>=0&&spell<Spell.values().length&&RecordedSpells.handles(Spell.values()[spell])&&Float.isFinite(pitch)&&pitch>=.5f&&pitch<=2&&Float.isFinite(gain)&&gain>=0&&gain<=8;}
    }
    /** Caster-owned visual lease; geometry follows the interpolated entity, never old world coordinates. */
    public record BeamFocus(int caster,int spell,int phase,float progress,float length) implements CustomPacketPayload {
        public BeamFocus(int caster,int phase,float progress,float length){this(caster,Spell.DOOM_BEAM.ordinal(),phase,progress,length);}
        public static final Type<BeamFocus> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(ResonantArcana.ID,"beam_focus"));
        public static final StreamCodec<RegistryFriendlyByteBuf,BeamFocus> CODEC=StreamCodec.of((b,m)->{b.writeVarInt(m.caster);b.writeVarInt(m.spell);b.writeByte(m.phase);b.writeFloat(m.progress);b.writeFloat(m.length);},b->new BeamFocus(b.readVarInt(),b.readVarInt(),b.readUnsignedByte(),b.readFloat(),b.readFloat()));
        @Override public Type<BeamFocus> type(){return TYPE;}
        boolean valid(){return caster>=0&&spell>=0&&spell<Spell.values().length&&Spell.values()[spell].kind==Spell.Kind.DEVASTATOR&&phase>=0&&phase<=2&&Float.isFinite(progress)&&progress>=0&&progress<=1&&Float.isFinite(length)&&length>=0&&length<=64;}
    }
    public record Loop(int caster,int spell,boolean play,boolean release,int variant,float progress) implements CustomPacketPayload {
        public Loop(int caster,int spell,boolean play,boolean release,int variant){this(caster,spell,play,release,variant,0);}
        public static final Type<Loop> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(ResonantArcana.ID,"spell_loop"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Loop> CODEC=StreamCodec.of((b,m)->{b.writeVarInt(m.caster);b.writeVarInt(m.spell);b.writeBoolean(m.play);b.writeBoolean(m.release);b.writeByte(m.variant);b.writeFloat(m.progress);},b->new Loop(b.readVarInt(),b.readVarInt(),b.readBoolean(),b.readBoolean(),b.readUnsignedByte(),b.readFloat()));
        @Override public Type<Loop> type(){return TYPE;}
        boolean valid(){return caster>=0&&spell>=0&&spell<Spell.values().length&&SpellLoops.supports(Spell.values()[spell])&&variant>=0&&variant<=2&&Float.isFinite(progress)&&progress>=0&&progress<=1;}
    }
    public record OrbitalFocus(int caster,Visual visual) implements CustomPacketPayload {
        public static final Type<OrbitalFocus> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(ResonantArcana.ID,"orbital_focus"));
        public static final StreamCodec<RegistryFriendlyByteBuf,OrbitalFocus> CODEC=StreamCodec.of((b,m)->{b.writeVarInt(m.caster);Visual.CODEC.encode(b,m.visual);},b->new OrbitalFocus(b.readVarInt(),Visual.CODEC.decode(b)));
        @Override public Type<OrbitalFocus> type(){return TYPE;}
        boolean valid(){return caster>=0&&visual!=null&&visual.valid()&&visual.spell()==Spell.ORBITAL_RAY.ordinal();}
    }
    public record RhoFocus(int caster,float layers,Visual visual) implements CustomPacketPayload {
        public static final Type<RhoFocus> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(ResonantArcana.ID,"rho_aias_focus"));
        public static final StreamCodec<RegistryFriendlyByteBuf,RhoFocus> CODEC=StreamCodec.of((b,m)->{b.writeVarInt(m.caster);b.writeFloat(m.layers);Visual.CODEC.encode(b,m.visual);},b->new RhoFocus(b.readVarInt(),b.readFloat(),Visual.CODEC.decode(b)));
        @Override public Type<RhoFocus> type(){return TYPE;}
        boolean valid(){return caster>=0&&Float.isFinite(layers)&&layers>=0&&layers<=3&&visual!=null&&visual.valid()&&visual.spell()==Spell.RHO_AIAS.ordinal()&&new net.minecraft.world.phys.Vec3(visual.x(),visual.y(),visual.z()).distanceToSqr(new net.minecraft.world.phys.Vec3(visual.endX(),visual.endY(),visual.endZ()))>.8&&new net.minecraft.world.phys.Vec3(visual.x(),visual.y(),visual.z()).distanceToSqr(new net.minecraft.world.phys.Vec3(visual.endX(),visual.endY(),visual.endZ()))<1.2;}
    }
    public record Select(ResourceLocation wand,int slot) implements CustomPacketPayload {
        public static final Type<Select> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(ResonantArcana.ID,"select"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Select> CODEC=StreamCodec.of((b,m)->{b.writeResourceLocation(m.wand);b.writeVarInt(m.slot);},b->new Select(b.readResourceLocation(),b.readVarInt()));
        @Override public Type<Select> type(){return TYPE;}
    }
    /** Pure visual burst: the server never spawns damaging rocket entities. */
    public record Burst(double x,double y,double z,int color,float scale) implements CustomPacketPayload {
        public Burst(double x,double y,double z,int color){this(x,y,z,color,1);}
        public static final Type<Burst> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(ResonantArcana.ID,"burst"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Burst> CODEC=StreamCodec.of((b,m)->{b.writeDouble(m.x);b.writeDouble(m.y);b.writeDouble(m.z);b.writeInt(m.color);b.writeFloat(m.scale);},b->new Burst(b.readDouble(),b.readDouble(),b.readDouble(),b.readInt(),b.readFloat()));
        @Override public Type<Burst> type(){return TYPE;}
    }
    /** One finite native effect; endpoints orient beams and travelling spells. */
    public record Visual(int spell,int stage,double x,double y,double z,double endX,double endY,double endZ,float size) implements CustomPacketPayload {
        public static final Type<Visual> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(ResonantArcana.ID,"visual"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Visual> CODEC=StreamCodec.of((b,m)->{
            b.writeVarInt(m.spell);b.writeVarInt(m.stage);b.writeDouble(m.x);b.writeDouble(m.y);b.writeDouble(m.z);
            b.writeDouble(m.endX);b.writeDouble(m.endY);b.writeDouble(m.endZ);b.writeFloat(m.size);
        },b->new Visual(b.readVarInt(),b.readVarInt(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readFloat()));
        @Override public Type<Visual> type(){return TYPE;}
        public boolean valid(){
            return spell>=0&&spell<Spell.values().length&&stage>=0&&stage<SpellVisuals.Stage.values().length
                &&Double.isFinite(x)&&Double.isFinite(y)&&Double.isFinite(z)&&Double.isFinite(endX)&&Double.isFinite(endY)&&Double.isFinite(endZ)
                &&Float.isFinite(size)&&size>=0.1f&&size<=(GodSpells.handles(Spell.values()[spell])?32:16)&&Math.abs(x)<=30_000_000&&Math.abs(y)<=30_000_000&&Math.abs(z)<=30_000_000
                &&Math.abs(endX)<=30_000_000&&Math.abs(endY)<=30_000_000&&Math.abs(endZ)<=30_000_000
                &&new net.minecraft.world.phys.Vec3(x,y,z).distanceToSqr(new net.minecraft.world.phys.Vec3(endX,endY,endZ))<=128*128;
        }
    }
    static void select(ServerPlayer player,Select message){
        if(message.slot>=0&&player.getMainHandItem().getItem() instanceof WandItem wand&&message.slot<wand.spells.size()
            &&net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).equals(message.wand))SpellEngine.action(player,message.slot+3);
    }
    public static void register(RegisterPayloadHandlersEvent e) {
        var r=e.registrar("33");
        r.playToClient(Particles.TYPE,Particles.CODEC,(msg,ctx)->ctx.enqueueWork(()->FirstPersonEffects.handle(msg)));
        r.playToClient(RhoFocus.TYPE,RhoFocus.CODEC,(msg,ctx)->ctx.enqueueWork(()->RhoAiasEffects.handle(msg)));
        r.playToClient(OrbitalFocus.TYPE,OrbitalFocus.CODEC,(msg,ctx)->ctx.enqueueWork(()->OrbitalEffects.handle(msg)));
        r.playToClient(Loop.TYPE,Loop.CODEC,(msg,ctx)->ctx.enqueueWork(()->SpellLoopAudio.handle(msg)));
        r.playToClient(BeamFocus.TYPE,BeamFocus.CODEC,(msg,ctx)->ctx.enqueueWork(()->DoomBeamEffects.track(msg)));
        r.playToClient(Chant.TYPE,Chant.CODEC,(msg,ctx)->ctx.enqueueWork(()->RecordedAudio.handle(msg)));
        r.playToServer(Action.TYPE,Action.CODEC,(msg,ctx)->ctx.enqueueWork(()->{
            if(ctx.player() instanceof ServerPlayer player)SpellEngine.action(player,msg.action);
        }));
        r.playToServer(Select.TYPE,Select.CODEC,(msg,ctx)->ctx.enqueueWork(()->{
            if(ctx.player() instanceof ServerPlayer p)select(p,msg);
        }));
        r.playToClient(Visual.TYPE,Visual.CODEC,(msg,ctx)->ctx.enqueueWork(()->{NativeSpellEffects.visual(msg);AaaEffects.visual(msg);}));
        r.playToClient(Burst.TYPE,Burst.CODEC,(msg,ctx)->ctx.enqueueWork(()->WandClient.burst(msg)));
    }
}

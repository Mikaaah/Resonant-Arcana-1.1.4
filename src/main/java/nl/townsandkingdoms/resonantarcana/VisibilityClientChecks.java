package nl.townsandkingdoms.resonantarcana;

import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Development-only UI fixture; excluded from release packaging. */
@EventBusSubscriber(modid=ResonantArcana.ID, value=Dist.CLIENT)
public final class VisibilityClientChecks {
    private static int stage, ticks, checks;
    private static ArcanaConfigScreen screen;
    private static TitleScreen parent;
    private static void check(boolean value, String label) { checks++; if (!value) throw new AssertionError(label); }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("resonant_arcana.visibilityClientChecks")) return;
        Minecraft mc=Minecraft.getInstance();
        try {
            if (stage==0) {
                if (mc.getOverlay()==null && mc.screen!=null && !(mc.screen instanceof TitleScreen)) mc.setScreen(new TitleScreen());
                if (!(mc.screen instanceof TitleScreen title) || !SpellVisibilityConfig.SPEC.isLoaded()) return;
                parent=title;
                Files.createDirectories(Path.of("build/validation"));
                var container=ModList.get().getModContainerById(ResonantArcana.ID).orElseThrow();
                var factory=IConfigScreenFactory.getForMod(container.getModInfo()).orElseThrow();
                screen=(ArcanaConfigScreen)factory.createScreen(container,parent);mc.setScreen(screen);stage=1;ticks=0;
                check(Files.isRegularFile(mc.gameDirectory.toPath().resolve("config/"+SpellVisibilityConfig.FILE_NAME)),"separate config generated");
            } else if (++ticks==15 && stage==1) {
                for (var widget:screen.children()) if(widget instanceof Button b) check(b.getX()>=0&&b.getY()>=0&&b.getX()+b.getWidth()<=screen.width&&b.getY()+b.getHeight()<=screen.height,"button inside screen");
                Screenshot.grab(Path.of("build/validation").toAbsolutePath().toFile(),"visibility-tab.png",mc.getMainRenderTarget(),message->{});
                EditBox search=screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).findFirst().orElseThrow();
                search.setValue("doom_beam");
                Button row=screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).filter(b->b.visible&&b.getMessage().getString().startsWith("Doom Beam:")).findFirst().orElseThrow();
                boolean before=SpellVisibilityConfig.clear(Spell.DOOM_BEAM);row.onPress();
                check(SpellVisibilityConfig.clear(Spell.DOOM_BEAM)!=before,"button changes live value");
                String file=Files.readString(mc.gameDirectory.toPath().resolve("config/"+SpellVisibilityConfig.FILE_NAME));
                check(file.contains("doom_beam = "+!before),"button saves actual config");
                screen.resize(mc,320,240);
                for(var widget:screen.children())if(widget instanceof Button b&&b.visible)check(b.getX()>=0&&b.getY()>=0&&b.getX()+b.getWidth()<=320&&b.getY()+b.getHeight()<=240,"small UI bounds");
                screen.resize(mc,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight());
                stage=2;ticks=0;
            } else if(stage==2&&ticks==15) {
                Screenshot.grab(Path.of("build/validation").toAbsolutePath().toFile(),"visibility-search.png",mc.getMainRenderTarget(),message->{});
                screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).filter(b->b.getMessage().getString().equals("Reset defaults")).findFirst().orElseThrow().onPress();
                check(!SpellVisibilityConfig.clear(Spell.DOOM_BEAM),"reset restores default");
                screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).filter(b->b.getMessage().getString().equals("General")).findFirst().orElseThrow().onPress();
                check(mc.screen instanceof net.neoforged.neoforge.client.gui.ConfigurationScreen,"general settings open");
                mc.screen.onClose();check(mc.screen==screen,"general returns to visibility tab");screen.onClose();check(mc.screen==parent,"done returns to parent");
                System.out.println("VISIBILITY_UI_CHECKS "+checks+" passed");
                stage=3;ticks=0;mc.createWorldOpenFlows().openWorld("ArcanaQA",()->{throw new AssertionError("QA world failed to open");});
            } else if(stage==3) {
                if(mc.level==null||mc.player==null) {
                    if(mc.screen!=null)for(var widget:mc.screen.children())if(widget instanceof Button b&&b.active&&(b.getMessage().getString().contains("I know what")||b.getMessage().getString().equals("Proceed"))){b.onPress();break;}
                    return;
                }
                if(ticks<30)return;
                mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);mc.setCameraEntity(mc.player);
                var camera=mc.gameRenderer.getMainCamera();
                var position=camera.getPosition();var vector=camera.getLookVector();
                var aim=new net.minecraft.world.phys.Vec3(vector.x(),vector.y(),vector.z());
                var near=position.add(aim.scale(2));var far=position.add(aim.scale(10));
                for(Spell spell:Spell.values()) {
                    var value=SpellVisibilityConfig.VALUES.get(spell);
                    var particle=new net.minecraft.client.particle.Particle[1];
                    value.set(true);
                    FirstPersonEffects.forSpell(spell,()->particle[0]=FirstPersonEffects.create(net.minecraft.core.particles.ParticleTypes.END_ROD,near,net.minecraft.world.phys.Vec3.ZERO));
                    check(particle[0]==null,spell+" enabled clears crosshair");
                    value.set(false);
                    FirstPersonEffects.forSpell(spell,()->particle[0]=FirstPersonEffects.create(net.minecraft.core.particles.ParticleTypes.END_ROD,near,net.minecraft.world.phys.Vec3.ZERO));
                    check(particle[0]!=null&&particle[0].isAlive(),spell+" disabled preserves crosshair");
                    value.set(true);FirstPersonEffects.tick(null);
                    check(!particle[0].isAlive(),spell+" live toggle clears existing particle");
                    FirstPersonEffects.forSpell(spell,()->particle[0]=FirstPersonEffects.create(net.minecraft.core.particles.ParticleTypes.END_ROD,far,net.minecraft.world.phys.Vec3.ZERO));
                    check(particle[0]!=null,spell+" enabled preserves distant effects");
                    mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
                    FirstPersonEffects.forSpell(spell,()->particle[0]=FirstPersonEffects.create(net.minecraft.core.particles.ParticleTypes.END_ROD,near,net.minecraft.world.phys.Vec3.ZERO));
                    check(particle[0]!=null,spell+" third person unaffected");
                    mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
                    var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),mc.level.registryAccess());
                    try {
                        var packet=new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket(net.minecraft.core.particles.ParticleTypes.END_ROD,false,near.x,near.y,near.z,0,0,0,0,0);
                        WandNetwork.Particles.CODEC.encode(buffer,new WandNetwork.Particles(packet,spell.ordinal()));
                        var decoded=WandNetwork.Particles.CODEC.decode(buffer);
                        check(decoded.valid()&&decoded.spell()==spell.ordinal(),spell+" network identity roundtrip");
                    } finally {buffer.release();}
                    value.set(SpellVisibilityConfig.defaultValue(spell));
                }
                SpellVisibilityConfig.SPEC.save();
                Files.writeString(Path.of("build/validation/client-checks.json"),"{\"success\":true,\"spells\":"+Spell.values().length+",\"checks\":"+checks+"}\n");
                System.out.println("VISIBILITY_CLIENT_CHECKS "+checks+" passed");mc.stop();stage=4;
            }
        } catch(Throwable failure) {
            failure.printStackTrace();try{Files.createDirectories(Path.of("build/validation"));Files.writeString(Path.of("build/validation/client-checks.json"),"{\"success\":false,\"error\":\""+failure.toString().replace('"','\'')+"\"}\n");}catch(Exception ignored){}
            mc.stop();stage=3;
        }
    }
}

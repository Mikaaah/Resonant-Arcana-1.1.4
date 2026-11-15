package nl.townsandkingdoms.resonantarcana;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Opt-in dedicated-server fixture, excluded from published JARs. */
@EventBusSubscriber(modid = ResonantArcana.ID)
final class BurstServerChecks {
    private static final List<String> CHECKS = new ArrayList<>();
    private static void check(boolean value, String message) { if(!value) throw new AssertionError(message); CHECKS.add(message); }
    @SubscribeEvent
    static void start(ServerStartedEvent event) {
        if(!Boolean.getBoolean("resonant_arcana.burstChecks")) return;
        var report = new LinkedHashMap<String,Object>();
        try { run(event.getServer().overworld()); report.put("success", true); }
        catch(Throwable error) { report.put("success", false); report.put("error", error.toString()); error.printStackTrace(); }
        report.put("checks", CHECKS);
        try { Path path = Path.of("../build/validation/burst-server-checks.json"); Files.createDirectories(path.getParent()); Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(report)); }
        catch(Exception error) { throw new RuntimeException(error); }
        event.getServer().halt(false);
    }
    private static Zombie enemy(ServerLevel level, double x, double z) {
        Zombie enemy = new Zombie(EntityType.ZOMBIE, level);
        enemy.setPos(x,81,z); enemy.setNoAi(true);
        enemy.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1024); enemy.getAttribute(Attributes.ARMOR).setBaseValue(0); enemy.setHealth(1024);
        level.addFreshEntity(enemy); return enemy;
    }
    private static void run(ServerLevel level) {
        for(int x=-20;x<=20;x++) for(int z=-20;z<=24;z++) {
            level.setBlockAndUpdate(new BlockPos(x,80,z), Blocks.STONE.defaultBlockState());
            for(int y=81;y<112;y++) level.setBlockAndUpdate(new BlockPos(x,y,z), Blocks.AIR.defaultBlockState());
        }
        ServerPlayer player = FakePlayerFactory.get(level,new GameProfile(UUID.fromString("b74c9011-c52e-4886-9f45-cc1d13b12000"),"BurstChecks"));
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        player.setPos(0,81,0); player.setYRot(0); player.setXRot(0);
        var team=level.getScoreboard().addPlayerTeam("burst_checks"); team.setAllowFriendlyFire(false);
        level.getScoreboard().addPlayerToTeam(player.getScoreboardName(),team);
        try {
            for(Spell spell : ElementalBursts.SPELLS) {
                SpellEngine.clear(); player.removeAllEffects(); player.setHealth(10);
                Zombie target = enemy(level,0,6), friend=enemy(level,1,6), hidden=enemy(level,0,12);
                level.getScoreboard().addPlayerToTeam(friend.getScoreboardName(),team);
                check(SpellEngine.castFromSpellbook(player,spell,1,level.damageSources().indirectMagic(player,player),1,true),spell+" starts");
                if(spell==Spell.PLANET_BEFALL||spell==Spell.CELESTIAL_SHOWER) {
                    var props=level.getEntitiesOfClass(net.minecraft.world.entity.Display.class,new AABB(-30,70,-30,30,120,40));
                    check(!props.isEmpty()&&props.stream().noneMatch(prop->prop.getType().canSerialize()),spell+" displays cannot persist in world saves");
                }
                for(int x=-18;x<=18;x++) for(int y=81;y<=110;y++) level.setBlockAndUpdate(new BlockPos(x,y,9),Blocks.STONE.defaultBlockState());
                try {
                    for(int tick=1;tick<=spell.duration()+2;tick++) {
                        target.invulnerableTime=0;friend.invulnerableTime=0;hidden.invulnerableTime=0;
                        SpellEngine.tick();
                        if(spell==Spell.PLANET_BEFALL && tick==36) {
                            check(target.hasEffect(ArcanaEffects.PETRIFIED),"meteor applies Petrified");
                            check(target.isNoAi(),"petrification suspends AI");
                            check(friend.getHealth()==1024 && !friend.hasEffect(ArcanaEffects.PETRIFIED),"meteor protects allies from damage and status");
                            target.removeEffect(ArcanaEffects.PETRIFIED);
                            NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(target));
                            check(target.isNoAi(),"petrification restores originally disabled AI");
                        }
                        if(spell==Spell.PRESERVER_OF_FORTUNE && tick==1) {
                            check(Math.abs(player.getHealth()-18)<.01,"fortune burst heals nearby caster by eight");
                            check(target.hasEffect(ArcanaEffects.FORTUNE_TALISMAN),"fortune burst marks enemies");
                            float before=player.getHealth();target.invulnerableTime=0;target.hurt(level.damageSources().playerAttack(player),1);
                            check(Math.abs(player.getHealth()-before-4)<.01,"marked target heals attacking ally");
                            target.invulnerableTime=0;target.hurt(level.damageSources().playerAttack(player),1);
                            check(Math.abs(player.getHealth()-before-4)<.01,"fortune healing is limited to once per second");
                        }
                    }
                    float total = switch(spell) {
                        case PLANET_BEFALL -> 180;
                        case PRESERVER_OF_FORTUNE -> 26;
                        case MUSOU_SHINSETSU -> 432;
                        case WINDS_GRAND_ODE -> 128;
                        default -> 216;
                    };
                    check(Math.abs(1024-target.getHealth()-total)<.05,spell+" exact total "+(1024-target.getHealth()));
                    check(friend.getHealth()==1024,spell+" team protection");
                    check(hidden.getHealth()==1024,spell+" cover protection");
                    check(SpellEngine.activeCount()==0,spell+" expires");
                    check(level.getEntitiesOfClass(net.minecraft.world.entity.Display.class,new AABB(-30,70,-30,30,120,40)).isEmpty(),spell+" display cleanup");
                    check(level.getBlockState(new BlockPos(0,80,6)).is(Blocks.STONE),spell+" terrain preserved");
                } finally {
                    for(int x=-18;x<=18;x++) for(int y=81;y<=110;y++) level.setBlockAndUpdate(new BlockPos(x,y,9),Blocks.AIR.defaultBlockState());
                    target.discard(); friend.discard(); hidden.discard(); SpellEngine.clear();
                }
            }
            Zombie normal=enemy(level,0,5);normal.setNoAi(false);
            BurstStatus.petrify(normal,4);
            var start=normal.position();normal.setDeltaMovement(1,0,1);
            NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(normal));
            check(normal.position().equals(start)&&normal.getDeltaMovement().lengthSqr()==0,"petrification pins movement");
            Zombie victim=enemy(level,2,5);
            victim.hurt(level.damageSources().mobAttack(normal),10);
            check(victim.getHealth()==1024,"petrified attackers cannot deal direct damage");victim.discard();
            for(int tick=0;tick<5;tick++) {
                NeoForge.EVENT_BUS.post(new EntityTickEvent.Pre(normal));normal.tick();NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(normal));
            }
            check(!normal.hasEffect(ArcanaEffects.PETRIFIED)&&!normal.isNoAi(),"petrification naturally expires and restores active AI");
            BurstStatus.petrify(normal,20);normal.removeEffect(ArcanaEffects.PETRIFIED);NeoForge.EVENT_BUS.post(new EntityTickEvent.Post(normal));
            check(!normal.isNoAi(),"cleansing restores active AI");normal.discard();
            check(SpellIndex.matches(Spell.PLANET_BEFALL,"zhongli geo"),"Zhongli search keywords");
            check(SpellIndex.matches(Spell.PRESERVER_OF_FORTUNE,"qiqi healing"),"Qiqi search keywords");
        } finally { level.getScoreboard().removePlayerTeam(team);SpellEngine.clear();player.removeAllEffects(); }
    }
}

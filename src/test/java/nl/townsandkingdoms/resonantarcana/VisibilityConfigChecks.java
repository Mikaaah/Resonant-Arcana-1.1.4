package nl.townsandkingdoms.resonantarcana;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import net.neoforged.fml.config.IConfigSpec;

public final class VisibilityConfigChecks {
    private static int checks;
    private static void check(boolean value, String label) { checks++; if (!value) throw new AssertionError(label); }
    public static void main(String[] args) throws Exception {
        var spec = SpellVisibilityConfig.SPEC;
        check(SpellVisibilityConfig.VALUES.size() == Spell.values().length, "every spell has a toggle");
        Path path = Path.of("build/validation/resonantarcana-spell-visibility.toml");
        Files.createDirectories(path.getParent()); Files.deleteIfExists(path);
        try (var file = CommentedFileConfig.builder(path).sync().build()) {
            file.load();
            spec.correct(file);
            var constructor = Class.forName("net.neoforged.fml.config.LoadedConfig").getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            spec.acceptConfig((IConfigSpec.ILoadedConfig)constructor.newInstance(file, null, null));
            var expected = new HashMap<Spell, Boolean>();
            for (Spell spell : Spell.values()) {
                boolean initial = SpellVisibilityConfig.defaultValue(spell);
                check(SpellVisibilityConfig.clear(spell) == initial, spell + " default");
                expected.put(spell, initial);
            }
            for (Spell spell : Spell.values()) {
                SpellVisibilityConfig.VALUES.get(spell).set(!expected.get(spell));
                expected.put(spell, !expected.get(spell));
                for (Spell other : Spell.values()) check(SpellVisibilityConfig.clear(other) == expected.get(other), spell + " is isolated from " + other);
            }
            file.save();
            file.load(); spec.afterReload();
            for (Spell spell : Spell.values()) check(SpellVisibilityConfig.clear(spell) == expected.get(spell), spell + " persists after reload");
            file.set("spells.spark", "invalid"); file.set("spells.doom_beam", 42);
            spec.correct(file); spec.afterReload();
            check(!SpellVisibilityConfig.clear(Spell.SPARK), "invalid string corrected");
            check(!SpellVisibilityConfig.clear(Spell.DOOM_BEAM), "invalid integer corrected");
            for (Spell spell : Spell.values()) SpellVisibilityConfig.VALUES.get(spell).set(SpellVisibilityConfig.defaultValue(spell));
            file.save(); file.load(); spec.afterReload();
            for (Spell spell : Spell.values()) check(SpellVisibilityConfig.clear(spell) == SpellVisibilityConfig.defaultValue(spell), spell + " reset persists");
        }
        check(SpellParticleScope.currentId() == -1, "scope starts empty");
        try (var a = SpellParticleScope.enter(Spell.DOOM_BEAM)) {
            check(SpellParticleScope.currentId() == Spell.DOOM_BEAM.ordinal(), "server scope carries spell");
            try { try (var b = SpellParticleScope.enter(Spell.SPARK)) { throw new IllegalStateException("fixture"); } }
            catch (IllegalStateException expected) { check(SpellParticleScope.currentId() == Spell.DOOM_BEAM.ordinal(), "nested failure restores parent"); }
        }
        check(SpellParticleScope.currentId() == -1, "scope clears after cast");
        check(AimVisibility.obscures(0,0,2,0,0,1,0), "near crosshair is cleared");
        check(!AimVisibility.obscures(0,0,8,0,0,1,0), "distant spectacle remains");
        check(!AimVisibility.obscures(3,0,2,0,0,1,0), "peripheral effects remain");
        Files.writeString(Path.of("build/validation/config-checks.json"), "{\"success\":true,\"spells\":" + Spell.values().length + ",\"checks\":" + checks + "}\n");
        System.out.println("VISIBILITY_CONFIG_CHECKS " + checks + " passed for " + Spell.values().length + " spells");
    }
}

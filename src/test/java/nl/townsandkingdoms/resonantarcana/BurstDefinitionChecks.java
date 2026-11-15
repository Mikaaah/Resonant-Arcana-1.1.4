package nl.townsandkingdoms.resonantarcana;

import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

/** Compare real baseline definitions, so tier changes cannot silently affect ordinary spells. */
public final class BurstDefinitionChecks {
    private static int checks;
    private static void check(boolean value, String message) { checks++; if(!value) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        int balanced = 0;
        SpellCatalogue.validateRegistry();
        try(var baseline = new ZipFile("baseline/resonant-arcana-1.1.3.jar")) {
            var root = JsonParser.parseString(new String(baseline.getInputStream(baseline.getEntry("assets/resonant_arcana/catalogue.json")).readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            var old = root.getAsJsonObject("spells");
            check(Spell.values().length == old.size() + 5, "exactly five new spells");
            for(var entry : old.entrySet()) {
                Spell spell = Spell.valueOf(entry.getKey());
                var before = entry.getValue().getAsJsonObject();
                float damage = before.get("damage").getAsFloat();
                boolean reduced = spell.category() == SpellCategory.GOD || spell.category() == SpellCategory.CATACLYSM;
                check(Math.abs(spell.damage - damage * (reduced ? .8f : 1)) < .001, spell + " damage");
                if(reduced && damage > 0) balanced++;
                check(spell.manaCost() == before.get("mana").getAsInt(), spell + " mana preserved");
                check(spell.cooldown == before.get("cooldown").getAsInt(), spell + " cooldown preserved");
                check(spell.castTicks() == before.get("castTicks").getAsInt(), spell + " cast time preserved");
                check(spell.duration() == before.get("duration").getAsInt(), spell + " sequence preserved");
            }
        }
        check(balanced == 43, "all 43 offensive God/Cataclysm spells reduced");
        for(Spell spell : ElementalBursts.SPELLS) {
            check(spell.category() == SpellCategory.CATACLYSM && spell.levelCap() == 1, spell + " tier");
            check(spell.definition.integration() == null, spell + " no optional integration required");
            check(SpellCastingAnimations.start(spell) != null && SpellCastingAnimations.finish(spell) != null, spell + " animations");
            for(String asset : new String[]{"assets/resonant_arcana/textures/gui/spell_icons/" + spell.bookId() + ".png", "data/resonant_arcana/recipe/scrolls/" + spell.bookId() + ".json"})
                check(BurstDefinitionChecks.class.getResource("/" + asset) != null, "packaged " + asset);
        }
        Files.createDirectories(Path.of("build/validation"));
        Files.writeString(Path.of("build/validation/burst-definition-checks.json"), "{\"success\":true,\"checks\":"+checks+",\"spells\":"+Spell.values().length+",\"rebalanced\":"+balanced+"}\n");
        System.out.println("BURST_DEFINITION_CHECKS " + checks + " passed");
    }
}

package nl.townsandkingdoms.resonantarcana;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Client preferences only. These values are never synchronized by a server. */
final class SpellVisibilityConfig {
    static final String FILE_NAME = "resonantarcana-spell-visibility.toml";
    static final ModConfigSpec SPEC;
    static final Map<Spell, ModConfigSpec.BooleanValue> VALUES;
    static {
        var builder = new ModConfigSpec.Builder();
        var values = new EnumMap<Spell, ModConfigSpec.BooleanValue>(Spell.class);
        builder.comment("Clear first-person view per spell. true = clear nearby particles from your aim; false = full particles.",
            "Only affects your client in first person. Distant particles and third-person effects remain visible.",
            "Changes apply immediately; no restart is required.").push("spells");
        for (Spell spell : Spell.values()) {
            values.put(spell, builder.comment(spell.title + ": true = clear first-person view; false = full effects.")
                .translation("resonant_arcana.visibility." + spell.bookId())
                .define(spell.bookId(), defaultValue(spell)));
        }
        builder.pop();
        SPEC = builder.build();
        VALUES = Collections.unmodifiableMap(values);
    }
    static boolean defaultValue(Spell spell) { return spell.theme == Spell.Theme.THUNDER; }
    static boolean clear(Spell spell) {
        return spell != null && (SPEC.isLoaded() ? VALUES.get(spell).get() : defaultValue(spell));
    }
    private SpellVisibilityConfig() {}
}

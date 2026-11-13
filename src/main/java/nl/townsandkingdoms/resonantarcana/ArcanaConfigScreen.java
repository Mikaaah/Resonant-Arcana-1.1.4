package nl.townsandkingdoms.resonantarcana;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

/** Searchable per-spell controls, alongside the existing native configuration editor. */
final class ArcanaConfigScreen extends Screen {
    private final ModContainer container;
    private final Screen parent;
    private final List<Spell> all = Arrays.stream(Spell.values()).sorted(Comparator.comparing(s -> s.title)).toList();
    private EditBox search;
    private String query = "";
    private int page;
    private int pageSize;
    private List<Spell> filtered = List.of();
    private Button previous, next;
    private final java.util.ArrayList<Button> rows = new java.util.ArrayList<>();

    ArcanaConfigScreen(ModContainer container, Screen parent) {
        super(Component.literal("Resonant Arcana")); this.container = container; this.parent = parent;
    }
    @Override protected void init() {
        int panel = Math.min(500, width - 24), left = (width - panel) / 2;
        addRenderableWidget(Button.builder(Component.literal("General"), b -> {
            save();
            minecraft.setScreen(new ConfigurationScreen(container, this, (config, key, element) ->
                key.equals("clearFirstPersonAim") ? null : element));
        }).bounds(left, 30, panel / 2 - 2, 20).build());
        Button tab = addRenderableWidget(Button.builder(Component.literal("First-person view"), b -> {}).bounds(left + panel / 2 + 2, 30, panel / 2 - 2, 20).build());
        tab.active = false;
        search = new EditBox(font, left, 70, panel, 20, Component.literal("Search spells"));
        search.setHint(Component.literal("Search spells...")); search.setValue(query);
        search.setResponder(value -> { query = value; page = 0; refresh(); });
        addRenderableWidget(search);
        pageSize = Math.max(1, (height - 164) / 24);
        rows.clear();
        for (int i = 0; i < pageSize; i++) {
            final int row = i;
            rows.add(addRenderableWidget(Button.builder(Component.empty(), b -> toggle(row))
                .bounds(left, 98 + i * 24, panel, 20).build()));
        }
        previous = addRenderableWidget(Button.builder(Component.literal("Previous"), b -> { page--; refresh(); }).bounds(left, height - 57, 80, 20).build());
        next = addRenderableWidget(Button.builder(Component.literal("Next"), b -> { page++; refresh(); }).bounds(left + panel - 80, height - 57, 80, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Reset defaults"), b -> {
            for (Spell s : all) SpellVisibilityConfig.VALUES.get(s).set(SpellVisibilityConfig.defaultValue(s));
            save(); refresh();
        }).bounds(width / 2 - 125, height - 29, 120, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose()).bounds(width / 2 + 5, height - 29, 120, 20).build());
        refresh();
    }
    private void toggle(int row) {
        int index = page * pageSize + row;
        if (index >= filtered.size()) return;
        Spell spell = filtered.get(index);
        SpellVisibilityConfig.VALUES.get(spell).set(!SpellVisibilityConfig.clear(spell));
        save(); refresh();
    }
    private void refresh() {
        String term = query.toLowerCase(Locale.ROOT).trim();
        filtered = all.stream().filter(s -> s.title.toLowerCase(Locale.ROOT).contains(term)
            || s.bookId().contains(term) || s.displayName().getString().toLowerCase(Locale.ROOT).contains(term)).toList();
        page = Math.clamp(page, 0, Math.max(0, (filtered.size() - 1) / pageSize));
        for (int i = 0; i < rows.size(); i++) {
            int index = page * pageSize + i;
            Button button = rows.get(i); button.visible = index < filtered.size();
            if (!button.visible) continue;
            Spell spell = filtered.get(index);
            String state = SpellVisibilityConfig.clear(spell) ? "true" : "false";
            String label = font.plainSubstrByWidth(spell.displayName().getString(), button.getWidth() - 65);
            button.setMessage(Component.literal(label + ": " + state));
            button.setTooltip(Tooltip.create(Component.literal(spell.title + "\ntrue: clear nearby particles from your aim\nfalse: full particles\nApplies immediately in first person.")));
        }
        previous.active = page > 0; next.active = (page + 1) * pageSize < filtered.size();
    }
    private void save() { if (SpellVisibilityConfig.SPEC.isLoaded()) SpellVisibilityConfig.SPEC.save(); }
    @Override public void onClose() { save(); minecraft.setScreen(parent); }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 12, 0xffffff);
        graphics.drawCenteredString(font, "true = clear aim | false = full particles", width / 2, 56, 0xb8b8c5);
        graphics.drawCenteredString(font, filtered.isEmpty() ? "No matching spells" : "Page " + (page + 1) + " / " + Math.max(1, (filtered.size() + pageSize - 1) / pageSize) + "  -  " + filtered.size() + (filtered.size()==1 ? " spell" : " spells"), width / 2, height - 51, 0xb8b8c5);
    }
}

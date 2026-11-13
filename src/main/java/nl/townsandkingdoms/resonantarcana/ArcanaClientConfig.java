package nl.townsandkingdoms.resonantarcana;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = ResonantArcana.ID, dist = Dist.CLIENT)
public final class ArcanaClientConfig {
    public ArcanaClientConfig(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SpellVisibilityConfig.SPEC, SpellVisibilityConfig.FILE_NAME);
        container.registerExtensionPoint(IConfigScreenFactory.class, (mod, parent) -> new ArcanaConfigScreen(mod, parent));
    }
}

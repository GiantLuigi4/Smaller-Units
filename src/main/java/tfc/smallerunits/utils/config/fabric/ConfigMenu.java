package tfc.smallerunits.utils.config.fabric;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.example.ExampleConfig;
import me.shedaniel.clothconfig2.ClothConfigDemo;
import net.minecraft.client.gui.screens.Screen;
import tfc.smallerunits.utils.config.ServerConfig;
import tfc.smallerunits.utils.config.annoconfg.AnnoCFG;

public class ConfigMenu implements ModMenuApi {
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ServerConfig::getScreen;
	}
}

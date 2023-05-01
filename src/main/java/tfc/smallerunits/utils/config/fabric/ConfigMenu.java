package tfc.smallerunits.utils.config.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import tfc.smallerunits.utils.config.ServerConfig;

public class ConfigMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ModMenuApi.super.getModConfigScreenFactory();
	}
}

package tfc.smallerunits.utils.config.annoconfg.builder;

import net.minecraft.client.gui.screens.Screen;
import tfc.smallerunits.utils.config.annoconfg.Config;
import tfc.smallerunits.utils.config.annoconfg.fabric.json.JsonCfgBuilder;

import java.util.function.Function;

public abstract class CfgBuilder {
	public static CfgBuilder automatic(String root) {
		return new JsonCfgBuilder(root);
	}
	
	public abstract CategoryBuilder rootCategory();
	
	public abstract void setSaveFunction(Runnable onConfigChange);
	
	public abstract Function<Screen, Screen> createScreen();
	
	public abstract Config build();
}

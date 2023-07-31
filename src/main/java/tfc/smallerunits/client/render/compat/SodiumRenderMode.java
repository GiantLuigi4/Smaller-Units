package tfc.smallerunits.client.render.compat;

import net.minecraft.ChatFormatting;
import tfc.smallerunits.utils.config.annoconfg.util.ConfigEnum;

public enum SodiumRenderMode implements ConfigEnum {
	VANILLA(ChatFormatting.RED, "Vanilla Style (slow)", "vanilla"),
	SODIUM(ChatFormatting.GREEN, "Sodium Style (fast)", "sodium");
	
	public final ChatFormatting formatting;
	public final String f3Text;
	public final String configText;
	
	SodiumRenderMode(ChatFormatting formatting, String f3Text, String configText) {
		this.formatting = formatting;
		this.f3Text = f3Text;
		this.configText = configText;
	}
	
	
	@Override
	public String getConfigName() {
		return configText;
	}
}

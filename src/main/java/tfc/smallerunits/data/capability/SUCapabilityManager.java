package tfc.smallerunits.data.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;

// so I mostly just abandoned any documentation that I was given and write this
public class SUCapabilityManager {
	private static final CapabilityProvider provider = new CapabilityProvider();
	
	public static void onAttachCapabilities(AttachCapabilitiesEvent<LevelChunk> event) {
		event.addCapability(
				new ResourceLocation("smallerunits", "unit_space_cap"),
				provider
		);
	}
	
	public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
		event.register(ISUCapability.class);
	}
	
	public static final Capability<ISUCapability> SU_CAPABILITY_TOKEN = CapabilityManager.get(new CapabilityToken<>() {
	});
	
	public static ISUCapability getCapability(LevelChunk chunk) {
		return chunk.getCapability(SU_CAPABILITY_TOKEN, null).orElse(null);
	}
}

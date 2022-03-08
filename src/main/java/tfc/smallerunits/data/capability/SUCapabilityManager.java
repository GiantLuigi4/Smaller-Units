package tfc.smallerunits.data.capability;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;

// https://github.com/100MediaJavaDevTeam/CapabilitySyncer/blob/1.18.x/src/main/java/dev/_100media/capabilitysyncer/example/player/ExamplePlayerCapabilityAttacher.java#L16
// https://github.com/100MediaJavaDevTeam/CapabilitySyncer/blob/1.18.x/src/main/java/dev/_100media/capabilitysyncer/core/CapabilityAttacher.java#L113-L127
public class SUCapabilityManager {
	private static CapabilityProvider provider = new CapabilityProvider();
	public static void onAttachCapabilities(AttachCapabilitiesEvent<LevelChunk> event) {
		event.addCapability(
				new ResourceLocation("smaller_units", "unit_space_cap"),
				provider
		);
	}
	
	public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
		event.register(ISUCapability.class);
	}
}

package tfc.smallerunits.mixin.data;

import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tfc.smallerunits.client.access.tracking.FastCapabilityHandler;
import tfc.smallerunits.data.capability.ComponentRegistry;
import tfc.smallerunits.data.capability.ISUCapability;

@Mixin(LevelChunk.class)
public abstract class FastCapabilityChunkMixin implements FastCapabilityHandler {
	@Unique
	ISUCapability capability = null;
	
	@Override
	public ISUCapability getSUCapability() {
		if (((Object) this) instanceof EmptyLevelChunk) return null;
		if (capability == null)
			//#if FABRIC==1
			return capability = ((dev.onyxstudios.cca.api.v3.component.ComponentProvider) this).getComponent(ComponentRegistry.SU_CAPABILITY_COMPONENT_KEY);
			//#else
			//$$return capability = (ISUCapability) ((LevelChunk) (Object) this).getCapability(ComponentRegistry.SU_CAPABILITY_COMPONENT_KEY).orElse(null);
			//#endif
		return capability;
	}
}

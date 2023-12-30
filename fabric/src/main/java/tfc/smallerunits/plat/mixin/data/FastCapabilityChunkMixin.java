package tfc.smallerunits.plat.mixin.data;

import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tfc.smallerunits.client.access.tracking.FastCapabilityHandler;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.plat.ComponentRegistry;
import tfc.smallerunits.plat.util.PlatformUtils;

@Mixin(LevelChunk.class)
public abstract class FastCapabilityChunkMixin implements FastCapabilityHandler {
	@Unique
	ISUCapability capability = null;
	
	@Override
	public ISUCapability getSUCapability() {
		//noinspection ConstantConditions
		if (((Object) this) instanceof EmptyLevelChunk) return null;
		if (capability == null)
			capability = ((ComponentProvider) this).getComponent(ComponentRegistry.SU_CAPABILITY_COMPONENT_KEY);
		return capability;
	}
}

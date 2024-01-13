package tfc.smallerunits.plat.mixin.data;

import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.client.access.tracking.FastCapabilityHandler;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.plat.CapabilityProvider;

@Mixin(LevelChunk.class)
public abstract class FastCapabilityChunkMixin implements FastCapabilityHandler {
	@Unique
	ISUCapability capability = null;
	
	@Unique
	boolean isEmpty = false;
	
	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/world/level/chunk/UpgradeData;Lnet/minecraft/world/ticks/LevelChunkTicks;Lnet/minecraft/world/ticks/LevelChunkTicks;J[Lnet/minecraft/world/level/chunk/LevelChunkSection;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;Lnet/minecraft/world/level/levelgen/blending/BlendingData;)V")
	public void postInit(Level p_196854_, ChunkPos p_196855_, UpgradeData p_196856_, LevelChunkTicks p_196857_, LevelChunkTicks p_196858_, long p_196859_, LevelChunkSection[] p_196860_, LevelChunk.PostLoadProcessor p_196861_, BlendingData p_196862_, CallbackInfo ci) {
		isEmpty = ((Object) this) instanceof EmptyLevelChunk;
	}
	
	@Shadow(remap = false)
	public abstract <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side);
	
	@Override
	public ISUCapability getSUCapability() {
		//noinspection ConstantConditions
		if (capability == null) {
			if (isEmpty) return null;
			capability = this.getCapability(CapabilityProvider.SU_CAPABILITY_TOKEN, null).orElse(null);
		}
		return capability;
	}
}

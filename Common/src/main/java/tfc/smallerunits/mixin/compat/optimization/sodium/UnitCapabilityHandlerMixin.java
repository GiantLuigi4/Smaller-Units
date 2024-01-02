package tfc.smallerunits.mixin.compat.optimization.sodium;

import it.unimi.dsi.fastutil.objects.ObjectBigList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.client.render.compat.sodium.SodiumGridAttachments;
import tfc.smallerunits.client.render.compat.sodium.SodiumSUAttached;
import tfc.smallerunits.data.capability.SUCapability;

@Mixin(value = SUCapability.class, remap = false)
public class UnitCapabilityHandlerMixin {
	@Shadow
	@Final
	Level level;
	@Shadow
	@Final
	ChunkAccess chunk;
	@Shadow
	ObjectBigList<UnitSpace> spaces;
	
	@Unique
	boolean smallerunits$isClient;
	
	@Inject(at = @At("TAIL"), method = "<init>")
	public void postInit(Level level, ChunkAccess chunk, CallbackInfo ci) {
		smallerunits$isClient = !(level instanceof ServerLevel);
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/ObjectBigList;add(Ljava/lang/Object;)Z"), method = "getOrMakeUnit")
	public void preGetOrMake(BlockPos pos, CallbackInfoReturnable<UnitSpace> cir) {
		if (smallerunits$isClient) {
			long uc = spaces.size64();
			if (uc == 1)
				((SodiumGridAttachments) level).renderChunksWithUnits().put(chunk.getPos(), new SodiumSUAttached(chunk));
		}
	}
	
	@Inject(at = @At(value = "RETURN"), method = "setUnit")
	public void postSet(BlockPos pos, UnitSpace space, CallbackInfo ci) {
		if (smallerunits$isClient) {
			long uc = spaces.size64();
			if (uc == 1)
				((SodiumGridAttachments) level).renderChunksWithUnits().put(chunk.getPos(), new SodiumSUAttached(chunk));
		}
	}
	
	@Inject(at = @At(value = "RETURN"), method = "removeUnit")
	public void postRemove(BlockPos pos, CallbackInfo ci) {
		if (smallerunits$isClient) {
			long uc = spaces.size64();
			if (uc == 0)
				((SodiumGridAttachments) level).renderChunksWithUnits().remove(chunk.getPos(), new SodiumSUAttached(chunk));
		}
	}
	
	@Inject(at = @At(value = "RETURN"), method = "makeUnit")
	public void postMake(BlockPos pos, CallbackInfo ci) {
		if (smallerunits$isClient) {
			long uc = spaces.size64();
			if (uc == 1)
				((SodiumGridAttachments) level).renderChunksWithUnits().put(chunk.getPos(), new SodiumSUAttached(chunk));
		}
	}
}

package tfc.smallerunits.mixin.dangit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.event.world.ChunkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.client.forge.SUModelDataManager;
import tfc.smallerunits.simulation.level.ITickerWorld;
import tfc.smallerunits.simulation.level.client.FakeClientLevel;

import java.util.Map;

@Mixin(value = ModelDataManager.class, remap = false)
public class ForgeWhy {
	@Inject(at = @At("HEAD"), method = "requestModelDataRefresh", cancellable = true)
	private static void stopForgeFromCrashingTheGame(BlockEntity te, CallbackInfo ci) {
		if (!(te.getLevel() instanceof ITickerWorld)) return;
		ci.cancel();
		SUModelDataManager manager = ((FakeClientLevel) te.getLevel()).modelDataManager;
		manager.requestModelDataRefresh(te);
	}
	
	@Inject(at = @At("HEAD"), method = "refreshModelData", cancellable = true)
	private static void preRequestRefresh(Level toUpdate, ChunkPos pos, CallbackInfo ci) {
		if (!(toUpdate instanceof ITickerWorld)) return;
		ci.cancel();
		SUModelDataManager manager = ((FakeClientLevel) toUpdate).modelDataManager;
		manager.refreshModelData(toUpdate, pos);
	}
	
	@Inject(at = @At("HEAD"), method = "cleanCaches", cancellable = true)
	private static void preRequestRefresh(Level toUpdate, CallbackInfo ci) {
		if (!(toUpdate instanceof ITickerWorld)) return;
		ci.cancel();
		SUModelDataManager manager = ((FakeClientLevel) toUpdate).modelDataManager;
		manager.cleanCaches(toUpdate);
	}
	
	@Inject(at = @At("HEAD"), method = "onChunkUnload", cancellable = true)
	private static void preUnload(ChunkEvent.Unload event, CallbackInfo ci) {
		LevelAccessor accessor = event.getWorld();
		if (accessor instanceof FakeClientLevel) {
			SUModelDataManager manager = ((FakeClientLevel) accessor).modelDataManager;
			manager.onChunkUnload(event);
			ci.cancel();
		}
	}
	
	@Inject(at = @At("HEAD"), method = "getModelData(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraftforge/client/model/data/IModelData;", cancellable = true)
	private static void preGetData(Level level, BlockPos pos, CallbackInfoReturnable<IModelData> cir) {
		if (level instanceof FakeClientLevel) {
			cir.setReturnValue(((FakeClientLevel) level).modelDataManager.getModelData(level, pos));
		}
	}
	
	@Inject(at = @At("HEAD"), method = "getModelData(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;)Ljava/util/Map;", cancellable = true)
	private static void preGetData(Level level, ChunkPos pos, CallbackInfoReturnable<Map<BlockPos, IModelData>> cir) {
		if (level instanceof FakeClientLevel) {
			cir.setReturnValue(((FakeClientLevel) level).modelDataManager.getModelData(level, pos));
		}
	}
	
	@Inject(at = @At("HEAD"), method = "cleanCaches", cancellable = true)
	private static void preCleanCaches(Level level, CallbackInfo ci) {
		if (level instanceof FakeClientLevel) {
			((FakeClientLevel) level).modelDataManager.cleanCaches(level);
			ci.cancel();
		}
	}
}

package tfc.smallerunits.plat.mixin.dangit;

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
import tfc.smallerunits.plat.itf.IMayManageModelData;
import tfc.smallerunits.plat.util.ver.SUModelDataManager;
import tfc.smallerunits.simulation.level.ITickerLevel;

import java.util.Map;

@Mixin(value = ModelDataManager.class, remap = false)
public class ForgeWhy {
	@Inject(at = @At("HEAD"), method = "requestModelDataRefresh", cancellable = true)
	private static void stopForgeFromCrashingTheGame(BlockEntity te, CallbackInfo ci) {
		if (!(te.getLevel() instanceof ITickerLevel)) return;
		ci.cancel();
		SUModelDataManager manager = ((IMayManageModelData) te.getLevel()).getModelDataManager();
		manager.requestModelDataRefresh(te);
	}
	
	@Inject(at = @At("HEAD"), method = "refreshModelData", cancellable = true)
	private static void preRequestRefresh(Level toUpdate, ChunkPos pos, CallbackInfo ci) {
		if (!(toUpdate instanceof ITickerLevel)) return;
		ci.cancel();
		SUModelDataManager manager = ((IMayManageModelData) toUpdate).getModelDataManager();
		manager.refreshModelData(toUpdate, pos);
	}
	
	@Inject(at = @At("HEAD"), method = "cleanCaches", cancellable = true)
	private static void preRequestRefresh(Level toUpdate, CallbackInfo ci) {
		if (!(toUpdate instanceof ITickerLevel)) return;
		ci.cancel();
		SUModelDataManager manager = ((IMayManageModelData) toUpdate).getModelDataManager();
		manager.cleanCaches(toUpdate);
	}
	
	@Inject(at = @At("HEAD"), method = "onChunkUnload", cancellable = true)
	private static void preUnload(ChunkEvent.Unload event, CallbackInfo ci) {
		LevelAccessor accessor = event.getWorld();
		if (accessor instanceof IMayManageModelData) {
			SUModelDataManager manager = ((IMayManageModelData) accessor).getModelDataManager();
			manager.onChunkUnload(event);
			ci.cancel();
		}
	}
	
	@Inject(at = @At("HEAD"), method = "getModelData(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraftforge/client/model/data/IModelData;", cancellable = true)
	private static void preGetData(Level level, BlockPos pos, CallbackInfoReturnable<IModelData> cir) {
		if (level instanceof IMayManageModelData) {
			cir.setReturnValue(((IMayManageModelData) level).getModelDataManager().getModelData(level, pos));
		}
	}
	
	@Inject(at = @At("HEAD"), method = "getModelData(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/ChunkPos;)Ljava/util/Map;", cancellable = true)
	private static void preGetData(Level level, ChunkPos pos, CallbackInfoReturnable<Map<BlockPos, IModelData>> cir) {
		if (level instanceof IMayManageModelData) {
			cir.setReturnValue(((IMayManageModelData) level).getModelDataManager().getModelData(level, pos));
		}
	}
	
	@Inject(at = @At("HEAD"), method = "cleanCaches", cancellable = true)
	private static void preCleanCaches(Level level, CallbackInfo ci) {
		if (level instanceof IMayManageModelData) {
			((IMayManageModelData) level).getModelDataManager().cleanCaches(level);
			ci.cancel();
		}
	}
}
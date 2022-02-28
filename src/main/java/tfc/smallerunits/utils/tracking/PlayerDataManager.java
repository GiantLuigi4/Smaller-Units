package tfc.smallerunits.utils.tracking;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import tfc.smallerunits.utils.accessor.SUTracked;
import tfc.smallerunits.utils.tracking.data.SUDataParameter;
import tfc.smallerunits.utils.tracking.data.SUDataSerializer;
import tfc.smallerunits.utils.tracking.data.SUDataTracker;

public class PlayerDataManager {
	public static final SUDataParameter<Byte> PROGRESS = SUDataTracker.createParameter(SUDataSerializer.BYTE, new ResourceLocation("smaller_units:progress"));
	public static final SUDataParameter<BlockPos> POS0 = SUDataTracker.createParameter(SUDataSerializer.BLOCK_POS, new ResourceLocation("smaller_units:pos0"));
	public static final SUDataParameter<BlockPos> POS1 = SUDataTracker.createParameter(SUDataSerializer.BLOCK_POS, new ResourceLocation("smaller_units:pos1"));
	
	public static void setMiningProgress(PlayerEntity entity, byte amt) {
		((SUTracked) entity).SmallerUnits_getTracker().set(PROGRESS, amt);
	}
	
	public static void setMiningProgress(PlayerEntity entity, int amt) {
		setMiningProgress(entity, (byte) amt);
	}
	
	// pos in fake world
	public static void setMiningPosition(PlayerEntity entity, BlockPos pos) {
		((SUTracked) entity).SmallerUnits_getTracker().set(POS0, pos);
	}
	
	// pos in real world
	public static void setMiningPosition2(PlayerEntity entity, BlockPos pos) {
		((SUTracked) entity).SmallerUnits_getTracker().set(POS1, pos);
	}
	
	public static void markFinished(PlayerEntity player) {
		((SUTracked) player).SmallerUnits_setHasFinished(true);
	}
}

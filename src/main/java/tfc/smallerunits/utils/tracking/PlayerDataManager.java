package tfc.smallerunits.utils.tracking;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import tfc.smallerunits.mixins.tracking.EntityAccessor;

public class PlayerDataManager {
	private static DataParameter<Byte> SU_MINING_PROGRESS = null;
	private static DataParameter<BlockPos> SU_MINING_POSITION = null;
	private static DataParameter<BlockPos> SU_MINING_POSITION2 = null;
	private static boolean lock = false;
	
	public static void setParameters(DataParameter<Byte> param0, DataParameter<BlockPos> param1,DataParameter<BlockPos> param2) {
		if (!lock) {
			SU_MINING_PROGRESS = param0;
			SU_MINING_POSITION = param1;
			SU_MINING_POSITION2 = param2;
			lock = true;
		}
	}
	
	public static int getMiningProgress(PlayerEntity entity) {
		EntityDataManager manager = ((EntityAccessor)entity).SmallerUnits_getDataManager();
		return manager.get(SU_MINING_PROGRESS);
	}
	
	public static BlockPos getMiningPosition(PlayerEntity entity) {
		EntityDataManager manager = ((EntityAccessor)entity).SmallerUnits_getDataManager();
		return manager.get(SU_MINING_POSITION);
	}
	
	public static BlockPos getMiningPositionInWorld(PlayerEntity entity) {
		EntityDataManager manager = ((EntityAccessor)entity).SmallerUnits_getDataManager();
		return manager.get(SU_MINING_POSITION2);
	}
	
	public static void setMiningProgress(PlayerEntity entity, byte amt) {
		EntityDataManager manager = ((EntityAccessor)entity).SmallerUnits_getDataManager();
		manager.set(SU_MINING_PROGRESS, amt);
	}
	
	public static void setMiningProgress(PlayerEntity entity, int amt) {
		EntityDataManager manager = ((EntityAccessor)entity).SmallerUnits_getDataManager();
		manager.set(SU_MINING_PROGRESS, (byte) amt);
	}
	
	// pos in fake world
	public static void setMiningPosition(PlayerEntity entity, BlockPos pos) {
		EntityDataManager manager = ((EntityAccessor)entity).SmallerUnits_getDataManager();
		manager.set(SU_MINING_POSITION, pos);
	}
	
	// pos in real world
	public static void setMiningPosition2(PlayerEntity entity, BlockPos pos) {
		EntityDataManager manager = ((EntityAccessor)entity).SmallerUnits_getDataManager();
		manager.set(SU_MINING_POSITION2, pos);
	}
	
	public static void markFinished(PlayerEntity player) {
		// TODO
	}
}
a
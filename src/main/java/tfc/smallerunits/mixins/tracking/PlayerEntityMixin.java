package tfc.smallerunits.mixins.tracking;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.utils.tracking.PlayerDataManager;

import static tfc.smallerunits.utils.tracking.PlayerDataManager.*;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
	@Shadow @Final private static DataParameter<Float> ABSORPTION;
	@Unique
	private static final DataParameter<Byte> SU_MINING_PROGRESS = EntityDataManager.createKey(PlayerEntity.class, DataSerializers.BYTE);
	@Unique
	private static final DataParameter<BlockPos> SU_MINING_POSITION = EntityDataManager.createKey(PlayerEntity.class, DataSerializers.BLOCK_POS);
	@Unique
	private static final DataParameter<BlockPos> SU_MINING_POSITION2 = EntityDataManager.createKey(PlayerEntity.class, DataSerializers.BLOCK_POS);

	static {
		PlayerDataManager.setParameters(SU_MINING_PROGRESS, SU_MINING_POSITION, SU_MINING_POSITION2);
	}

	@Unique
	private EntityDataManager dataManager;

	@Inject(at = @At("TAIL"), method = "registerData")
	public void preRegisterData(CallbackInfo ci) {
		dataManager = ((EntityAccessor) this).SmallerUnits_getDataManager();
		assert ABSORPTION != null;
		//noinspection ConstantConditions
		assert SU_MINING_PROGRESS != null;
		assert dataManager != null;
		//noinspection ConstantConditions
		assert this != null;
		dataManager.register(SU_MINING_PROGRESS, (byte) -1);
		dataManager.register(SU_MINING_POSITION, new BlockPos(0, 0, 0));
		dataManager.register(SU_MINING_POSITION2, new BlockPos(0, 0, 0));
	}

	@Unique
	public void setMiningProgress(byte amt) {
		this.dataManager.set(SU_MINING_PROGRESS, amt);
	}

	@Unique
	// pos in fake world
	public void setMiningPosition(BlockPos pos) {
		this.dataManager.set(SU_MINING_POSITION, pos);
	}

	@Unique
	// pos in real world
	public void setMiningPosition2(BlockPos pos) {
		this.dataManager.set(SU_MINING_POSITION2, pos);
	}

	@Inject(at = @At("HEAD"), method = "tick")
	public void preTick(CallbackInfo ci) {

	}
}
a
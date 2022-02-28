package tfc.smallerunits.mixins.tracking;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.utils.accessor.SUTracked;
import tfc.smallerunits.utils.data.SUCapabilityManager;
import tfc.smallerunits.utils.tracking.PlayerDataManager;
import tfc.smallerunits.utils.tracking.data.SUDataTracker;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements SUTracked {
	@Unique
	private SUDataTracker tracker;
	@Unique
	private double progressVal = 0;
	@Unique
	private boolean hasFinished = false;
	
	@Override
	public SUDataTracker SmallerUnits_getTracker() {
		return tracker;
	}
	
	@Inject(at = @At("HEAD"), method = "registerData")
	public void preRegisterData(CallbackInfo ci) {
		if (tracker == null) tracker = new SUDataTracker((Entity) (Object) this);
		tracker.register(PlayerDataManager.POS0, new BlockPos(0, 0, 0));
		tracker.register(PlayerDataManager.POS1, new BlockPos(0, 0, 0));
		tracker.register(PlayerDataManager.PROGRESS, (byte) -1);
	}
	
	@Override
	public boolean SmallerUnits_hasFinished() {
		return hasFinished;
	}
	
	@Override
	public void SmallerUnits_setHasFinished(boolean hasFinished) {
		this.hasFinished = hasFinished;
	}
	
	@Inject(at = @At("HEAD"), method = "tick")
	public void preTick(CallbackInfo ci) {
		if (tracker.get(PlayerDataManager.PROGRESS) != -1) {
			BlockPos worldPos = tracker.get(PlayerDataManager.POS0);
			BlockPos littlePos = tracker.get(PlayerDataManager.POS1);
			
			PlayerEntity player = ((PlayerEntity) (Object) this);
			World world = player.getEntityWorld();
			UnitTileEntity te = SUCapabilityManager.getUnitAtBlock(world, worldPos);
			if (te != null) {
				BlockState state = te.getBlockState(littlePos);
				double hardness = state.getPlayerRelativeBlockHardness(player, te.getFakeWorld(), littlePos);
				tracker.set(PlayerDataManager.PROGRESS, (byte) (hardness * 7));
				progressVal += hardness;
				if (hasFinished && progressVal >= 1) {
					progressVal = 0;
					
					te.getBlockState().removedByPlayer(
							world, worldPos, player,
							true, Fluids.EMPTY.getDefaultState()
					);
					
					tracker.set(PlayerDataManager.POS0, new BlockPos(0, 0, 0));
					tracker.set(PlayerDataManager.POS1, new BlockPos(0, 0, 0));
					tracker.set(PlayerDataManager.PROGRESS, (byte) -1);
				}
			} else {
				tracker.set(PlayerDataManager.POS0, new BlockPos(0, 0, 0));
				tracker.set(PlayerDataManager.POS1, new BlockPos(0, 0, 0));
				tracker.set(PlayerDataManager.PROGRESS, (byte) -1);
			}
			
			// TODO: tracker.sync()
		}
	}
}

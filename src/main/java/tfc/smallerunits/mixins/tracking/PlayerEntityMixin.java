package tfc.smallerunits.mixins.tracking;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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

import java.util.HashMap;
import java.util.UUID;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements SUTracked {
	@Unique
	private SUDataTracker tracker;
	@Unique
	private double progressVal = 0;
	@Unique
	private boolean hasFinished = false;
	
	@Unique
	private HashMap<UUID, Boolean> trackMap;
	@Unique
	private HashMap<UUID, Boolean> trackMapPrev;
	
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
		trackMap = new HashMap<>();
		trackMapPrev = new HashMap<>();
	}
	
	@Override
	public boolean SmallerUnits_hasFinished() {
		return hasFinished;
	}
	
	@Override
	public void SmallerUnits_setHasFinished(boolean hasFinished) {
		this.hasFinished = hasFinished;
	}
	
	public boolean SmallerUnits_setTracking(UUID entity) {
		boolean b = trackMapPrev.containsKey(entity);
		trackMap.put(entity, true);
		return b;
	}
	
	@Inject(at = @At("HEAD"), method = "tick")
	public void preTick(CallbackInfo ci) {
		if (tracker.get(PlayerDataManager.PROGRESS) == -2) progressVal = 0;
		if (tracker.get(PlayerDataManager.PROGRESS) != -1) {
			BlockPos worldPos = tracker.get(PlayerDataManager.POS0);
			BlockPos littlePos = tracker.get(PlayerDataManager.POS1);
			
			PlayerEntity player = ((PlayerEntity) (Object) this);
			World world = player.getEntityWorld();
			UnitTileEntity te = SUCapabilityManager.getUnitAtBlock(world, worldPos);
			if (te != null) {
				BlockState state = te.getBlockState(littlePos);
				if (state == null) return;
				// don't ask me why I have to multiply it by 2, for I do not know why
				double hardness = state.getPlayerRelativeBlockHardness(player, te.getFakeWorld(), littlePos) * te.unitsPerBlock * 2;
				int prog = tracker.get(PlayerDataManager.PROGRESS);
				tracker.set(PlayerDataManager.PROGRESS, (byte) (progressVal * 7));
				progressVal += hardness;
				if (!state.equals(Blocks.AIR.getDefaultState())) {
					if ((hasFinished && progressVal >= 1) || (prog == 100 && hardness >= 1) || player.isCreative()) {
						progressVal = 0;
						
						te.getBlockState().removedByPlayer(
								world, worldPos, player,
								true, Fluids.EMPTY.getDefaultState()
						);
						
						tracker.set(PlayerDataManager.POS0, new BlockPos(0, 0, 0));
						tracker.set(PlayerDataManager.POS1, new BlockPos(0, 0, 0));
						tracker.set(PlayerDataManager.PROGRESS, (byte) -1);
						
						hasFinished = false;
					}
				}
			} else {
				tracker.set(PlayerDataManager.POS0, new BlockPos(0, 0, 0));
				tracker.set(PlayerDataManager.POS1, new BlockPos(0, 0, 0));
				tracker.set(PlayerDataManager.PROGRESS, (byte) -1);
			}
		} else {
			hasFinished = false;
			progressVal = 0;
		}
		
		HashMap<UUID, Boolean> swap = trackMapPrev;
		trackMapPrev = trackMap;
		trackMap = swap;
		swap.clear();
		
		tracker.sync();
		tracker.markClean();
	}
}

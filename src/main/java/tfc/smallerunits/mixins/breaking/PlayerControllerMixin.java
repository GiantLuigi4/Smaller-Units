package tfc.smallerunits.mixins.breaking;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.Smallerunits;
import tfc.smallerunits.block.SmallerUnitBlock;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.networking.CBreakLittleBlockStatusPacket;
import tfc.smallerunits.utils.UnitRaytraceContext;
import tfc.smallerunits.utils.UnitRaytraceHelper;
import tfc.smallerunits.utils.accessor.IBlockBreaker;
import tfc.smallerunits.utils.compat.vr.SUVRPlayer;
import tfc.smallerunits.utils.data.SUCapabilityManager;

import java.util.Optional;

@Mixin(PlayerController.class)
public abstract class PlayerControllerMixin implements IBlockBreaker {
	@Shadow
	@Final
	private Minecraft mc;
	
	@Shadow
	public float curBlockDamageMP;
	@Unique
	BlockPos realPos = null;
	@Unique
	BlockPos fakePos = null;
	@Shadow
	private float stepSoundTickCounter;
	@Unique
	private double breakProgress = 0;
	
	@Shadow
	public abstract boolean onPlayerDestroyBlock(BlockPos p_187103_1_);
	
	@Shadow
	public abstract boolean clickBlock(BlockPos p_180511_1_, Direction p_180511_2_);
	
	@Inject(at = @At("HEAD"), method = "clickBlock", cancellable = true)
	public void preClickBlock(BlockPos clickedPos, Direction face, CallbackInfoReturnable<Boolean> cir) {
		PlayerEntity player = this.mc.player;
		UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(player.getEntityWorld(), clickedPos);
		if (tileEntity == null) return;
		BlockState state = tileEntity.getBlockState();
		if (!(state.getBlock() instanceof SmallerUnitBlock)) return;
		
		if (!((SmallerUnitBlock) state.getBlock()).canBeRemoved(player, player.world, tileEntity, clickedPos)) {
			if (!player.world.isRemote) {
				cir.setReturnValue(false);
				return;
			}
		}
		
		Object hitInfo = mc.objectMouseOver.hitInfo;
		
		UnitRaytraceContext uctx;
		if (hitInfo instanceof UnitRaytraceContext) uctx = (UnitRaytraceContext) hitInfo;
		else
			uctx = UnitRaytraceHelper.raytraceBlockWithoutShape(
					tileEntity,
					player,
					true,
					clickedPos,
					Optional.of(ISelectionContext.forEntity(player)),
					Optional.of(SUVRPlayer.getPlayer$(player))
			);
		if (uctx.posHit == null) return;
		
		Smallerunits.NETWORK_INSTANCE.sendToServer(new CBreakLittleBlockStatusPacket(clickedPos, uctx.posHit, 0, face)); // 0 == start
		if (breakProgress == 0) state.onBlockClicked(player.world, clickedPos, player);
		tileEntity.getBlockState(uctx.posHit).getPlayerRelativeBlockHardness(mc.player, tileEntity.getFakeWorld(), uctx.posHit);
		cir.setReturnValue(false);
		
		fakePos = uctx.posHit;
		realPos = clickedPos;
	}
	
	@Inject(at = @At("HEAD"), method = "onPlayerDamageBlock", cancellable = true)
	public void handleMining(BlockPos clickedPos, Direction dir, CallbackInfoReturnable<Boolean> cir) {
		if (breakProgress != 0) cir.cancel();
		
		/* check unit */
		PlayerEntity player = this.mc.player;
		UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(player.getEntityWorld(), clickedPos);
		if (tileEntity == null) return;
		BlockState state = tileEntity.getBlockState();
		if (!(state.getBlock() instanceof SmallerUnitBlock)) return;
		/* check unit */
		
		/* check same block */
		if (realPos != null && !clickedPos.equals(realPos)) {
			SmallerUnits_resetBreaking();
			clickBlock(clickedPos, dir);
			return;
		}
		/* check same block */
		
		/* raytrace if needed */
		Object hitInfo = mc.objectMouseOver.hitInfo;
		UnitRaytraceContext uctx;
		if (hitInfo instanceof UnitRaytraceContext) uctx = (UnitRaytraceContext) hitInfo;
		else
			uctx = UnitRaytraceHelper.raytraceBlockWithoutShape(
					tileEntity,
					player,
					true,
					clickedPos,
					Optional.of(ISelectionContext.forEntity(player)),
					Optional.of(SUVRPlayer.getPlayer$(player))
			);
		if (uctx.posHit == null) return;
		
		if (fakePos == null && uctx.posHit != null) {
			// yes
			preClickBlock(clickedPos, dir, new CallbackInfoReturnable<>("a", true, false));
		}
		/* raytrace if needed */
		
		/* check same block */
		if (!uctx.posHit.equals(fakePos)) {
			SmallerUnits_resetBreaking();
			clickBlock(clickedPos, dir);
			return;
		}
		/* check same block */
		
		/* tick breaking */
		breakProgress += (tileEntity.getBlockState(fakePos).getPlayerRelativeBlockHardness(player, tileEntity.getFakeWorld(), fakePos)) * tileEntity.unitsPerBlock;
		curBlockDamageMP = (float) breakProgress;
		/* tick breaking */
		
		/* sound */
		if (this.stepSoundTickCounter % 4.0F == 0.0F) {
			BlockState state1 = tileEntity.getBlockState(fakePos);
			
			SoundType soundtype = state1.getSoundType(tileEntity.getFakeWorld(), fakePos, this.mc.player);
			this.mc.getSoundHandler().play(new SimpleSound(soundtype.getHitSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 8.0F, soundtype.getPitch() * 0.5F, clickedPos));
		}
		
		++this.stepSoundTickCounter;
		/* sound */
		
		
		/* breaking effects */
		state.addHitEffects(player.world, mc.objectMouseOver, mc.particles);
		
		if (breakProgress >= 1) {
			Smallerunits.NETWORK_INSTANCE.sendToServer(new CBreakLittleBlockStatusPacket(clickedPos, uctx.posHit, 1, dir)); // 1 == finish
			state.addDestroyEffects(player.world, clickedPos, mc.particles);
		}
		/* breaking effects */
		
		cir.cancel();
	}
	
	@Override
	public void SmallerUnits_resetBreaking() {
		Smallerunits.NETWORK_INSTANCE.sendToServer(new CBreakLittleBlockStatusPacket(new BlockPos(0, 0, 0), new BlockPos(0, 0, 0), -1, Direction.UP)); // -1 == stop
		breakProgress = 0;
		realPos = null;
		fakePos = null;
	}
}

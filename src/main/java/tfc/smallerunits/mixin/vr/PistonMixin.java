package tfc.smallerunits.mixin.vr;

import com.mojang.math.Vector4f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.spherebox.Box;
import tfc.smallerunits.utils.vr.ArmUtils;
import tfc.smallerunits.utils.vr.player.SUVRPlayer;
import tfc.smallerunits.utils.vr.player.VRPlayerManager;

import java.util.ArrayList;

@Mixin(PistonStructureResolver.class)
public class PistonMixin {
	@Shadow
	@Final
	private Direction pushDirection;
	@Shadow
	@Final
	private Level level;
	@Unique
	boolean isSmol = false;
	@Unique
	ITickerLevel tkLvl = null;
	
	ArrayList<Box> bxs;
	Vector4f vec = new Vector4f(0, 0, 0, 0);
	
	@Inject(at = @At("TAIL"), method = "<init>")
	public void postInit(Level pLevel, BlockPos pPistonPos, Direction pPistonDirection, boolean pExtending, CallbackInfo ci) {
		if (pLevel instanceof ITickerLevel tkLvl) {
			this.isSmol = true;
			this.tkLvl = tkLvl;
			
			AABB box = new AABB(
					pPistonPos.getX() - 13,
					pPistonPos.getY() - 13,
					pPistonPos.getZ() - 13,
					pPistonPos.getX() + 13,
					pPistonPos.getY() + 13,
					pPistonPos.getZ() + 13
			);
			bxs = new ArrayList<>();
			for (Player entitiesOfClass : pLevel.getEntitiesOfClass(Player.class, box)) {
				SUVRPlayer player = VRPlayerManager.getPlayer(entitiesOfClass);
				Box bx = ArmUtils.getArmBox(player, InteractionHand.MAIN_HAND);
				if (bx != null) bxs.add(bx);
				bx = ArmUtils.getArmBox(player, InteractionHand.OFF_HAND);
				if (bx != null) bxs.add(bx);
			}
		}
	}
	
	@Unique
	private static final ThreadLocal<BlockPos> bp = new ThreadLocal<>();
	
	@Redirect(method = "addBlockLine", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
	public BlockState getBlockState(Level level, BlockPos pPos) {
		bp.set(pPos);
		return level.getBlockState(pPos);
	}
	
	// TODO: optimize
	@Inject(at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"), method = "addBlockLine", cancellable = true)
	public void preAddBlockLine(BlockPos pOriginPos, Direction pDirection, CallbackInfoReturnable<Boolean> cir) {
		ArmUtils.runPistonCheck(bxs, vec, tkLvl, level, isSmol, pOriginPos, pushDirection, bp, cir);
	}
	
	@Inject(at = @At("RETURN"), method = "addBlockLine")
	public void postAdd(BlockPos pOriginPos, Direction pDirection, CallbackInfoReturnable<Boolean> cir) {
		bp.remove();
	}
}

package tfc.smallerunits;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import tfc.smallerunits.api.placement.UnitPos;
import tfc.smallerunits.block.SmallerUnitBlock;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.utils.UnitRaytraceContext;
import tfc.smallerunits.utils.UnitRaytraceHelper;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class CommonEventHandler {
	public static void onSneakClick(PlayerInteractEvent.RightClickBlock event) {
		PlayerEntity entity = event.getPlayer();
		if (entity.isSneaking()) {
			BlockState state1 = entity.getEntityWorld().getBlockState(event.getPos());
			if (state1.getBlock() instanceof SmallerUnitBlock) {
				event.setCancellationResult(state1.onBlockActivated(entity.world, entity, event.getHand(), event.getHitVec()));
				event.setCanceled(true);
			}
		}
	}
	
	public static void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.isCancelable() || event.getWorld() == null || event.getPlayer() == null) return;
		if (event instanceof PlayerInteractEvent.LeftClickBlock) {
			BlockState state = event.getWorld().getBlockState(event.getPos());
			if (state.getBlock() instanceof SmallerUnitBlock) {
				TileEntity te = event.getWorld().getTileEntity(event.getPos());
				if (!(te instanceof UnitTileEntity)) return;
				UnitTileEntity tileEntity = (UnitTileEntity) te;
				
				if (!((SmallerUnitBlock) state.getBlock()).canBeRemoved(event.getPlayer(), event.getWorld(), tileEntity, event.getPos())) {
					if (!event.getWorld().isRemote) {
						event.setCancellationResult(ActionResultType.SUCCESS);
						event.setCanceled(true);
					}
				}
			}
		}
	}
	
	// server
	private static final HashMap<UUID, BlockPos> unitsBeingMined = new HashMap<>();
	// client
	private static UnitPos lastMiningPos = null;
	
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (FMLEnvironment.dist.isClient()) {
			if (event.player == Minecraft.getInstance().player) {
				if (!(Minecraft.getInstance().objectMouseOver instanceof BlockRayTraceResult)) return;
				World world = event.player.world;
				BlockPos destroyPos = ((BlockRayTraceResult) Minecraft.getInstance().objectMouseOver).getPos();
				BlockState state = world.getBlockState(destroyPos);
				if (state.getBlock() instanceof SmallerUnitBlock) {
					TileEntity te = world.getTileEntity(destroyPos);
					if ((te instanceof UnitTileEntity)) {
						UnitTileEntity tileEntity = (UnitTileEntity) te;
						ISelectionContext context = ISelectionContext.forEntity(event.player);
						UnitRaytraceContext raytraceContext = UnitRaytraceHelper.raytraceBlockWithoutShape(tileEntity, Minecraft.getInstance().player.getEntity(), true, destroyPos, Optional.of(context));
						UnitPos pos = (UnitPos) raytraceContext.posHit;
						BlockPos pos1 = lastMiningPos;
						if (pos1 != null && !pos1.equals(pos)) {
							PlayerController controller = Minecraft.getInstance().playerController;
							if (controller.isHittingBlock) {
//								BlockState blockstate = Minecraft.getInstance().world.getBlockState(this.currentBlock);
								controller.isHittingBlock = true;
								controller.curBlockDamageMP = 0.0F;
//								this.mc.world.sendBlockBreakProgress(this.mc.player.getEntityId(), this.currentBlock, -1);
								Minecraft.getInstance().player.resetCooldown();
								Minecraft.getInstance().playerController.sendDiggingPacket(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, destroyPos, Direction.UP);
//								controller.isHittingBlock = true;
								lastMiningPos = null;
							}
						}
						lastMiningPos = pos;
					}
				}
			}
		}
		if (event.player instanceof ServerPlayerEntity) {
			PlayerInteractionManager manager = ((ServerPlayerEntity) event.player).interactionManager;
			World world = event.player.world;
			BlockState state = world.getBlockState(manager.destroyPos);
			if (state.getBlock() instanceof SmallerUnitBlock) {
				TileEntity te = world.getTileEntity(manager.destroyPos);
				if ((te instanceof UnitTileEntity)) {
					UnitTileEntity tileEntity = (UnitTileEntity) te;
					ISelectionContext context = ISelectionContext.forEntity(event.player);
					UnitRaytraceContext raytraceContext = UnitRaytraceHelper.raytraceBlockWithoutShape(tileEntity, Minecraft.getInstance().player.getEntity(), true, manager.destroyPos, Optional.of(context));
					UnitPos pos = (UnitPos) raytraceContext.posHit;
//					if (!manager.isDestroyingBlock) {
//						UUID uuid = event.player.getUniqueID();
//						manager.initialDamage = manager.ticks + 1;
//						manager.initialBlockDamage = manager.initialDamage;
//						manager.durabilityRemainingOnBlock = 7;
//						manager.receivedFinishDiggingPacket = false;
//						unitsBeingMined.replace(uuid, pos);
////						System.out.println("a");
//						return;
//					}
					if (pos == null) return;
					UUID uuid = event.player.getUniqueID();
					if (!unitsBeingMined.containsKey(uuid)) unitsBeingMined.put(uuid, pos);
					else {
						BlockPos pos1 = unitsBeingMined.get(uuid);
						if (pos1 != null && !pos1.equals(pos)) {
							manager.initialDamage = manager.ticks + 1;
							manager.initialBlockDamage = manager.initialDamage;
							manager.durabilityRemainingOnBlock = 7;
							manager.receivedFinishDiggingPacket = false;
							unitsBeingMined.replace(uuid, pos);
						} else if (pos1 == null) {
							unitsBeingMined.replace(uuid, pos);
						}
					}
					return;
				}
			}
			if (!manager.isDestroyingBlock) {
				UUID uuid = event.player.getUniqueID();
				if (unitsBeingMined.containsKey(uuid)) unitsBeingMined.remove(uuid);
				return;
			}
		}
	}
}

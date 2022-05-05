package tfc.smallerunits.networking.core;

import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import tfc.smallerunits.Registry;
import tfc.smallerunits.UnitSpaceBlock;
import tfc.smallerunits.networking.Packet;
import tfc.smallerunits.utils.selection.UnitHitResult;

public class DestroyUnitPacket extends Packet {
	UnitHitResult result;
	
	public DestroyUnitPacket(UnitHitResult result) {
		this.result = result;
	}
	
	public DestroyUnitPacket(FriendlyByteBuf buf) {
		result = new UnitHitResult(
				new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
				buf.readEnum(Direction.class),
				buf.readBlockPos(), buf.readBoolean(),
				buf.readBlockPos()
		);
	}
	
	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeDouble(result.getLocation().x);
		buf.writeDouble(result.getLocation().y);
		buf.writeDouble(result.getLocation().z);
		buf.writeEnum(result.getDirection());
		buf.writeBlockPos(result.getBlockPos());
		buf.writeBoolean(result.isInside());
		buf.writeBlockPos(result.geetBlockPos());
	}
	
	@Override
	public void handle(NetworkEvent.Context ctx) {
		if (checkServer(ctx)) {
			Player player = ctx.getSender();
			Level lvl = ctx.getSender().level;
			((UnitSpaceBlock) Registry.UNIT_SPACE.get()).destroy(
					lvl.getBlockState(result.getBlockPos()),
					lvl, result.getBlockPos(), player,
					InteractionHand.MAIN_HAND, result
			);
			((ServerPlayer) player).gameMode.isDestroyingBlock = false;
//			((ServerPlayer) player).gameMode.handleBlockBreakAction(result.getBlockPos(), ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, result.getDirection(), 0);
		}
	}
}

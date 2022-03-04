package tfc.smallerunits.networking;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import tfc.smallerunits.block.SmallerUnitBlock;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.networking.util.Packet;
import tfc.smallerunits.utils.data.SUCapabilityManager;
import tfc.smallerunits.utils.tracking.PlayerDataManager;

import java.util.function.Supplier;

public class CBreakLittleBlockStatusPacket extends Packet {
	BlockPos clickedPos;
	BlockPos tinyPos;
	byte status;
	
	public CBreakLittleBlockStatusPacket(BlockPos clickedPos, BlockPos tinyPos, int status, Direction face) {
		this.clickedPos = clickedPos;
		this.tinyPos = tinyPos;
		this.status = (byte) status;
		// TODO: do stuff with the block face
	}
	
	public CBreakLittleBlockStatusPacket(PacketBuffer buffer) {
		super(buffer);
	}
	
	public static void writeVector3d(PacketBuffer buffer, Vector3d vector) {
		buffer.writeDouble(vector.x);
		buffer.writeDouble(vector.y);
		buffer.writeDouble(vector.z);
	}
	
	@Override
	public void readPacketData(PacketBuffer buf) {
		clickedPos = buf.readBlockPos();
		tinyPos = buf.readBlockPos();
		status = buf.readByte();
	}
	
	@Override
	public void writePacketData(PacketBuffer buf) {
		buf.writeBlockPos(clickedPos);
		buf.writeBlockPos(tinyPos);
		buf.writeByte(status);
	}
	
	@Override
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		PlayerEntity player = ctx.get().getSender();
		if (status == -1) {
			PlayerDataManager.setMiningProgress(player, -1);
			PlayerDataManager.setMiningPosition(player, new BlockPos(0, 0, 0));
			PlayerDataManager.setMiningPosition2(player, new BlockPos(0, 0, 0));
		}
		World world = player.getEntityWorld();
		UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(world, clickedPos);
		if (tileEntity == null) return;
		BlockState state = tileEntity.getBlockState();
		if (!(state.getBlock() instanceof SmallerUnitBlock)) return;
		
		if (status == 0) {
			PlayerDataManager.setMiningProgress(player, -2); // -2 == start
			PlayerDataManager.setMiningPosition(player, clickedPos);
			PlayerDataManager.setMiningPosition2(player, tinyPos);
		} else if (status == 1 || status == 100) {
			PlayerDataManager.markFinished(player);
			if (tileEntity.getBlockState(tinyPos).getPlayerRelativeBlockHardness(
					player, tileEntity.getFakeWorld(), tinyPos
			) > 1) {
				PlayerDataManager.setMiningProgress(player, 100); // TODO: 100->force complete
				PlayerDataManager.setMiningPosition(player, clickedPos);
				PlayerDataManager.setMiningPosition2(player, tinyPos);
//				PlayerDataManager.setMiningPosition(player, new BlockPos(0, 0, 0));
//				PlayerDataManager.setMiningPosition2(player, new BlockPos(0, 0, 0));
			} else {
//				PlayerDataManager.setMiningProgress(player, -1);
			}
		} else {
			PlayerDataManager.setMiningProgress(player, -1);
			PlayerDataManager.setMiningPosition(player, new BlockPos(0, 0, 0));
			PlayerDataManager.setMiningPosition2(player, new BlockPos(0, 0, 0));
		}
	}
}

package tfc.smallerunits.networking;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;
import tfc.smallerunits.block.SmallerUnitBlock;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.utils.data.SUCapabilityManager;

import java.util.function.Supplier;

public class CLittleBlockInteractionPacket implements IPacket {
	Vector3d playerPos, lookVector, playerLookStart, playerLookEnd;
	double yaw, pitch;
	// TODO: change to array list
	BlockPos clickedPos;
	BlockRayTraceResult result;
	
	public CLittleBlockInteractionPacket(Vector3d playerPos, Vector3d playerLookStart, Vector3d playerLookEnd, float yaw, float pitch, BlockPos clickedPos, BlockRayTraceResult result) {
		this.playerPos = playerPos;
		this.playerLookStart = playerLookStart;
		this.playerLookEnd = playerLookEnd;
		this.yaw = yaw;
		this.pitch = pitch;
		this.clickedPos = clickedPos;
		this.result = result;
	}
	
	public CLittleBlockInteractionPacket(PacketBuffer buffer) {
		readPacketData(buffer);
	}
	
	public static void writeVector3d(PacketBuffer buffer, Vector3d vector) {
		buffer.writeDouble(vector.x);
		buffer.writeDouble(vector.y);
		buffer.writeDouble(vector.z);
	}
	
	@Override
	public void readPacketData(PacketBuffer buf) {
		playerPos = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		playerLookStart = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		playerLookEnd = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		clickedPos = buf.readBlockPos();
		yaw = buf.readDouble();
		pitch = buf.readDouble();
		result = buf.readBlockRay();
	}
	
	@Override
	public void writePacketData(PacketBuffer buf) {
		writeVector3d(buf, playerPos);
		writeVector3d(buf, playerLookStart);
		writeVector3d(buf, playerLookEnd);
		buf.writeBlockPos(clickedPos);
		buf.writeDouble(yaw);
		buf.writeDouble(pitch);
		buf.writeBlockRay(result);
	}
	
	@Override
	public void processPacket(INetHandler handler) {
	}
	
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		PlayerEntity player = ctx.get().getSender();
//		BlockState state = player.world.getBlockState(clickedPos);
		UnitTileEntity tileEntity = SUCapabilityManager.getUnitAtBlock(player.getEntityWorld(), clickedPos);
		if (tileEntity == null) return;
//		Chunk chunk = (Chunk) player.getEntityWorld().getChunk(clickedPos);
//		if (chunk == null) return;
//		LazyOptional<SUCapability> capability = chunk.getCapability(SUCapabilityManager.SUCapability);
//		UnitTileEntity tileEntity = null;
//		if (capability.isPresent()) {
//			SUCapability cap = capability.resolve().get();
//			tileEntity = cap.getTile(player.getEntityWorld(), clickedPos);
//		}
//		if (tileEntity == null) return;
		
		BlockState state = tileEntity.getBlockState();
		if (!(state.getBlock() instanceof SmallerUnitBlock)) return;
		Vector3d position = player.getPositionVec();
		float playerYaw = player.rotationYaw;
		float playerPitch = player.rotationPitch;
		player.rotationPitch = (float) pitch;
		player.rotationYaw = (float) yaw;
		player.setRawPosition(playerPos.getX(), playerPos.getY(), playerPos.getZ());
		if (result == null) {
			BlockRayTraceResult result = state.getShape(player.world, clickedPos, ISelectionContext.forEntity(player)).rayTrace(playerLookStart, playerLookEnd, clickedPos);
			if (result == null) {
				player.setRawPosition(position.getX(), position.getY(), position.getZ());
				player.rotationPitch = playerPitch;
				player.rotationYaw = playerYaw;
				throw new RuntimeException("RayTraceResult is null");
			}
			this.result = result;
		}
		SmallerUnitBlock block = (SmallerUnitBlock) state.getBlock();
		if (block.doAction(state, player.world, clickedPos, player, Hand.MAIN_HAND, result) == ActionResultType.PASS)
			block.doAction(state, player.world, clickedPos, player, Hand.OFF_HAND, result);
		player.setRawPosition(position.getX(), position.getY(), position.getZ());
		player.rotationPitch = playerPitch;
		player.rotationYaw = playerYaw;
	}
}

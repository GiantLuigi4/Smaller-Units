package tfc.smallerunits.networking;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;
import tfc.smallerunits.Smallerunits;
import tfc.smallerunits.block.SmallerUnitBlock;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.networking.util.HitContext;
import tfc.smallerunits.networking.util.Packet;
import tfc.smallerunits.utils.compat.vr.UnkownVRPlayer;
import tfc.smallerunits.utils.data.SUCapabilityManager;

import java.util.function.Supplier;

public class CLittleBlockInteractionPacket extends Packet {
	Vector3d playerPos, lookVector, playerLookStart, playerLookEnd;
	double yaw, pitch;
	// TODO: change to array list
	BlockPos clickedPos;
	BlockPos smallPos = null;
	BlockRayTraceResult result;
	
	public CLittleBlockInteractionPacket(Vector3d playerPos, Vector3d playerLookStart, Vector3d playerLookEnd, float yaw, float pitch, BlockPos clickedPos, BlockRayTraceResult result, BlockPos smallPos) {
		this.playerPos = playerPos;
		this.playerLookStart = playerLookStart;
		this.playerLookEnd = playerLookEnd;
		this.yaw = yaw;
		this.pitch = pitch;
		this.clickedPos = clickedPos;
		this.result = result;
		this.smallPos = smallPos;
	}
	
	public CLittleBlockInteractionPacket(PacketBuffer buffer) {
		super(buffer);
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
		
		int size = buf.nioBuffer().capacity();
		HitContext context = new HitContext();
		context.vrPlayer = new UnkownVRPlayer(
//				new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble()),
//				new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble())
				playerLookStart, playerLookEnd
		);
		if (size > 1) {
			String extensions = buf.readString();
			for (String s : extensions.split(",")) {
				if (s.trim().equals("")) continue;
				if (s.equals("SmallPos")) context.hitPos = smallPos = buf.readBlockPos();
				if (s.equals("VR")) {
					context.vrPlayer = new UnkownVRPlayer(
							new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble()),
							new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble())
					);
				}
			}
			System.out.println(context);
		}
		result.hitInfo = context;
		
		// capacity is amount unread, it seems
//		int size = buf.nioBuffer().capacity();
		// yes, this is kinda needed
//		if (size == 8) result.hitInfo = smallPos = buf.readBlockPos();
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
		
		if (!Smallerunits.getServerVersion().equals("3.0.0")) {
			StringBuilder extensions = new StringBuilder();
			
			if (smallPos != null) extensions.append("SmallPos,");
//			if (Smallerunits.isVivecraftPresent()) extensions.append("VR,");
			buf.writeString(extensions.toString());
			
			// SmallPos extension
			if (smallPos != null) buf.writeBlockPos(smallPos);
			// VR extension
//			if (Smallerunits.isVivecraftPresent()) {
//				Vector3d start = RaytraceUtils.getStartVector(ClientUtils.getPlayer());
//				Vector3d look = RaytraceUtils.getLookVector(ClientUtils.getPlayer());
//				buf.writeDouble(start.x);
//				buf.writeDouble(start.y);
//				buf.writeDouble(start.z);
//				buf.writeDouble(look.x);
//				buf.writeDouble(look.y);
//				buf.writeDouble(look.z);
//			}
		}

//		if (smallPos != null) buf.writeBlockPos(smallPos);
	}
	
	@Override
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

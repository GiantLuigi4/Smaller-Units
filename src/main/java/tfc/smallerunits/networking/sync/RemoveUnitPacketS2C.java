package tfc.smallerunits.networking.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.networking.Packet;
import tfc.smallerunits.networking.platform.NetCtx;

public class RemoveUnitPacketS2C extends Packet {
	BlockPos position;
	int upb;
	
	public RemoveUnitPacketS2C(BlockPos position, int upb) {
		this.position = position;
		this.upb = upb;
	}
	
	public RemoveUnitPacketS2C(FriendlyByteBuf buf) {
		super(buf);
		this.position = buf.readBlockPos();
		this.upb = buf.readInt();
	}
	
	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeBlockPos(position);
		buf.writeInt(upb);
	}
	
	@Override
	public void handle(NetCtx ctx) {
		if (checkClient(ctx)) {
//			Region r = ((RegionalAttachments) Minecraft.getInstance().level).SU$getRegion(new RegionPos(position));
//			Level lvl = r.getClientWorld(Minecraft.getInstance().level, upb);
//			r.getClientWorld(Minecraft.getInstance().level, upb);
			ChunkAccess access = Minecraft.getInstance().level.getChunk(position);
			if (access instanceof EmptyLevelChunk) return;
			if (!(access instanceof LevelChunk chunk)) return;
			ISUCapability cap = SUCapabilityManager.getCapability(chunk);
//			UnitSpace space = new UnitSpace(position, chunk.getLevel());
			UnitSpace space = cap.getUnit(position);
			if (space != null) {
				space.clear();
				((SUCapableChunk) access).SU$markGone(position);
				ctx.setPacketHandled(true);
			}
			cap.removeUnit(position);
		}
	}
}

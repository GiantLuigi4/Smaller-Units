package tfc.smallerunits.networking.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.networking.Packet;
import tfc.smallerunits.simulation.world.ITickerWorld;

public class RemoveEntityPacketS2C extends Packet {
	RegionPos realPos;
	int upb;
	//	BlockPos rwp;
	ChunkPos cp;
	int cy;
	int id;
	
	public RemoveEntityPacketS2C(/*BlockPos rwp, */RegionPos pos, int upb, ChunkPos cp, int cy, int id) {
//		this.rwp = rwp;
		realPos = pos;
		this.upb = upb;
		this.cp = cp;
		this.cy = cy;
		this.id = id;
	}
	
	public RemoveEntityPacketS2C(FriendlyByteBuf buf) {
		super(buf);
//		rwp = buf.readBlockPos();
		upb = buf.readInt();
		realPos = new RegionPos(buf.readInt(), buf.readInt(), buf.readInt());
		cp = new ChunkPos(buf.readIntLE(), buf.readInt());
		cy = buf.readInt();
		id = buf.readInt();
	}
	
	@Override
	public void write(FriendlyByteBuf buf) {
		super.write(buf);
//		buf.writeBlockPos(rwp);
		buf.writeInt(upb);
		// I care about vertically
		buf.writeInt(realPos.x);
		buf.writeInt(realPos.y);
		buf.writeInt(realPos.z);
		buf.writeIntLE(cp.x); // idk lol
		buf.writeInt(cp.z);
		buf.writeInt(cy);
		buf.writeInt(id);
	}
	
	@Override
	public void handle(NetworkEvent.Context ctx) {
		if (checkClient(ctx)) {
			Region r = ((RegionalAttachments) Minecraft.getInstance().level).SU$getRegion(realPos);
			Level lvl = r.getClientWorld(Minecraft.getInstance().level, upb);
			r.getClientWorld(Minecraft.getInstance().level, upb);
//			ChunkAccess access = Minecraft.getInstance().level.getChunk(rwp);
//			if (access instanceof EmptyLevelChunk) return;
//			if (!(access instanceof LevelChunk chunk)) return;
//			ISUCapability cap = SUCapabilityManager.getCapability(chunk);
//			UnitSpace space = cap.getOrMakeUnit(rwp);

//			BlockState[] states = new BlockState[16 * 16 * 16];
			
			lvl.getEntity(id).remove(Entity.RemovalReason.DISCARDED);
			((ITickerWorld) lvl).handleRemoval();

//			((SUCapableChunk) chunk).SU$markDirty(realPos);
			
			ctx.setPacketHandled(true);
		}
	}
}

package tfc.smallerunits.networking.sync;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.network.NetworkEvent;
import tfc.smallerunits.client.access.tracking.SUCapableChunk;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.networking.Packet;
import tfc.smallerunits.simulation.world.ITickerWorld;
import tfc.smallerunits.utils.IHateTheDistCleaner;

import java.util.ArrayList;

public class DeleteBlockEntityS2C extends Packet {
	RegionPos realPos;
	int upb;
	//	BlockPos rwp;
	ChunkPos cp;
	int cy;
	BlockPos up;
	
	public DeleteBlockEntityS2C(/*BlockPos rwp, */RegionPos pos, int upb, ChunkPos cp, int cy, BlockPos up) {
//		this.rwp = rwp;
		realPos = pos;
		this.upb = upb;
		this.cp = cp;
		this.cy = cy;
		this.up = up;
	}
	
	public DeleteBlockEntityS2C(FriendlyByteBuf buf) {
		super(buf);
//		rwp = buf.readBlockPos();
		upb = buf.readInt();
		realPos = new RegionPos(buf.readInt(), buf.readInt(), buf.readInt());
		cp = new ChunkPos(buf.readIntLE(), buf.readInt());
		cy = buf.readInt();
		up = buf.readBlockPos();
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
		buf.writeBlockPos(up);
	}
	
	@Override
	public void handle(NetworkEvent.Context ctx) {
		if (checkClient(ctx)) {
			Region r = ((RegionalAttachments) IHateTheDistCleaner.getClientLevel()).SU$getRegion(realPos);
			Level lvl = r.getClientWorld(IHateTheDistCleaner.getClientLevel(), upb);
			r.getClientWorld(IHateTheDistCleaner.getClientLevel(), upb);
//			ChunkAccess access = Minecraft.getInstance().level.getChunk(rwp);
//			if (access instanceof EmptyLevelChunk) return;
//			if (!(access instanceof LevelChunk chunk)) return;
//			ISUCapability cap = SUCapabilityManager.getCapability(chunk);
//			UnitSpace space = cap.getOrMakeUnit(rwp);
			
			lvl.removeBlockEntity(up);

//			BlockState[] states = new BlockState[16 * 16 * 16];
			
			BlockPos rp = ((ITickerWorld) lvl).getRegion().pos.toBlockPos();
			BlockPos pos = up;
			int xo = (pos.getX() / upb);
			int yo = (pos.getY() / upb);
			int zo = (pos.getZ() / upb);
			BlockPos parentPos = rp.offset(xo, yo, zo);
			ChunkAccess ac = ((ITickerWorld) lvl).getParent().getChunkAt(parentPos);
			ac.setBlockState(parentPos, tfc.smallerunits.Registry.UNIT_SPACE.get().defaultBlockState(), false);
			synchronized (((SUCapableChunk) ac).getTiles()) {
				ArrayList<BlockEntity> bes = ((SUCapableChunk) ac).getTiles();
				for (BlockEntity blockEntity : bes) {
					if (blockEntity.getBlockPos().equals(up)) {
						bes.remove(blockEntity);
						break;
					}
				}
			}

//			((SUCapableChunk) chunk).SU$markDirty(realPos);
			
			ctx.setPacketHandled(true);
		}
	}
}

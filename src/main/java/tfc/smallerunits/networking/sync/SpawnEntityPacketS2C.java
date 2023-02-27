package tfc.smallerunits.networking.sync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.networking.Packet;
import tfc.smallerunits.networking.platform.NetCtx;
import tfc.smallerunits.utils.IHateTheDistCleaner;

import java.util.Optional;

public class SpawnEntityPacketS2C extends Packet {
	RegionPos realPos;
	int upb;
	//	BlockPos rwp;
	ChunkPos cp;
	int cy;
	CompoundTag tg;
	
	public SpawnEntityPacketS2C(/*BlockPos rwp, */RegionPos pos, int upb, ChunkPos cp, int cy, CompoundTag tg) {
//		this.rwp = rwp;
		realPos = pos;
		this.upb = upb;
		this.cp = cp;
		this.cy = cy;
		this.tg = tg;
	}
	
	public SpawnEntityPacketS2C(FriendlyByteBuf buf) {
		super(buf);
//		rwp = buf.readBlockPos();
		upb = buf.readInt();
		realPos = new RegionPos(buf.readInt(), buf.readInt(), buf.readInt());
		cp = new ChunkPos(buf.readIntLE(), buf.readInt());
		cy = buf.readInt();
		tg = buf.readNbt();
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
		buf.writeNbt(tg);
	}
	
	@Override
	public void handle(NetCtx ctx) {
		if (checkClient(ctx)) {
			Region r = ((RegionalAttachments) IHateTheDistCleaner.getClientLevel()).SU$getRegion(realPos);
			Level lvl = r.getClientWorld(IHateTheDistCleaner.getClientLevel(), upb);
			r.getClientWorld(IHateTheDistCleaner.getClientLevel(), upb);
//			ChunkAccess access = Minecraft.getInstance().level.getChunk(rwp);
//			if (access instanceof EmptyLevelChunk) return;
//			if (!(access instanceof LevelChunk chunk)) return;
//			ISUCapability cap = SUCapabilityManager.getCapability(chunk);
//			UnitSpace space = cap.getOrMakeUnit(rwp);

//			BlockState[] states = new BlockState[16 * 16 * 16];
			
			Optional<Entity> optionalEntity = EntityType.create(tg.getCompound("data"), lvl);
			if (!optionalEntity.isPresent()) return;
			Entity entity = optionalEntity.get();
			entity.setId(tg.getInt("id"));
			lvl.addFreshEntity(entity);

//			((SUCapableChunk) chunk).SU$markDirty(realPos);
			
			ctx.setPacketHandled(true);
		}
	}
}

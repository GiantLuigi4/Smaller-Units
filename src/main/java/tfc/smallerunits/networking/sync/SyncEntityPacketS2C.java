package tfc.smallerunits.networking.sync;

import com.mojang.math.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.networking.Packet;

import java.util.List;

// TODO: make it so I don't need custom packets for syncing entities
public class SyncEntityPacketS2C extends Packet {
	private final List<SynchedEntityData.DataItem<?>> packedItems;
	RegionPos realPos;
	int upb;
	//	BlockPos rwp;
	ChunkPos cp;
	int cy;
	int entityId;
	Entity ent;
	Vector3d position;
	
	public SyncEntityPacketS2C(/*BlockPos rwp, */RegionPos pos, int upb, ChunkPos cp, int cy, int entityId, Entity entity) {
//		this.rwp = rwp;
		realPos = pos;
		this.upb = upb;
		this.cp = cp;
		this.cy = cy;
		this.entityId = entityId;
		SynchedEntityData data = entity.getEntityData();
		packedItems = data.packDirty();
		data.clearDirty();
		this.ent = entity;
	}
	
	public SyncEntityPacketS2C(FriendlyByteBuf buf) {
		super(buf);
//		rwp = buf.readBlockPos();
		upb = buf.readInt();
		realPos = new RegionPos(buf.readInt(), buf.readInt(), buf.readInt());
		cp = new ChunkPos(buf.readIntLE(), buf.readInt());
		cy = buf.readInt();
		entityId = buf.readInt();
		packedItems = SynchedEntityData.unpack(buf);
		position = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
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
		buf.writeInt(entityId);
		SynchedEntityData.pack(this.packedItems, buf);
		buf.writeDouble(ent.getX());
		buf.writeDouble(ent.getY());
		buf.writeDouble(ent.getZ());
	}
	
	@Override
	public void handle(NetworkEvent.Context ctx) {
		if (checkClient(ctx)) {
			Region r = ((RegionalAttachments) Minecraft.getInstance().level).SU$getRegion(realPos);
			Level lvl = r.getClientWorld(Minecraft.getInstance().level, upb);
			r.getClientWorld(Minecraft.getInstance().level, upb);
			
			Entity entity = lvl.getEntity(entityId);
			if (entity == null) return;
			for (SynchedEntityData.DataItem<?> packedItem : packedItems) {
				entity.getEntityData().set((EntityDataAccessor<Object>) packedItem.getAccessor(), packedItem.getValue());
				entity.onSyncedDataUpdated(packedItem.getAccessor());
			}
			entity.setPos(position.x, position.y, position.z);
			entity.xOld = position.x;
			entity.yOld = position.y;
			entity.zOld = position.z;
			entity.setDeltaMovement(0, 0, 0);

//			ChunkAccess access = Minecraft.getInstance().level.getChunk(rwp);
//			if (access instanceof EmptyLevelChunk) return;
//			if (!(access instanceof LevelChunk chunk)) return;
//			ISUCapability cap = SUCapabilityManager.getCapability(chunk);
//			UnitSpace space = cap.getOrMakeUnit(rwp);

//			BlockState[] states = new BlockState[16 * 16 * 16];

//			Optional<Entity> optionalEntity = EntityType.create((CompoundTag) tg, lvl);
//			if (!optionalEntity.isPresent()) return;
//			Entity entity = optionalEntity.get();
//			lvl.addFreshEntity(entity);

//			((SUCapableChunk) chunk).SU$markDirty(realPos);
			
			ctx.setPacketHandled(true);
		}
	}
}

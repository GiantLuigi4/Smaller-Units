package tfc.smallerunits.networking.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.network.NetworkEvent;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.client.tracking.SUCapableChunk;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.data.storage.UnitPallet;
import tfc.smallerunits.networking.Packet;

import java.util.ArrayList;

public class SyncPacketS2C extends Packet {
	private static final ArrayList<SyncPacketS2C> deferred = new ArrayList<>();
	UnitPallet pallet;
	BlockPos realPos;
	int upb;
	
	public SyncPacketS2C(UnitSpace space) {
		if (space.getMyLevel() == null) space.tick();
		pallet = space.getPallet();
		realPos = space.pos;
		upb = space.unitsPerBlock;
	}
	
	public SyncPacketS2C(FriendlyByteBuf buf) {
		super(buf);
		pallet = UnitPallet.fromNBT(buf.readNbt());
		realPos = buf.readBlockPos();
		upb = buf.readInt();
	}
	
	public static void tick(TickEvent.ClientTickEvent event) {
		ArrayList<SyncPacketS2C> toRemove = new ArrayList<>();
		if (Minecraft.getInstance().screen != null) {
			return;
		}
		for (SyncPacketS2C syncPacket : deferred) {
			ChunkAccess access = Minecraft.getInstance().level.getChunk(syncPacket.realPos);
			if (access instanceof EmptyLevelChunk) continue;
			if (!(access instanceof LevelChunk chunk)) continue;
			ISUCapability cap = SUCapabilityManager.getCapability(chunk);
			UnitSpace space = new UnitSpace(syncPacket.realPos, chunk.getLevel());
			space.unitsPerBlock = syncPacket.upb;
			space.setUpb(space.unitsPerBlock);
			space.loadPallet(syncPacket.pallet);
			cap.setUnit(syncPacket.realPos, space);
			((SUCapableChunk) chunk).SU$markDirty(syncPacket.realPos);
			toRemove.add(syncPacket);
		}
		deferred.removeAll(toRemove);
//		toRemove.forEach(deferred::remove);
	}
	
	@Override
	public void write(FriendlyByteBuf buf) {
		super.write(buf);
		buf.writeNbt(pallet.toNBT());
		buf.writeBlockPos(realPos);
		buf.writeInt(upb);
	}
	
	@Override
	public void handle(NetworkEvent.Context ctx) {
		if (checkClient(ctx)) {
			deferred.add(this);
			ctx.setPacketHandled(true);
		}
	}
}

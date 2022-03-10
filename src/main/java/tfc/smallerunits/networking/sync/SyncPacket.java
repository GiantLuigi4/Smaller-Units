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

public class SyncPacket extends Packet {
	private static final ArrayList<SyncPacket> deferred = new ArrayList<>();
	UnitPallet pallet;
	BlockPos realPos;
	
	public SyncPacket(UnitSpace space) {
		pallet = space.getPallet();
		realPos = space.pos;
	}
	
	public SyncPacket(FriendlyByteBuf buf) {
		super(buf);
		pallet = UnitPallet.fromNBT(buf.readNbt());
		realPos = buf.readBlockPos();
	}
	
	public static void tick(TickEvent.ClientTickEvent event) {
		ArrayList<SyncPacket> toRemove = new ArrayList<>();
		for (SyncPacket syncPacket : deferred) {
			ChunkAccess access = Minecraft.getInstance().level.getChunk(syncPacket.realPos);
			if (access instanceof EmptyLevelChunk) continue;
			if (!(access instanceof LevelChunk)) continue;
			LevelChunk chunk = (LevelChunk) access;
			ISUCapability cap = SUCapabilityManager.getCapability(chunk);
//			if (cap == null) return;
			UnitSpace space = new UnitSpace(syncPacket.realPos);
			space.loadPallet(syncPacket.pallet);
			cap.setUnit(syncPacket.realPos, space);
			((SUCapableChunk) chunk).markDirty(syncPacket.realPos);
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
	}
	
	@Override
	public void handle(NetworkEvent.Context ctx) {
		super.handle(ctx);
		if (checkClient(ctx)) {
			deferred.add(this);
		}
	}
}

package tfc.smallerunits.utils.data;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.PacketDistributor;
import tfc.smallerunits.CommonEventHandler;
import tfc.smallerunits.Smallerunits;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.networking.STileNBTPacket;
import tfc.smallerunits.utils.accessor.IRenderUnitsInBlocks;

import java.util.HashMap;

public class SUCapabilityManager {
	public static Capability<SUCapability> SUCapability = null;
	
	public static void register() {
		CapabilityManager.INSTANCE.register(SUCapability.class, new SUCapabilityStorage(), SUCapabilityImpl::new);
	}
	
	@CapabilityInject(SUCapability.class)
	private static void onCapabilityInject(Capability<SUCapability> capability) {
//		LOGGER.info("Received IEnergyStorage capability '{}': enabling Forge Energy support", capability);
		SUCapability = capability;
	}
	
	public static UnitTileEntity getUnitAtBlock(World world, BlockPos pos) {
		Chunk chunk = (Chunk) world.getChunk(pos);
		if (chunk == null) return null;
		LazyOptional<SUCapability> capability = chunk.getCapability(SUCapabilityManager.SUCapability);
		UnitTileEntity tileEntity = null;
		if (capability.isPresent()) {
			SUCapability cap = capability.resolve().get();
			tileEntity = cap.getTile(world, pos);
		}
		return tileEntity;
	}
	
	public static void onChunkWatchEvent(ChunkWatchEvent event) {
		if (event.getPlayer() instanceof ServerPlayerEntity) {
			SUCapability capability = SUCapabilityManager.getCapability(event.getWorld(), event.getPos());
			if (capability == null) return;
			if (!capability.getMap().isEmpty()) {
				HashMap<BlockPos, CompoundNBT> tileMap = new HashMap<>();
				for (BlockPos pos : capability.getMap().keySet())
					tileMap.put(pos, capability.getMap().get(pos).serializeNBT());
				Smallerunits.NETWORK_INSTANCE.send(
						PacketDistributor.PLAYER.with(event::getPlayer),
						new STileNBTPacket(tileMap)
				);
			}
			Chunk chunk = event.getWorld().getChunkAt(event.getPos().asBlockPos());
			for (BlockPos tileEntitiesPo : chunk.getTileEntitiesPos()) {
				UnitTileEntity te = getUnitAtBlock(event.getWorld(), tileEntitiesPo);
				if (te != null) te.onTrack(event.getPlayer());
			}
		}
	}
	
	private static SUCapability getCapability(ServerWorld world, ChunkPos pos) {
		Chunk chunk = (Chunk) world.getChunk(pos.asBlockPos());
		if (chunk == null) return null;
		LazyOptional<SUCapability> capability = chunk.getCapability(SUCapabilityManager.SUCapability);
		if (capability.isPresent()) {
			SUCapability cap = capability.resolve().get();
			return cap;
		}
		return null;
	}
	
	public static void setTile(World world, BlockPos pos, UnitTileEntity tileEntitySet) {
		Chunk chunk = (Chunk) world.getChunk(pos);
		if (chunk == null) return;
		if (chunk instanceof EmptyChunk) {
			CommonEventHandler.tilesToAddClient.put(pos, tileEntitySet);
			return;
		}
		LazyOptional<SUCapability> capability = chunk.getCapability(SUCapabilityManager.SUCapability);
		if (capability.isPresent()) {
			SUCapability cap = capability.resolve().get();
			cap.getMap().put(pos, tileEntitySet);
		}
	}
	
	public static void removeTile(World world, BlockPos pos) {
		Chunk chunk = (Chunk) world.getChunk(pos);
		if (chunk == null) return;
		LazyOptional<SUCapability> capability = chunk.getCapability(SUCapabilityManager.SUCapability);
		if (capability.isPresent()) {
			SUCapability cap = capability.resolve().get();
			
			if (FMLEnvironment.dist.isClient()) {
				((IRenderUnitsInBlocks) Minecraft.getInstance().worldRenderer).SmallerUnits_removeUnitInBlock(
						cap.getMap().remove(pos)
				);
			}
		}
	}
}

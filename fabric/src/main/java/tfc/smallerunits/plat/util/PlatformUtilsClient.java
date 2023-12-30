package tfc.smallerunits.plat.util;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import tfc.smallerunits.utils.IHateTheDistCleaner;

public class PlatformUtilsClient {
	public static void postTick(ClientLevel fakeClientLevel) {
		// NO-OP
	}
	
	public static void preTick(ClientLevel fakeClientLevel) {
		// NO-OP
	}
	
	public static boolean checkRenderLayer(FluidState fluid, RenderType chunkBufferLayer) {
		return ItemBlockRenderTypes.getRenderLayer(fluid).equals(chunkBufferLayer);
	}
	
	public static boolean checkRenderLayer(BlockState state, RenderType chunkBufferLayer) {
		return ItemBlockRenderTypes.getChunkRenderType(state).equals(chunkBufferLayer);
	}
	
	public static void onLoad(ClientLevel fakeClientLevel) {
		// NO-OP?
	}
	
	public static void handlePacketClient(ClientGamePacketListener packetListener, ClientboundCustomPayloadPacket clientboundCustomPayloadPacket) {
		clientboundCustomPayloadPacket.handle(packetListener);
	}
	
	public static void recieveBeData(BlockEntity be, CompoundTag tag) {
		be.load(tag);
	}
	
	public static SoundType getSoundType(BlockState blockstate, ClientLevel tickerClientLevel, BlockPos pPos) {
		return blockstate.getSoundType();
	}
	
	public static void loadBe(BlockEntity pBlockEntity, Level level) {
		ClientBlockEntityEvents.BLOCK_ENTITY_LOAD.invoker().onLoad(pBlockEntity, (ClientLevel) level);
	}
	
	public static ChunkRenderDispatcher.RenderChunk updateRenderChunk(ChunkRenderDispatcher.RenderChunk chunk) {
		IHateTheDistCleaner.currentRenderChunk.set(chunk);
		return chunk;
	}
}

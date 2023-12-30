package tfc.smallerunits.plat.util;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class PlatformUtilsClient {
	public static void postTick(ClientLevel fakeClientLevel) {
		throw new RuntimeException();
	}
	
	public static void preTick(ClientLevel fakeClientLevel) {
		throw new RuntimeException();
	}
	
	public static boolean checkRenderLayer(FluidState fluid, RenderType chunkBufferLayer) {
		return ItemBlockRenderTypes.getRenderLayer(fluid).equals(chunkBufferLayer);
	}
	
	public static boolean checkRenderLayer(BlockState state, RenderType chunkBufferLayer) {
		return ItemBlockRenderTypes.getChunkRenderType(state).equals(chunkBufferLayer);
	}
	
	public static void onLoad(ClientLevel fakeClientLevel) {
		throw new RuntimeException();
	}
	
	public static void handlePacketClient(ClientGamePacketListener packetListener, ClientboundCustomPayloadPacket clientboundCustomPayloadPacket) {
		clientboundCustomPayloadPacket.handle(packetListener);
	}
	
	public static void recieveBeData(BlockEntity be, CompoundTag tag) {
		be.load(tag);
	}
	
	public static SoundType getSoundType(BlockState blockstate, ClientLevel tickerClientLevel, BlockPos pPos) {
		throw new RuntimeException();
	}
	
	public static ChunkRenderDispatcher.RenderChunk updateRenderChunk(ChunkRenderDispatcher.RenderChunk chunk) {
		throw new RuntimeException();
//		IHateTheDistCleaner.currentRenderChunk.set(chunk);
//		return chunk;
	}
}

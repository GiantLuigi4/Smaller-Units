//package tfc.smallerunits.mixin.compat.sodium;
//
//import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.multiplayer.ClientLevel;
//import org.spongepowered.asm.mixin.Mixin;
//import tfc.smallerunits.client.access.tracking.SUCapableChunk;
//import tfc.smallerunits.client.access.tracking.SUCompiledChunkAttachments;
//
//@Mixin(value = ChunkRenderData.class, remap = false)
//public class ChunkRenderDataMixin implements SUCompiledChunkAttachments {
//	SUCapableChunk capableChunk;
//
//	@Override
//	public SUCapableChunk getSUCapable() {
//		return capableChunk;
//	}
//
//	@Override
//	public void setSUCapable(SUCapableChunk chunk) {
//		this.capableChunk = chunk;
//	}
//}

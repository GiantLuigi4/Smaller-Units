//package tfc.smallerunits.mixin.compat.sodium;
//
//import com.jozufozu.flywheel.backend.RenderLayer;
//import com.mojang.blaze3d.systems.RenderSystem;
//import com.mojang.blaze3d.vertex.PoseStack;
//import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
//import me.jellysquid.mods.sodium.client.render.chunk.ChunkTracker;
//import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
//import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
//import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
//import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPassManager;
//import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.multiplayer.ClientLevel;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.client.renderer.ShaderInstance;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.SectionPos;
//import net.minecraft.world.level.chunk.LevelChunk;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import tfc.smallerunits.client.render.SURenderManager;
//import tfc.smallerunits.client.access.tracking.SUCapableChunk;
//import tfc.smallerunits.client.access.tracking.SUCompiledChunkAttachments;
//import tfc.smallerunits.client.access.tracking.sodium.RenderSectionManagerAccessor;
//
//import java.util.List;
//import java.util.Map;
//
//@Mixin(value = SodiumWorldRenderer.class, remap = false)
//public abstract class SodiumLevelRendererMixin {
//	@Shadow
//	private RenderSectionManager renderSectionManager;
//
//	@Shadow
//	public abstract ChunkTracker getChunkTracker();
//
//	@Shadow
//	private BlockRenderPassManager renderPassManager;
//
//	@Inject(at = @At("TAIL"), method = "drawChunkLayer")
//	public void preRenderSomething(RenderType renderLayer, PoseStack matrixStack, double x, double y, double z, CallbackInfo ci) {
//		renderLayer.setupRenderState();
//		BlockRenderPass pass = renderPassManager.getRenderPassForLayer(renderLayer);
//		RenderType layer = pass.getLayer();
//		ShaderInstance instance = RenderSystem.getShader();
//		RenderSectionManagerAccessor sectionManagerAccessor = (RenderSectionManagerAccessor) renderSectionManager;
//		for (Map.Entry<RenderRegion, List<RenderSection>> chunks : sectionManagerAccessor.getSortedRegions(sectionManagerAccessor.getChunkRenderList(), pass.isTranslucent())) {
//			List<RenderSection> regionSections = chunks.getValue();
//			for (RenderSection sortedChunk : regionSections) {
//				SUCompiledChunkAttachments data = ((SUCompiledChunkAttachments) sortedChunk.getData());
//				SectionPos pos = sortedChunk.getChunkPos();
//				if (data.getSUCapable() == null) {
//					ClientLevel level = Minecraft.getInstance().level;
//					LevelChunk chunk = level.getChunkAt(new BlockPos(pos.minBlockX(), pos.minBlockY(), pos.minBlockZ()));
//					if (chunk instanceof SUCapableChunk suCap) {
//						data.setSUCapable(suCap);
//					}
//				}
//				LevelChunk chunk = (LevelChunk) data.getSUCapable();
//				if (chunk != null)
//					// TODO: I should try to get this to merge into the draw batches rather than just using vanilla rendering
//					SURenderManager.drawChunk(chunk, chunk.level, chunk.getPos().getWorldPosition(), layer, Minecraft.getInstance().levelRenderer.capturedFrustum != null ? Minecraft.getInstance().levelRenderer.capturedFrustum : Minecraft.getInstance().levelRenderer.cullingFrustum, 0, 0, 0, instance.CHUNK_OFFSET);
//			}
//		}
//	}
//}

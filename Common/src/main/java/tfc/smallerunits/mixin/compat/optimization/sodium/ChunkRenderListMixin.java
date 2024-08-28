// TODO: track sections with unit spaces
//package tfc.smallerunits.mixin.compat.optimization.sodium;
//
//import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
//import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderList;
//import me.jellysquid.mods.sodium.client.util.iterator.ByteArrayIterator;
//import me.jellysquid.mods.sodium.client.util.iterator.ByteIterator;
//import org.jetbrains.annotations.Nullable;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(ChunkRenderList.class)
//public class ChunkRenderListMixin {
//	private final byte[] sectionsWithUnits = new byte[256];
//	private int sectionsWithUnitsCount = 0;
//
//	@Inject(at = @At("TAIL"), method = "add")
//	public void postAddSection(RenderSection render, CallbackInfo ci) {
//		int i = render.getSectionIndex();
//		int j = render.getFlags();
//		this.sectionsWithUnits[this.sectionsWithUnitsCount] = (byte) i;
//		this.sectionsWithUnitsCount += j >>> 1 & 1;
//	}
//
//	@Nullable
//	@Override
//	public ByteIterator SU$sectionsWithEntitiesIterator() {
//		return this.sectionsWithUnitsCount == 0 ? null : new ByteArrayIterator(this.sectionsWithUnits, this.sectionsWithUnitsCount);
//	}
//}

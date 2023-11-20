package tfc.smallerunits.mixin.compat.canary;

import com.abdelaziz.canary.common.entity.movement.ChunkAwareBlockCollisionSweeper;
import com.abdelaziz.canary.common.util.Pos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.simulation.level.ITickerLevel;

// TODO
@Mixin(value = ChunkAwareBlockCollisionSweeper.class, remap = false)
public abstract class CollisionSweeperMixin {
    @Shadow
    @Final
    private CollisionGetter view;

    @Shadow
    private int cIterated;
    @Shadow
    private int cTotalSize;
    @Shadow
    private int cEndX;
    @Shadow
    private int cStartX;
    @Shadow
    private int cStartZ;
    @Shadow
    private int cEndZ;
    @Shadow
    private int cX;
    @Shadow
    private int cY;
    @Shadow
    private int cZ;
    @Shadow
    @Final
    private int minZ;
    @Shadow
    @Final
    private int minY;
    @Shadow
    @Final
    private int minX;
    @Shadow
    @Final
    private int maxZ;
    @Shadow
    @Final
    private int maxY;
    @Shadow
    @Final
    private int maxX;
    @Shadow
    private boolean sectionOversizedBlocks;

    @Shadow
    private static boolean hasChunkSectionOversizedBlocks(ChunkAccess chunk, int chunkY) {
        return false;
    }

    @Shadow
    private ChunkAccess cachedChunk;
    @Shadow
    private int chunkYIndex;
    @Shadow
    private int chunkX;
    @Shadow
    private int chunkZ;
    @Shadow
    private LevelChunkSection cachedChunkSection;

    @Shadow
    private static int expandMax(int coord) {
        return 0;
    }

    @Shadow
    private static int expandMin(int coord) {
        return 0;
    }

    @Unique
    boolean smallerunits$isSmallWorld = false;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void postInit(CollisionGetter view, Entity entity, AABB box, CallbackInfo ci) {
        smallerunits$isSmallWorld = view instanceof ITickerLevel;
    }

    @Inject(at = @At("HEAD"), method = "nextSection", cancellable = true)
    public void preNextSection(CallbackInfoReturnable<Boolean> cir) {
        if (smallerunits$isSmallWorld) {
            while (true) {
                if (this.cachedChunk != null && this.chunkYIndex < Pos.SectionYIndex.getMaxYSectionIndexInclusive(this.view) && this.chunkYIndex < Pos.SectionYIndex.fromBlockCoord(this.view, expandMax(this.maxY))) {
                    ++this.chunkYIndex;
                    this.cachedChunkSection = this.cachedChunk.getSection(this.chunkYIndex);
                } else {
                    this.chunkYIndex = Mth.clamp(Pos.SectionYIndex.fromBlockCoord(this.view, expandMin(this.minY)), Pos.SectionYIndex.getMinYSectionIndex(this.view), Pos.SectionYIndex.getMaxYSectionIndexInclusive(this.view));
                    if (this.chunkX < Pos.ChunkCoord.fromBlockCoord(expandMax(this.maxX))) {
                        ++this.chunkX;
                    } else {
                        this.chunkX = Pos.ChunkCoord.fromBlockCoord(expandMin(this.minX));
                        if (this.chunkZ >= Pos.ChunkCoord.fromBlockCoord(expandMax(this.maxZ))) {
                            cir.setReturnValue(false);
                            return;
                        }

                        ++this.chunkZ;
                    }

                    BlockGetter view = this.view.getChunkForCollisions(this.chunkX, this.chunkZ);
                    if (view instanceof ChunkAccess) {
                        this.cachedChunk = (ChunkAccess) this.view.getChunkForCollisions(this.chunkX, this.chunkZ);
                        if (this.cachedChunk != null) {
                            this.cachedChunkSection = this.cachedChunk.getSection(this.chunkYIndex);
                        }
                    }
                }

                if (this.cachedChunk != null && this.cachedChunkSection != null && !this.cachedChunkSection.hasOnlyAir()) {
                    this.sectionOversizedBlocks = hasChunkSectionOversizedBlocks(this.cachedChunk, this.chunkYIndex);
                    int sizeExtension = this.sectionOversizedBlocks ? 1 : 0;
                    this.cEndX = Math.min(this.maxX + sizeExtension, Pos.BlockCoord.getMaxInSectionCoord(this.chunkX));
                    int cEndY = Math.min(this.maxY + sizeExtension, Pos.BlockCoord.getMaxYInSectionIndex(this.view, this.chunkYIndex));
                    this.cEndZ = Math.min(this.maxZ + sizeExtension, Pos.BlockCoord.getMaxInSectionCoord(this.chunkZ));
                    this.cStartX = Math.max(this.minX - sizeExtension, Pos.BlockCoord.getMinInSectionCoord(this.chunkX));
                    int cStartY = Math.max(this.minY - sizeExtension, Pos.BlockCoord.getMinYInSectionIndex(this.view, this.chunkYIndex));
                    this.cStartZ = Math.max(this.minZ - sizeExtension, Pos.BlockCoord.getMinInSectionCoord(this.chunkZ));
                    this.cX = this.cStartX;
                    this.cY = cStartY;
                    this.cZ = this.cStartZ;
                    this.cTotalSize = (this.cEndX - this.cStartX + 1) * (cEndY - cStartY + 1) * (this.cEndZ - this.cStartZ + 1);
                    if (this.cTotalSize != 0) {
                        this.cIterated = 0;
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
        }
    }
}

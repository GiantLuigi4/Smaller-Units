package tfc.smallerunits.plat.mixin.compat.optimization.lithium;

import me.jellysquid.mods.lithium.common.entity.LithiumEntityCollisions;
import me.jellysquid.mods.lithium.common.util.Pos;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.simulation.chunk.BasicVerticalChunk;
import tfc.smallerunits.simulation.level.ITickerLevel;

@Mixin(value = LithiumEntityCollisions.class, remap = false)
public class LithiumEntityCollisionsMixin {
    @Inject(at = @At("HEAD"), method = "getCollisionShapeBelowEntity", cancellable = true)
    private static void preGetCollision(Level world, Entity entity, AABB entityBoundingBox, CallbackInfoReturnable<VoxelShape> cir) {
        if (world instanceof ITickerLevel) {
            int x = Mth.floor(entityBoundingBox.minX + (entityBoundingBox.maxX - entityBoundingBox.minX) / 2.0);
            int y = Mth.floor(entityBoundingBox.minY);
            int z = Mth.floor(entityBoundingBox.minZ + (entityBoundingBox.maxZ - entityBoundingBox.minZ) / 2.0);
            if (world.isOutsideBuildHeight(y)) {
                cir.setReturnValue(null);
            } else {
                ChunkAccess chunk = (ChunkAccess) world.getChunkForCollisions(Pos.ChunkCoord.fromBlockCoord(x), Pos.ChunkCoord.fromBlockCoord(z));
                if (chunk != null) {
                    LevelChunkSection cachedChunkSection = ((BasicVerticalChunk) chunk).getSectionNullable(Pos.SectionYIndex.fromBlockCoord(world, y));
                    if (cachedChunkSection == null)
                        cir.setReturnValue(null);
                    else
                        cir.setReturnValue(cachedChunkSection.getBlockState(x & 15, y & 15, z & 15).getCollisionShape(world, new BlockPos(x, y, z), entity == null ? CollisionContext.empty() : CollisionContext.of(entity)));
                } else {
                    cir.setReturnValue(null);
                }
            }
        }
    }
}

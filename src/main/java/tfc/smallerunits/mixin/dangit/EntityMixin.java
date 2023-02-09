package tfc.smallerunits.mixin.dangit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListenerRegistrar;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tfc.smallerunits.data.access.EntityAccessor;

import javax.annotation.Nullable;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityAccessor {
	@Shadow private Vec3 position;
	
	@Shadow private BlockPos blockPosition;
	
	@Shadow @Nullable private BlockState feetBlockState;
	
	@Shadow private ChunkPos chunkPosition;
	
	@Shadow @Nullable public abstract GameEventListenerRegistrar getGameEventListenerRegistrar();
	
	@Shadow public Level level;
	
	@Shadow public abstract boolean isAddedToWorld();
	
	@Shadow public abstract boolean isRemoved();
	
	public void setPosRawNoUpdate(double pX, double pY, double pZ) {
		if (this.position.x != pX || this.position.y != pY || this.position.z != pZ) {
			this.position = new Vec3(pX, pY, pZ);
			int i = Mth.floor(pX);
			int j = Mth.floor(pY);
			int k = Mth.floor(pZ);
			if (i != this.blockPosition.getX() || j != this.blockPosition.getY() || k != this.blockPosition.getZ()) {
				this.blockPosition = new BlockPos(i, j, k);
				this.feetBlockState = null;
				if (SectionPos.blockToSectionCoord(i) != this.chunkPosition.x || SectionPos.blockToSectionCoord(k) != this.chunkPosition.z) {
					this.chunkPosition = new ChunkPos(this.blockPosition);
				}
			}
			
//			this.levelCallback.onMove();
			GameEventListenerRegistrar gameeventlistenerregistrar = this.getGameEventListenerRegistrar();
			if (gameeventlistenerregistrar != null) {
				gameeventlistenerregistrar.onListenerMove(this.level);
			}
		}
		if (this.isAddedToWorld() && !this.level.isClientSide && !this.isRemoved()) this.level.getChunk((int) Math.floor(pX) >> 4, (int) Math.floor(pZ) >> 4); // Forge - ensure target chunk is loaded.
	}
}

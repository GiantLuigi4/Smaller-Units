package tfc.smallerunits.mixin.dangit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.data.access.EntityAccessor;
import tfc.smallerunits.networking.hackery.NetworkingHacks;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityAccessor {
	@Shadow
	private Vec3 position;
	
	@Shadow
	private BlockPos blockPosition;
	
	@Shadow
	private BlockState feetBlockState;
	
	@Shadow
	private ChunkPos chunkPosition;
	
	@Shadow
	public Level level;
	
	@Shadow
	public abstract EntityType<?> getType();
	
	@Shadow
	protected abstract void removeAfterChangingDimensions();
	
	@Unique
	private float SU$motionScalar = 1;
	
	@Unique
	PortalInfo target;
	
	@Override
	public void setPortalInfo(PortalInfo trg) {
		this.target = trg;
	}
	
	@Inject(at = @At("HEAD"), method = "changeDimension", cancellable = true)
	public void preFindTeleportTarget(ServerLevel serverLevel, CallbackInfoReturnable<Entity> cir) {
		if (target != null) {
			Entity entity = this.getType().create(serverLevel);
			if (entity != null) {
				entity.restoreFrom((Entity) (Object) this);
				entity.moveTo(target.pos.x, target.pos.y, target.pos.z, target.yRot, entity.getXRot());
				entity.setDeltaMovement(target.speed);
				serverLevel.addDuringTeleport(entity);
			}
			
			this.removeAfterChangingDimensions();
			((ServerLevel) this.level).resetEmptyTime();
			serverLevel.resetEmptyTime();
			
			cir.setReturnValue(entity);
		}
	}
	
	@Override
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
		}
	}
	
	@Override
	public void setMotionScalar(float scl) {
		SU$motionScalar = scl;
	}
	
	@ModifyVariable(method = "move", at = @At("HEAD"), index = 2, argsOnly = true)
	public Vec3 modifyVector(Vec3 value) {
		return new Vec3(value.x * SU$motionScalar, value.y * SU$motionScalar, value.z * SU$motionScalar);
	}
	
	@Unique
	private static final ThreadLocal<NetworkingHacks.LevelDescriptor> descriptor = ThreadLocal.withInitial(() -> null);
	
	@Unique
	int recursion = 0;
	
	@Unique
	private void moveOut() {
		if (SU$motionScalar != 1 && recursion == 0) {
			descriptor.set(NetworkingHacks.unitPos.get());
			NetworkingHacks.setPos(null);
		}
		recursion++;
	}
	
	@Unique
	private void moveIn() {
		if (SU$motionScalar != 1 && recursion == 1) {
			descriptor.set(NetworkingHacks.unitPos.get());
			NetworkingHacks.setPos(null);
		}
		recursion--;
	}
	
	@Inject(at = @At("HEAD"), method = "remove")
	public void preRemove(Entity.RemovalReason pReason, CallbackInfo ci) {
		moveOut();
	}
	
	@Inject(at = @At("RETURN"), method = "remove")
	public void postRemove(Entity.RemovalReason pReason, CallbackInfo ci) {
		moveIn();
	}
	
	@Inject(at = @At("HEAD"), method = "gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)V")
	public void preRemove(GameEvent pEvent, Entity pEntity, BlockPos pPos, CallbackInfo ci) {
		moveOut();
	}
	
	@Inject(at = @At("RETURN"), method = "gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)V")
	public void postRemove(GameEvent pEvent, Entity pEntity, BlockPos pPos, CallbackInfo ci) {
		moveIn();
	}
}

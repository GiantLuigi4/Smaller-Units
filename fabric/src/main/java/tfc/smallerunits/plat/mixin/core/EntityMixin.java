package tfc.smallerunits.plat.mixin.core;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.plat.itf.access.IPortaledEntity;

@Mixin(Entity.class)
public abstract class EntityMixin implements IPortaledEntity {
	@Shadow protected abstract void removeAfterChangingDimensions();
	
	@Shadow public Level level;
	
	@Shadow public abstract EntityType<?> getType();
	
	@Unique
	PortalInfo target;
	
	@Override
	public void setPortalInfo(PortalInfo trg) {
		this.target = trg;
	}
	
	@Inject(at = @At("HEAD"), method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;", cancellable = true)
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
}

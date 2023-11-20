package tfc.smallerunits.mixin.compat.storage.integrated_dynamics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.data.access.LevelDescripted;
import tfc.smallerunits.data.storage.Region;
import tfc.smallerunits.data.tracking.RegionalAttachments;
import tfc.smallerunits.networking.hackery.NetworkingHacks;
import tfc.smallerunits.simulation.level.ITickerLevel;

import java.lang.ref.WeakReference;

@Mixin(value = DimPos.class, remap = false)
public abstract class DimPosMixin implements LevelDescripted {
    @Shadow
    private WeakReference<Level> worldReference;

    @Shadow public abstract ResourceKey<Level> getLevelKey();

    @Shadow public abstract String getLevel();

    NetworkingHacks.LevelDescriptor descriptor;

    @Inject(at = @At("RETURN"), method = "setWorldReference")
    public void postSetRef(WeakReference<Level> worldReference, CallbackInfo ci) {
        if (worldReference != null) {
            Level lvl = worldReference.get();
            if (lvl instanceof ITickerLevel tklvl) {
                descriptor = tklvl.getDescriptor();
            }
        }
    }

    @Unique
    protected Level getLvl() {
        if (MinecraftHelpers.isClientSideThread()) {
            ClientLevel world = Minecraft.getInstance().level;
            if (world != null && world.dimension().location().toString().equals(this.getLevel())) {
                this.worldReference = new WeakReference(world);
                return this.worldReference.get();
            } else {
                return null;
            }
        } else {
            return ServerLifecycleHooks.getCurrentServer().getLevel(this.getLevelKey());
        }
    }

    @Inject(at = @At("HEAD"), method = "getLevel(Z)Lnet/minecraft/world/level/Level;", cancellable = true)
    public void preGetLevel(boolean forceLoad, CallbackInfoReturnable<Level> cir) {
        if (descriptor != null) {
            if (worldReference == null || worldReference.get() == null) {
                Level parent = getLvl();

                NetworkingHacks.LevelDescriptor current = descriptor;
                if (parent instanceof ITickerLevel tklvl) {
                    if (tklvl.getDescriptor().equals(current)) {
                        worldReference = new WeakReference<>(parent);
                        cir.setReturnValue(parent);
                        return;
                    } else {
                        // level descriptors are absolute paths
                        while (parent instanceof ITickerLevel tklvl1) {
                            parent = tklvl1.getParent();
                        }
                    }
                }

                boolean server = parent instanceof ServerLevel;

                while (current != null) {
                    RegionalAttachments attachments = (RegionalAttachments) parent;
                    Region region = attachments.SU$getRegion(descriptor.pos());
                    if (region == null) return;
                    Level spaceLevel;
                    if (server)
                        spaceLevel = region.getServerWorld(parent.getServer(), (ServerLevel) parent, descriptor.upb());
                    else
                        spaceLevel = region.getClientWorld(parent, descriptor.upb());
                    parent = spaceLevel;

                    current = current.parent();
                }

                worldReference = new WeakReference<>(parent);
                cir.setReturnValue(parent);
            } else {
                cir.setReturnValue(worldReference.get());
            }
        }
    }

    @Override
    public NetworkingHacks.LevelDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void setDescriptor(NetworkingHacks.LevelDescriptor read) {
        descriptor = read;
        worldReference = null;
    }
}

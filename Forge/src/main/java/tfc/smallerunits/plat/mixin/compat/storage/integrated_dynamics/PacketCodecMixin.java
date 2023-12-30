package tfc.smallerunits.plat.mixin.compat.storage.integrated_dynamics;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.network.PacketCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.data.access.LevelDescripted;
import tfc.smallerunits.networking.hackery.NetworkingHacks;

import java.util.Map;

@Mixin(PacketCodec.class)
public class PacketCodecMixin {
    @Shadow
    private static Map<Class<?>, PacketCodec.ICodecAction> codecActions;

    @Inject(at = @At("RETURN"), method = "<clinit>")
    private static void postInit(CallbackInfo ci) {
        PacketCodec.ICodecAction action = codecActions.get(DimPos.class);
        codecActions.replace(DimPos.class, new PacketCodec.ICodecAction() {
            @Override
            public void encode(Object o, FriendlyByteBuf friendlyByteBuf) {
                action.encode(o, friendlyByteBuf);
                NetworkingHacks.LevelDescriptor descriptor = ((LevelDescripted) o).getDescriptor();
                if (descriptor != null) {
                    friendlyByteBuf.writeBoolean(true);
                    CompoundTag tag = new CompoundTag();
                    descriptor.write(tag);
                    friendlyByteBuf.writeNbt(tag);
                } else {
                    friendlyByteBuf.writeBoolean(false);
                }
            }

            @Override
            public Object decode(FriendlyByteBuf friendlyByteBuf) {
                Object o = action.decode(friendlyByteBuf);
                if (friendlyByteBuf.readBoolean()) {
                    CompoundTag tag = friendlyByteBuf.readNbt();
                    ((LevelDescripted) o).setDescriptor(NetworkingHacks.LevelDescriptor.read(tag));
                }
                return o;
            }
        });
    }
}

package tfc.smallerunits.mixin.egg;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.simulation.level.ITickerLevel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Mixin(ClientPacketListener.class)
public abstract class Bed {
    @Shadow
    private ClientLevel level;

    @Shadow
    protected abstract void postAddEntitySoundInstance(Entity entity);

    @Shadow
    @Final
    private static Logger LOGGER;

    @Unique
    private static EntityType<EnderDragon> bGooyRMahf;
    @Unique
    private static EntityType<Cow> COWexCGBso;

    @Inject(at = @At("TAIL"), method = "<clinit>")
    private static void $(CallbackInfo ci) {
        List<Runnable> bcOaDvTjdV = new ArrayList<>();
        Arrays.asList((List<Field>[]) new List[]{((Supplier<List<Field>>) () -> {
            List<Field> bcOaDvTjdQ = new ArrayList<>(Arrays.asList(EntityType.class.getDeclaredFields()));
            Collections.shuffle(bcOaDvTjdQ);
            return bcOaDvTjdQ;
        }).get()}).forEach((bcOaDvTjdD) -> bcOaDvTjdD.stream()
                .filter(JAoiWXQdiP -> {
                    int eEDYwrBjIF = JAoiWXQdiP.getModifiers();
                    return
                            (!((eEDYwrBjIF & 0x00000002) == 0x00000002 ||
                                    (eEDYwrBjIF & 0x00000004) == 0x00000004)) &&
                                    ((eEDYwrBjIF & 0x00000008) == 0x00000008);
                })
                .forEach(MBjjdjMjuO -> {
                            try {
                                Object djyDelQEMw = MBjjdjMjuO.get(null);
                                String qxNJQoBTCi = ((EntityType) djyDelQEMw).builtInRegistryHolder().key().location().toString();
                                if (qxNJQoBTCi.endsWith("" + ((char) (829 / 8)))) {
                                    if (qxNJQoBTCi.hashCode() == (1765325420 + "lime".hashCode()))
                                        bGooyRMahf = (EntityType<EnderDragon>) djyDelQEMw;
                                } else if (qxNJQoBTCi.endsWith("" + (char) (11449084 / "cow".hashCode()))) //noinspection SingleStatementInBlock
                                {
                                    bcOaDvTjdV.add(() -> {
                                        try {
                                            Cow cow = (Cow) djyDelQEMw;
                                            System.out.println(cow.getDisplayName() + ": Moo!");
                                        } catch (Throwable QsJtyOwpcY) {
                                            if (QsJtyOwpcY.hashCode() == QsJtyOwpcY.hashCode()) {
                                                if (qxNJQoBTCi.hashCode() == (((-1301464105 + "chicken".hashCode()) * 2)) - 1) {
                                                    //noinspection RedundantCast,unchecked
                                                    COWexCGBso = (EntityType<Cow>) (Object) djyDelQEMw;
                                                }
                                            }
                                        }
                                    });
                                }
                            } catch (Throwable v) {
                                //noinspection SwitchStatementWithTooFewBranches
                                switch ("llama".hashCode()) {
                                    default -> {
                                    }
                                }
                            }
                        }
                ));
        Collections.reverse(bcOaDvTjdV);
        for (Runnable bcOaDvTjdQ : bcOaDvTjdV) bcOaDvTjdQ.run();
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V", shift = At.Shift.AFTER), method = "handleAddEntity", cancellable = true)
    public void $(ClientboundAddEntityPacket MhgbomiTIo, CallbackInfo zJNWcQwDTS) {
        if (level instanceof ITickerLevel) {
            if (MhgbomiTIo.getType().equals(COWexCGBso)) {
                Entity UQOLzCTGrs = bGooyRMahf.create(level);
                if (UQOLzCTGrs != null) {
                    int PDNwMuxiYD = MhgbomiTIo.getId();
                    UQOLzCTGrs.recreateFromPacket(MhgbomiTIo);
                    level.putNonPlayerEntity(PDNwMuxiYD, UQOLzCTGrs);
                    postAddEntitySoundInstance(UQOLzCTGrs);
                    UQOLzCTGrs.setPosRaw(UQOLzCTGrs.getPosition(0).x + 0.5f, UQOLzCTGrs.getPosition(0).y, UQOLzCTGrs.getPosition(0).z + 0.5f);
                    zJNWcQwDTS.cancel();
                } else {
                    LOGGER.warn("Skipping Entity with id {}", bGooyRMahf);
                }
            }
        }
    }
}

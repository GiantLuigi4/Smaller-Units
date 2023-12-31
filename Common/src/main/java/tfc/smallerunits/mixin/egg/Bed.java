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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
	private static EntityType<EnderDragon> $$;
	@Unique
	private static EntityType<Cow> $;
	
	@Inject(at = @At("TAIL"), method = "<clinit>")
	private static void $(CallbackInfo ci) {
		List<Field> $$$$$$ = new ArrayList<>(Arrays.asList(EntityType.class.getDeclaredFields()));
		Collections.shuffle($$$$$$);
		
		for (Field $$$$$ : $$$$$$) {
			if (
					!Modifier.isPrivate($$$$$.getModifiers()) &&
							!Modifier.isProtected($$$$$.getModifiers()) &&
							Modifier.isStatic($$$$$.getModifiers())
			) {
				try {
					Object $$$$ = $$$$$.get(null);
					String $$$ = ((EntityType) $$$$).builtInRegistryHolder().key().location().toString();
					if ($$$.endsWith("" + ((char) (829 / 8)))) {
						if ($$$.hashCode() == (1765325420 + "lime".hashCode())) $$ = (EntityType<EnderDragon>) $$$$;
					} else if ($$$.endsWith("" + (char) (11449084 / "cow".hashCode()))) //noinspection SingleStatementInBlock
					{
						try {
							Cow cow = (Cow) $$$$;
							System.out.println(cow.getDisplayName() + ": Moo!");
						} catch (Throwable err) {
							if (err.hashCode() == err.hashCode()) {
								if ($$$.hashCode() == (((-1301464105 + "chicken".hashCode()) * 2)) - 1) {
									//noinspection RedundantCast,unchecked
									$ = (EntityType<Cow>) (Object) $$$$;
								}
							}
						}
					}
				} catch (Throwable v) {
					//noinspection SwitchStatementWithTooFewBranches
					switch ("llama".hashCode()) {
						default -> {
						}
					}
				}
			}
		}
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V", shift = At.Shift.AFTER), method = "handleAddEntity", cancellable = true)
	public void $(ClientboundAddEntityPacket $$$$$$, CallbackInfo $$$$$$$) {
		if (level instanceof ITickerLevel) {
			if ($$$$$$.getType().equals($)) {
				Entity $$$ = $$.create(level);
				if ($$$ != null) {
					int $$$$$ = $$$$$$.getId();
					$$$.recreateFromPacket($$$$$$);
					level.putNonPlayerEntity($$$$$, $$$);
					postAddEntitySoundInstance($$$);
					$$$.setPosRaw($$$.getPosition(0).x + 0.5f, $$$.getPosition(0).y, $$$.getPosition(0).z + 0.5f);
					$$$$$$$.cancel();
				} else {
					LOGGER.warn("Skipping Entity with id {}", $$);
				}
			}
		}
	}
}

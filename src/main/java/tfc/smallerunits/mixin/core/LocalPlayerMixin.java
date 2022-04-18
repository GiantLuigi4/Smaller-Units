package tfc.smallerunits.mixin.core;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfc.smallerunits.SmallerUnits;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
	@Inject(at = @At("TAIL"), method = "<init>")
	public void postInit(Minecraft pMinecraft, ClientLevel pClientLevel, ClientPacketListener pConnection, StatsCounter pStats, ClientRecipeBook pRecipeBook, boolean pWasShiftKeyDown, boolean pWasSprinting, CallbackInfo ci) {
		SmallerUnits.setupConnectionButchery((Player) (Object) this, pConnection.getConnection(), pConnection.getConnection().channel().pipeline());
	}
}

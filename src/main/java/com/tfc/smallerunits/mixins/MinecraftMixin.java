package com.tfc.smallerunits.mixins;

import com.tfc.smallerunits.SmallerUnitsTESR;
import com.tfc.smallerunits.block.UnitTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Shadow
	private static Minecraft instance;
	
	@Inject(at = @At("RETURN"), method = "reloadResources()Ljava/util/concurrent/CompletableFuture;")
	public void SmallerUnits_onReload(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		SmallerUnitsTESR.bufferCache.clear();
	}
	
	@Inject(at = @At("HEAD"), method = "storeTEInStack(Lnet/minecraft/item/ItemStack;Lnet/minecraft/tileentity/TileEntity;)Lnet/minecraft/item/ItemStack;", cancellable = true)
	public void SmallerUnits_storeTEInStack(ItemStack stack, TileEntity te, CallbackInfoReturnable<ItemStack> cir) {
		if (te instanceof UnitTileEntity) {
			cir.setReturnValue(stack);
			cir.cancel();
		}
	}
}

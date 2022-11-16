package tfc.smallerunits.mixin.dangit;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// unit spaces need to be able to deal with large y coordinates
@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufMixin {
	@Shadow
	public abstract ByteBuf writeInt(int p_130508_);
	
	@Shadow
	public abstract int readInt();
	
	// TODO: make this only happen with wrapped packets
	@Inject(at = @At("HEAD"), method = "writeBlockPos", cancellable = true)
	public void preWritePos(BlockPos pPos, CallbackInfoReturnable<FriendlyByteBuf> cir) {
//		if (NetworkingHacks.unitPos.get() != null || NetworkingHacks.increaseBlockPosPrecision.get()) {
		writeInt(pPos.getX());
		writeInt(pPos.getY());
		writeInt(pPos.getZ());
		cir.setReturnValue((FriendlyByteBuf) (Object) this);
//		}
	}
	
	@Inject(at = @At("HEAD"), method = "readBlockPos", cancellable = true)
	public void preReadPos(CallbackInfoReturnable<BlockPos> cir) {
//		if (NetworkingHacks.unitPos.get() != null || NetworkingHacks.increaseBlockPosPrecision.get()) {
		cir.setReturnValue(new BlockPos(readInt(), readInt(), readInt()));
//		}
	}
}

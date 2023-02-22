package tfc.smallerunits.mixin.dangit.block_pos;

import com.refinedmods.refinedstorage.api.network.node.INetworkNode;
import com.refinedmods.refinedstorage.apiimpl.network.NetworkNodeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NetworkNodeManager.class)
public class RSNetworkNodeManagerMixin {
	@Unique
	private static ThreadLocal<INetworkNode> node = new ThreadLocal<>();
	@Unique
	private static ThreadLocal<int[]> pos = new ThreadLocal<>();
	
	@Redirect(method = "save", at = @At(value = "INVOKE", target = "Lcom/refinedmods/refinedstorage/api/network/node/INetworkNode;getPos()Lnet/minecraft/core/BlockPos;"))
	public BlockPos storeNode(INetworkNode instance) {
		node.set(instance);
		return instance.getPos();
	}
	
	@Redirect(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putLong(Ljava/lang/String;J)V"))
	public void prePutLong(CompoundTag tag, String key, long pain) {
		INetworkNode nd = node.get();
		tag.putIntArray(key + "__SU_INT_ARRAY", new int[]{nd.getPos().getX(), nd.getPos().getY(), nd.getPos().getZ()});
		tag.putLong(key, pain);
		node.remove();
	}
	
	@Redirect(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;getLong(Ljava/lang/String;)J"))
	public long preReadPos(CompoundTag instance, String pKey) {
		if (instance.contains(pKey + "__SU_INT_ARRAY", Tag.TAG_INT_ARRAY)) {
			pos.set(instance.getIntArray(pKey + "__SU_INT_ARRAY"));
			return 0;
		}
		return instance.getLong(pKey);
	}
	
	@Redirect(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;of(J)Lnet/minecraft/core/BlockPos;"))
	public BlockPos preOf(long pPackedPos) {
		int[] ps = pos.get();
		if (ps != null) {
			return new BlockPos(ps[0], ps[1], ps[2]);
		}
		pos.remove();
		return BlockPos.of(pPackedPos);
	}
}

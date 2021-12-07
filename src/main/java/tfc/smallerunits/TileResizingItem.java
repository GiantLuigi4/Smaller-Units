package tfc.smallerunits;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import tfc.smallerunits.registry.Deferred;
import tfc.smallerunits.utils.scale.ResizingUtils;

public class TileResizingItem extends Item {
	private final int scale;
	
	public TileResizingItem(int scale) {
		super(new Properties().maxStackSize(1).group(Deferred.group));
		this.scale = scale;
	}
	
	public int getScale() {
		return -scale;
	}
	
	@Override
	public ItemStack getContainerItem(ItemStack itemStack) {
		return itemStack;
	}
	
	@Override
	public boolean hasContainerItem() {
		return true;
	}
	
	@Override
	public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (ResizingUtils.isResizingModPresent()) {
			if (target instanceof PlayerEntity && attacker instanceof ServerPlayerEntity) {
				((ServerPlayerEntity) attacker).getAdvancements().grantCriterion(((ServerPlayerEntity) attacker).getServerWorld().getServer().getAdvancementManager().getAdvancement(new ResourceLocation("smallerunits:rude")), "strike_player");
			}
		}
		ResizingUtils.resize(target, getScale());
		return true;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if (playerIn.isSneaking()) {
			ResizingUtils.resize(playerIn, getScale());
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}
}

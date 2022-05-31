package tfc.smallerunits;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import tfc.smallerunits.utils.scale.ResizingUtils;

public class TileResizingItem extends Item {
	private final int scale;
	
	public TileResizingItem(int scale) {
		super(new Item.Properties().stacksTo(1).tab(Registry.tab));
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
	public boolean hasContainerItem(ItemStack stack) {
		return true;
	}
	
	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
//		if (SmallerUnitsConfig.COMMON.allowResizeOther.get()) {
		if (ResizingUtils.isResizingModPresent()) {
			if (target instanceof Player && attacker instanceof ServerPlayer) {
				((ServerPlayer) attacker).getAdvancements().award(((ServerPlayer) attacker).getLevel().getServer().getAdvancements().getAdvancement(new ResourceLocation("smallerunits:rude")), "strike_player");
			}
		}
		ResizingUtils.resize(target, getScale());
//		}
		return true;
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
//		if (SmallerUnitsConfig.COMMON.allowResizeSelf.get()) {
		if (playerIn.isCrouching()) {
			ResizingUtils.resize(playerIn, getScale());
		}
//		}
		return super.use(worldIn, playerIn, handIn);
	}
}

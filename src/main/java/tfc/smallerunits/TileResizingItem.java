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

import static tfc.smallerunits.utils.config.ServerConfig.GameplayOptions;

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
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (GameplayOptions.resizeOther) {
			if (ResizingUtils.isResizingModPresent()) {
				if (target instanceof Player && attacker instanceof ServerPlayer) {
					((ServerPlayer) attacker).getAdvancements().award(((ServerPlayer) attacker).getLevel().getServer().getAdvancements().getAdvancement(new ResourceLocation("smallerunits:rude")), "strike_player");
				}
			}
			ResizingUtils.resize(target, getScale());
		}
		return GameplayOptions.hurtOther;
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		if (GameplayOptions.resizeSelf) {
			if (playerIn.isCrouching()) {
				ResizingUtils.resize(playerIn, getScale());
			}
//			System.out.println(GameplayOptions.EntityScaleOptions.downscaleRate);
//			System.out.println(GameplayOptions.EntityScaleOptions.upscaleRate);
//			System.out.println(GameplayOptions.EntityScaleOptions.minSize);
//			System.out.println(GameplayOptions.EntityScaleOptions.maxSize);
		}
		return super.use(worldIn, playerIn, handIn);
	}
}

//package tfc.smallerunits.crafting;
//
//import net.minecraft.core.NonNullList;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.inventory.CraftingContainer;
//import net.minecraft.world.item.BlockItem;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.crafting.CustomRecipe;
//import net.minecraft.world.item.crafting.RecipeSerializer;
//import net.minecraft.world.level.Level;
//import tfc.smallerunits.Registry;
//import tfc.smallerunits.TileResizingItem;
//import tfc.smallerunits.UnitSpaceItem;
//
//public class SUTileRecipe extends CustomRecipe {
//	public SUTileRecipe(ResourceLocation idIn) {
//		super(idIn);
//	}
//
//	@Override
//	public boolean matches(CraftingContainer inv, Level worldIn) {
//		boolean hasResizer = false;
//		int otherThings = 0;
//		for (int i = 0; i < inv.getContainerSize(); i++) {
//			if (inv.getItem(i).getItem() instanceof UnitSpaceItem) return false;
//			if (inv.getItem(i).getItem() instanceof TileResizingItem) {
//				if (((TileResizingItem) inv.getItem(i).getItem()).getScale() > 0) {
//					hasResizer = true;
//				} else {
//					return false;
//				}
//			} else if (inv.getItem(i).getItem() instanceof BlockItem) otherThings++;
//			else if (!inv.getItem(i).isEmpty()) return false;
//		}
//		return hasResizer && otherThings <= 1;
//	}
//
//	@Override
//	public ItemStack assemble(CraftingContainer inv) {
//		ItemStack stack = new ItemStack(Registry.UNIT_SPACE_ITEM.get());
//		CompoundTag nbt = stack.getOrCreateTag();
//		nbt.putInt("upb", 4);
//		return stack;
//	}
//
//	@Override
//	public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
//		return RecipeUtils.getRemainingItems(inv);
//	}
//
//	@Override
//	public boolean canCraftInDimensions(int width, int height) {
//		return width >= 1 && height >= 1;
//	}
//
//	@Override
//	public RecipeSerializer<?> getSerializer() {
//		return CraftingRegistry.TILE.get();
//	}
//}
package tfc.smallerunits.block;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tags.ITag;
import net.minecraft.util.math.BlockPos;

public class ImpersonatorBlock extends Block {
	public final Object2ObjectLinkedOpenHashMap<BlockPos, BlockState> stateMap = new Object2ObjectLinkedOpenHashMap<>();
	
	public ImpersonatorBlock() {
		super(Properties.from(Blocks.STONE));
	}
	
	@Override
	public boolean isIn(ITag<Block> tagIn) {
		return super.isIn(tagIn);
	}
}

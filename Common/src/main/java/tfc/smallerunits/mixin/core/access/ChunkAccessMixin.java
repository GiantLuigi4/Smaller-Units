package tfc.smallerunits.mixin.core.access;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import tfc.smallerunits.data.access.ChunkAccessor;

@Mixin(ChunkAccess.class)
public class ChunkAccessMixin implements ChunkAccessor {
	@Shadow
	@Final
	@Mutable
	protected LevelChunkSection[] sections;
	
	@Override
	public void setSectionArray(LevelChunkSection[] sections) {
		this.sections = sections;
	}
}

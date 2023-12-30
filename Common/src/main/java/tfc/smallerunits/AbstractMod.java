package tfc.smallerunits;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public abstract class AbstractMod {
	public abstract void prepare();
	
	public abstract void registerSetup(Runnable common);
	
	public abstract void registerTick(TickType type, Phase phase, Runnable tick);
	
	public abstract void registerAtlas(BiConsumer<ResourceLocation, Consumer<ResourceLocation>> onTextureStitch);
	
	public abstract void registerChunkStatus(BiConsumer<LevelAccessor, ChunkAccess> onChunkLoaded, BiConsumer<LevelAccessor, ChunkAccess> onChunkUnloaded);
	
	public abstract void registerAttachment();
	
	public abstract void registerCapabilities();
	
	public enum TickType {
		SERVER,
		CLIENT,
		ALL
	}
	
	public enum Phase {
		START, END, ANY
	}
}

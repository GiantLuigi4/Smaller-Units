package com.tfc.smallerunits.utils.world;

import com.mojang.datafixers.DataFixer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.ServerWorldLightManager;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.SaveFormat;
import sun.misc.Unsafe;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FakeServerChunkProvider extends ServerChunkProvider {
	private static final Unsafe theUnsafe;
	
	static {
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			theUnsafe = (Unsafe) f.get(null);
		} catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}
	
	public Chunk theChunk;
	
	public FakeServerChunkProvider(ServerWorld p_i232603_1_, SaveFormat.LevelSave p_i232603_2_, DataFixer p_i232603_3_, TemplateManager p_i232603_4_, Executor p_i232603_5_, ChunkGenerator p_i232603_6_, int p_i232603_7_, boolean p_i232603_8_, IChunkStatusListener p_i232603_9_, Supplier<DimensionSavedDataManager> p_i232603_10_) {
		super(p_i232603_1_, p_i232603_2_, p_i232603_3_, p_i232603_4_, p_i232603_5_, p_i232603_6_, p_i232603_7_, p_i232603_8_, p_i232603_9_, p_i232603_10_);
	}
	
	public static ServerChunkProvider getProvider(FakeServerWorld world) {
		try {
			FakeServerChunkProvider provider = (FakeServerChunkProvider) theUnsafe.allocateInstance(FakeServerChunkProvider.class);
			
			provider.theChunk = world.getChunk(0, 0);
			provider.world = world;
			provider.ticketManager = (FakeTicketManager) theUnsafe.allocateInstance(FakeTicketManager.class);
			((FakeTicketManager) provider.ticketManager).provider = provider;
			((FakeTicketManager) provider.ticketManager).init();
			provider.chunkManager = (FakeChunkManager) theUnsafe.allocateInstance(FakeChunkManager.class);
			((FakeChunkManager) provider.chunkManager).provider = provider;
			((FakeChunkManager) provider.chunkManager).world = world;
			((FakeChunkManager) provider.chunkManager).init();
			provider.savedData = new FakeDimensionSavedData(null, null, () -> world.owner.dataNBT);
			
			return provider;
		} catch (Throwable err) {
			err.printStackTrace();
			throw new RuntimeException(err);
		}
	}
	
	@Override
	public ChunkHolder func_217213_a(long chunkPosIn) {
		return new ChunkHolder(new ChunkPos(chunkPosIn), 0, world.getLightManager(), (a, b, c, d) -> {
		}, (a, b) -> Stream.empty());
	}
	
	@Override
	public void tick(BooleanSupplier hasTimeLeft) {
		this.world.getProfiler().endStartSection("chunks");
		this.tickChunks();
		this.world.getProfiler().endStartSection("unload");
	}
	
	@Nullable
	@Override
	public IChunk getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean load) {
		return theChunk;
	}
	
	@Nullable
	@Override
	public Chunk getChunkNow(int chunkX, int chunkZ) {
		return theChunk;
	}
	
	@Override
	public int getLoadedChunksCount() {
		return 100000;
	}
	
	@Override
	public ServerWorldLightManager getLightManager() {
		return (ServerWorldLightManager) world.getLightManager();
	}
	
	@Override
	public boolean chunkExists(int x, int z) {
		return true;
	}
	
	@Override
	public boolean isChunkLoaded(Entity entityIn) {
		return true;
	}
	
	@Override
	public boolean isChunkLoaded(ChunkPos pos) {
		return true;
	}
	
	@Override
	public int getLoadedChunkCount() {
		return getLoadedChunksCount();
	}
	
	@Override
	public boolean canTick(BlockPos pos) {
		return true;
	}
	
	@Override
	public IBlockReader getChunkForLight(int chunkX, int chunkZ) {
		return world;
	}
}

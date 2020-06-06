package tfc.smallerunits.Utils;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.profiler.IProfiler;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;

import javax.annotation.Nullable;
import java.util.concurrent.Executor;
import java.util.function.IntSupplier;

public class FakeServerWorld extends ServerWorld {
	public FakeServerWorld(ServerWorld realWorld) {
		super(realWorld.getServer(), runnable -> {}, new SaveHandler(null, "", null, new DataFixer() {
			@Override
			public <T> Dynamic<T> update(DSL.TypeReference type, Dynamic<T> input, int version, int newVersion) {
				return input;
			}
			
			@Override
			public Schema getSchema(int key) {
				return new Schema(0, null);
			}
		}), new WorldInfo() {
		}, DimensionType.OVERWORLD, new Profiler(0, () -> 0, false), new IChunkStatusListener() {
			@Override
			public void start(ChunkPos center) {
			
			}
			
			@Override
			public void statusChanged(ChunkPos chunkPosition, @Nullable ChunkStatus newStatus) {
			
			}
			
			@Override
			public void stop() {
			
			}
		});
	}
}

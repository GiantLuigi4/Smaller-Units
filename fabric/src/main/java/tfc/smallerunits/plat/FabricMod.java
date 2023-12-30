package tfc.smallerunits.plat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.impl.client.texture.SpriteRegistryCallbackHolder;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import tfc.smallerunits.SmallerUnits;
import tfc.smallerunits.plat.util.PlatformUtils;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FabricMod extends SmallerUnits implements ModInitializer {
	@Override
	public void onInitialize() {
		// no-op
	}
	
	@Override
	public void prepare() {
		// no-op
	}
	
	@Override
	public void registerSetup(Runnable common) {
		if (PlatformUtils.isClient()) ClientLifecycleEvents.CLIENT_STARTED.register((a) -> common.run());
		else ServerLifecycleEvents.SERVER_STARTED.register((a) -> common.run());
	}
	
	@Override
	public void registerTick(TickType type, Phase phase, Runnable tick) {
		if (phase == Phase.ANY || phase == Phase.START) {
			if (type == TickType.ALL || type == TickType.CLIENT)
				if (PlatformUtils.isClient())
					ClientTickEvents.START_CLIENT_TICK.register((n) -> tick.run());
			if (type == TickType.ALL || type == TickType.SERVER)
				ServerTickEvents.START_SERVER_TICK.register((n) -> tick.run());
		}
		if (phase == Phase.ANY || phase == Phase.END) {
			if (type == TickType.ALL || type == TickType.CLIENT)
				if (PlatformUtils.isClient())
					ClientTickEvents.END_CLIENT_TICK.register((n) -> tick.run());
			if (type == TickType.ALL || type == TickType.SERVER)
				ServerTickEvents.END_SERVER_TICK.register((n) -> tick.run());
		}
	}
	
	@Override
	public void registerAtlas(BiConsumer<ResourceLocation, Consumer<ResourceLocation>> onTextureStitch) {
		SpriteRegistryCallbackHolder.EVENT_GLOBAL.register((listener, whatdoyouwantfabric) -> {
			onTextureStitch.accept(listener.location(), whatdoyouwantfabric::register);
		});
	}
	
	@Override
	public void registerChunkStatus(BiConsumer<LevelAccessor, ChunkAccess> onChunkLoaded, BiConsumer<LevelAccessor, ChunkAccess> onChunkUnloaded) {
		ServerChunkEvents.CHUNK_LOAD.register(onChunkLoaded::accept);
		ServerChunkEvents.CHUNK_UNLOAD.register(onChunkUnloaded::accept);
	}
	
	@Override
	public void registerAttachment() {
	
	}
	
	@Override
	public void registerCapabilities() {
	
	}
}

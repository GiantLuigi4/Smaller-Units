package com.tfc.smallerunits;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.VertexBuffer;

import java.util.Optional;

public class BufferStorage {
	public RenderType renderType;
	public Optional<VertexBuffer> terrainBuffer;
	public Optional<VertexBuffer> fluidBuffer;
	
	public BufferStorage() {
		//I've had a weird experience where java decided to mess up init order and initialized fields *after* I assigned values to them so
		terrainBuffer = Optional.empty();
		fluidBuffer = Optional.empty();
	}
}

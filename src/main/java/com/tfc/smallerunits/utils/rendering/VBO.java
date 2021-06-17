package com.tfc.smallerunits.utils.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import org.lwjgl.system.MemoryUtil;

import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class VBO extends VertexBuffer {
	private static Method cleanerMethod = null;
	private static Method cleanMethod = null;
	private static Method viewedBufferMethod = null;
	private static Method freeMethod;
	
	static {
		try {
			try {
				cleanerMethod = Class.forName("sun.nio.ch.DirectBuffer").getMethod("cleaner");
				cleanerMethod.setAccessible(true);
			} catch (Throwable ignored) {
			}
			
			try {
				cleanMethod = Class.forName("sun.misc.Cleaner").getMethod("clean");
				cleanMethod.setAccessible(true);
			} catch (Throwable ignored) {
			}
			
			try {
				viewedBufferMethod = Class.forName("sun.nio.ch.DirectBuffer").getMethod("viewedBuffer");
				viewedBufferMethod.setAccessible(true);
			} catch (Throwable ignored) {
				try {
					viewedBufferMethod = Class.forName("sun.nio.ch.DirectBuffer").getMethod("attachment");
					viewedBufferMethod.setAccessible(true);
				} catch (Throwable ignored1) {
				}
			}
		} catch (Throwable ignored) {
		}
		
		ByteBuffer bb = ByteBuffer.allocateDirect(1);
		Class<?> clazz = bb.getClass();
		try {
			freeMethod = clazz.getMethod("free");
			freeMethod.invoke(bb);
		} catch (Throwable ignored) {
		}
	}
	
	public VBO(VertexFormat vertexFormatIn) {
		super(vertexFormatIn);
	}
	
	public void uploadAndFree(BufferBuilder bufferIn) {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> {
				this.uploadRaw(bufferIn);
			});
		} else {
			this.uploadRaw(bufferIn);
		}
	}
	
	public void upload(ByteArrayBuilder bufferIn) {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> {
				this.uploadRaw(bufferIn);
			});
		} else {
			this.uploadRaw(bufferIn);
		}
	}
	
	private void uploadRaw(BufferBuilder bufferIn) {
		if (bufferIn.drawStates.isEmpty()) return;
		Pair<BufferBuilder.DrawState, ByteBuffer> pair;
		pair = bufferIn.getNextBuffer();
		if (this.glBufferId != -1) {
			ByteBuffer bytebuffer = pair.getSecond();
			this.count = bytebuffer.remaining() / this.vertexFormat.getSize();
			this.bindBuffer();
			RenderSystem.glBufferData(34962, bytebuffer, 35044);
			unbindBuffer();
			destroyBuffer(bytebuffer);
		}
	}
	
	public void destroyBuffer(Buffer buffer) {
		try {
			try {
				if (cleanerMethod != null) {
					Object cleaner = cleanerMethod.invoke(buffer);
					if (cleaner instanceof Runnable) {
						((Runnable) cleaner).run();
					} else if (cleaner != null) {
						cleanMethod.invoke(cleaner);
					}
				}
			} catch (Throwable ignored) {
			}
			try {
				if (viewedBufferMethod != null) {
					Object viewedBuffer = viewedBufferMethod.invoke(buffer);
					if (viewedBuffer instanceof Buffer) {
						destroyBuffer((Buffer) viewedBuffer);
					}
				}
			} catch (Throwable ignored) {
			}
			if (freeMethod != null)
				freeMethod.invoke(buffer);
		} catch (Throwable err) {
		}
	}
	
	private void uploadRaw(ByteArrayBuilder bufferIn) {
		if (this.glBufferId != -1) {
			ByteBuffer bytes = bufferIn.getBytes();
			System.out.println(bytes.remaining());
			this.count = bytes.remaining() / this.vertexFormat.getSize();
			this.bindBuffer();
			RenderSystem.glBufferData(34962, bytes, 35044);
//			RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
//			GL15.glBufferData(34962, bytes, 34962);
			unbindBuffer();
			MemoryUtil.memFree(bytes);
		}
	}
}

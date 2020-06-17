package tfc.smallerunits;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.ReportedException;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.apache.logging.log4j.Level;
import tfc.smallerunits.Utils.SmallUnit;

import java.util.*;

public class SmallerUnitTESR extends TileEntityRenderer<SmallerUnitsTileEntity> {
	private static SmallerUnitTESR INSTANCE;
	
	public SmallerUnitTESR(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
		INSTANCE=this;
	}
	
	public static SmallerUnitTESR getINSTANCE(){return INSTANCE;}
	
	@Override
	public void render(SmallerUnitsTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		if (true) {
			matrixStackIn.push();
			int sc=tileEntityIn.serializeNBT().getInt("upb");
			if (sc==0) {
				sc=4;
			}
			int packedlight=combinedLightIn;
			if (tileEntityIn.hasWorld()) {
				packedlight=((tileEntityIn.getWorld().getLightFor(LightType.SKY,tileEntityIn.getPos())<<20)|(tileEntityIn.getWorld().getLightFor(LightType.BLOCK,tileEntityIn.getPos())<<4));
			}
			Collection<SmallUnit> units=tileEntityIn.containedWorld.unitHashMap.values();
			matrixStackIn.scale(1f/sc,1f/sc,1f/sc);
			for (SmallUnit unit:units) {
				matrixStackIn.push();
				BlockState state=unit.s;
				matrixStackIn.translate(unit.x,unit.y,unit.z);
				int light=tileEntityIn.hasWorld()?LightTexture.packLight(tileEntityIn.containedWorld.getBlockLightValue(new BlockPos(unit.x,unit.y,unit.z)),tileEntityIn.containedWorld.getSkyLightValue(new BlockPos(unit.x,unit.y,unit.z))):combinedLightIn;
				if (state.getRenderType().equals(BlockRenderType.MODEL)||state.getRenderType().equals(BlockRenderType.ENTITYBLOCK_ANIMATED)||state.getRenderType().equals(BlockRenderType.INVISIBLE))
					if (tileEntityIn.containedWorld.getTileEntity(new BlockPos(unit.x,unit.y,unit.z))!=null) {
						matrixStackIn.push();
						try {
							TileEntity renderTE=tileEntityIn.containedWorld.getTileEntity(new BlockPos(unit.x,unit.y,unit.z));
							renderTE.setWorldAndPos(tileEntityIn.containedWorld,new BlockPos(unit.x,unit.y,unit.z));
							if (TileEntityRendererDispatcher.instance.getRenderer(renderTE)!=null)TileEntityRendererDispatcher.instance.getRenderer(renderTE).render(renderTE,partialTicks,matrixStackIn,bufferIn,light,combinedOverlayIn);
						} catch (NullPointerException err) {} catch (Throwable err) {
							StringBuilder errmsg= new StringBuilder("\n");errmsg.append(err.toString()).append('\n');
							for(StackTraceElement element:err.getStackTrace())errmsg.append(element.toString()).append('\n');
							System.out.println(errmsg.toString());
						}
						matrixStackIn.pop();
					}
				try {
					if (state.getRenderType().equals(BlockRenderType.MODEL)) {
						ArrayList<BakedQuad> qds=new ArrayList<>();
						IBakedModel mdl=Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(state);
						for (Direction dir:Direction.values()) {
							if (
									(!tileEntityIn.containedWorld.getBlockState(new BlockPos(unit.x,unit.y,unit.z).offset(dir)).isSolidSide(tileEntityIn.containedWorld,new BlockPos(unit.x,unit.y,unit.z).offset(dir),dir))||
											(!(RenderTypeLookup.getRenderType(tileEntityIn.containedWorld.getBlockState(new BlockPos(unit.x,unit.y,unit.z).offset(dir))).equals(RenderType.getSolid())))) {
								if (RenderTypeLookup.getRenderType(state).equals((RenderTypeLookup.getRenderType(tileEntityIn.containedWorld.getBlockState(new BlockPos(unit.x,unit.y,unit.z).offset(dir)))))) {
									if (!state.equals(tileEntityIn.containedWorld.getBlockState(new BlockPos(unit.x,unit.y,unit.z).offset(dir)))) {
										if (RenderTypeLookup.getRenderType(state).equals(RenderType.getTranslucent())&&
												tileEntityIn.containedWorld.getBlockState(new BlockPos(unit.x,unit.y,unit.z).offset(dir)).isSolidSide(tileEntityIn.containedWorld,new BlockPos(unit.x,unit.y,unit.z).offset(dir),dir)) {
										} else {
											qds.addAll(mdl.getQuads(state,dir,new Random(new BlockPos(unit.x,unit.y,unit.z).toLong())));
										}
									}
								} else {
									qds.addAll(mdl.getQuads(state,dir,new Random(new BlockPos(unit.x,unit.y,unit.z).toLong())));
								}
							}
						}
						qds.addAll(mdl.getQuads(state,null,new Random(new BlockPos(unit.x,unit.y,unit.z).toLong())));
//						if (true||state.getShape(tileEntityIn.containedWorld,new BlockPos(unit.x,unit.y,unit.z)).equals(VoxelShapes.create(0,0,0,1,1,1))&&tileEntityIn.useManual) {
//							Minecraft.getInstance().getItemRenderer().renderQuads(matrixStackIn,bufferIn.getBuffer(RenderTypeLookup.getRenderType(state)),qds,new ItemStack(Item.getItemFromBlock(state.getBlock())),light, combinedOverlayIn);
							ItemStack itemStackIn=new ItemStack(Item.getItemFromBlock(state.getBlock()));
							boolean flag = !itemStackIn.isEmpty();
							MatrixStack.Entry matrixstack$entry = matrixStackIn.getLast();
							
							for(BakedQuad bakedquad : qds) {
								int i = -1;
								if(flag&&bakedquad.hasTintIndex())i=Minecraft.getInstance().getBlockColors().getColor(state,tileEntityIn.containedWorld,new BlockPos(unit.x,unit.y,unit.z), bakedquad.getTintIndex());
								
								float f = (float)(i >> 16 & 255) / 255.0F;
								float f1 = (float)(i >> 8 & 255) / 255.0F;
								float f2 = (float)(i & 255) / 255.0F;
								bufferIn.getBuffer(RenderTypeLookup.getRenderType(state)).addVertexData(matrixstack$entry, bakedquad, f, f1, f2, light, combinedOverlayIn, true);
							}
//						}
						if (tileEntityIn.isEnchanted)
							Minecraft.getInstance().getBlockRendererDispatcher().renderModel(state,new BlockPos(unit.x,unit.y,unit.z),tileEntityIn.containedWorld,matrixStackIn,bufferIn.getBuffer(RenderType.getGlint()),true,new Random(new BlockPos(unit.x,unit.y,unit.z).toLong()),net.minecraftforge.client.model.data.EmptyModelData.INSTANCE);
//						IFluidState fluidState=Blocks.WATER.getDefaultState().getFluidState();
//						Minecraft.getInstance().getBlockRendererDispatcher().renderFluid(new BlockPos(unit.x,unit.y,unit.z),tileEntityIn.containedWorld,bufferIn.getBuffer(RenderTypeLookup.getRenderType(fluidState)),fluidState);
					}
				} catch (Throwable err) {
					Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(state,matrixStackIn,bufferIn,packedlight,combinedOverlayIn);
					if (err instanceof ReportedException) {
						System.out.println(((ReportedException)err).getCrashReport().getCompleteReport());
					} else {
						StringBuilder errmsg=new StringBuilder("\n");
						errmsg.append(err.toString()).append('\n');
						for (StackTraceElement element:err.getStackTrace()) {
							errmsg.append(element.toString()).append('\n');
						}
						System.out.println(errmsg.toString());
					}
				}
				matrixStackIn.pop();
			}
			matrixStackIn.pop();
		}
		if (Minecraft.getInstance().gameSettings.showDebugInfo) {
			matrixStackIn.push();
			try {
				RenderSystem.enableDepthTest();
				VoxelShape shape=tileEntityIn.getBlockState().getBlock().getCollisionShape(tileEntityIn.getBlockState(),tileEntityIn.getWorld(),tileEntityIn.getPos(),null);
				for (AxisAlignedBB box:shape.toBoundingBoxList()) {
					WorldRenderer.drawBoundingBox(matrixStackIn,bufferIn.getBuffer(RenderType.getLines()),box,1,0,0,1);
				}
				shape=tileEntityIn.getBlockState().getBlock().getShape(tileEntityIn.getBlockState(),tileEntityIn.getWorld(),tileEntityIn.getPos(),null);
				for (AxisAlignedBB box:shape.toBoundingBoxList()) {
					WorldRenderer.drawBoundingBox(matrixStackIn,bufferIn.getBuffer(RenderType.getLines()),box,0,0,1,1);
				}
				shape=tileEntityIn.getBlockState().getBlock().getRaytraceShape(tileEntityIn.getBlockState(),tileEntityIn.getWorld(),tileEntityIn.getPos());
				for (AxisAlignedBB box:shape.toBoundingBoxList()) {
					WorldRenderer.drawBoundingBox(matrixStackIn,bufferIn.getBuffer(RenderType.getLines()),box,0,1,0,1);
				}
				BlockRayTraceResult result=(shape.rayTrace(
						Minecraft.getInstance().player.getEyePosition(0),
						Minecraft.getInstance().player.getEyePosition(0).add(Minecraft.getInstance().player.getLookVec().scale(9)),
						tileEntityIn.getPos()
				));
				if (result!=null) {
					float size=0.001f;
					Vec3d hit=result.getHitVec();
					hit=hit.subtract(new Vec3d(tileEntityIn.getPos()));
					WorldRenderer.drawBoundingBox(matrixStackIn,bufferIn.getBuffer(RenderType.getLines()),
							new AxisAlignedBB(
									hit.x-size,
									hit.y-size,
									hit.z-size,
									hit.x+size,
									hit.y+size,
									hit.z+size
							),0,0,0,1);
				}
				shape=tileEntityIn.getBlockState().getBlock().getRenderShape(tileEntityIn.getBlockState(),tileEntityIn.getWorld(),tileEntityIn.getPos());
				for (AxisAlignedBB box:shape.toBoundingBoxList()) {
					WorldRenderer.drawBoundingBox(matrixStackIn,bufferIn.getBuffer(RenderType.getLines()),box,1,0,1,1);
				}
			} catch (Exception ignored) {}
			matrixStackIn.pop();
		}
	}
	
	private static void drawShape(MatrixStack matrixStackIn, IVertexBuilder bufferIn, VoxelShape shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha) {
		Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();
		shapeIn.forEachEdge((p_230013_12_, p_230013_14_, p_230013_16_, p_230013_18_, p_230013_20_, p_230013_22_) -> {
			bufferIn.pos(matrix4f, (float)(p_230013_12_ + xIn), (float)(p_230013_14_ + yIn), (float)(p_230013_16_ + zIn)).color(red, green, blue, alpha).endVertex();
			bufferIn.pos(matrix4f, (float)(p_230013_18_ + xIn), (float)(p_230013_20_ + yIn), (float)(p_230013_22_ + zIn)).color(red, green, blue, alpha).endVertex();
		});
	}
}

package tfc.smallerunits.utils.asm;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfc.smallerunits.UnitEdge;
import tfc.smallerunits.UnitSpace;
import tfc.smallerunits.UnitSpaceBlock;
import tfc.smallerunits.client.abstraction.VanillaFrustum;
import tfc.smallerunits.client.render.TileRendererHelper;
import tfc.smallerunits.client.render.util.SUTesselator;
import tfc.smallerunits.data.capability.ISUCapability;
import tfc.smallerunits.data.capability.SUCapabilityManager;
import tfc.smallerunits.data.storage.RegionPos;
import tfc.smallerunits.simulation.level.ITickerLevel;
import tfc.smallerunits.utils.math.HitboxScaling;
import tfc.smallerunits.utils.scale.ResizingUtils;
import tfc.smallerunits.utils.selection.MutableVec3;
import tfc.smallerunits.utils.selection.UnitHitResult;
import tfc.smallerunits.utils.selection.UnitShape;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AssortedQol {
	public static FogType getFogType(Level level, RegionPos regionPos, Vec3 position, Vec3 camPos) {
		position = position.scale(1d / ((ITickerLevel) level).getUPB()).add(camPos);
		
		BlockPos pos = regionPos.toBlockPos();
		position = position.subtract(pos.getX(), pos.getY(), pos.getZ());
		position = position.scale(((ITickerLevel) level).getUPB());
		
		BlockPos ps = new BlockPos(position);
		BlockState block = level.getBlockState(ps);
		FluidState fluid = block.getFluidState();
		if (fluid.is(FluidTags.LAVA)) {
			if (position.y <= (double) (fluid.getHeight(level, ps) + (float) ps.getY())) {
				return FogType.LAVA;
			}
		} else if (fluid.is(FluidTags.WATER)) {
			if (position.y <= (double) (fluid.getHeight(level, ps) + (float) ps.getY())) {
				return FogType.WATER;
			}
		} else if (block.is(Blocks.POWDER_SNOW)) {
			return FogType.POWDER_SNOW;
		}
		
		return FogType.NONE;
	}
	
	public static void handleBlockInfo(HitResult block, CallbackInfoReturnable<List<String>> cir, List<String> strings) {
		if (block instanceof UnitHitResult result) {
			if (strings.get(strings.size() - 1).equals("smallerunits:unit_space")) {
				strings.remove(strings.size() - 1);
//				strings.remove(strings.size() - 1);
			} else {
				strings.add("");
			}
			
			Level level = Minecraft.getInstance().level;
			ISUCapability capability = SUCapabilityManager.getCapability(level, new ChunkPos(result.getBlockPos()));
			UnitSpace space = capability.getUnit(result.getBlockPos());
			
			Vec3 start = Minecraft.getInstance().cameraEntity.getEyePosition(0);
			Vec3 end = Minecraft.getInstance().cameraEntity.getEyePosition(0).add(Minecraft.getInstance().cameraEntity.getViewVector(0).scale(20)); // TODO: figure out what exactly this should be
			start = new Vec3(
					HitboxScaling.scaleX((ITickerLevel) space.getMyLevel(), start.x),
					HitboxScaling.scaleY((ITickerLevel) space.getMyLevel(), start.y),
					HitboxScaling.scaleZ((ITickerLevel) space.getMyLevel(), start.z)
			);
			end = new Vec3(
					HitboxScaling.scaleX((ITickerLevel) space.getMyLevel(), end.x),
					HitboxScaling.scaleY((ITickerLevel) space.getMyLevel(), end.y),
					HitboxScaling.scaleZ((ITickerLevel) space.getMyLevel(), end.z)
			);
			BlockHitResult result1 = space.getMyLevel().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, Minecraft.getInstance().player));
			
			BlockPos blockpos = result1.getBlockPos();
			BlockState state = space.getMyLevel().getBlockState(blockpos);
			List<String> list = strings;

//			list.add(ChatFormatting.UNDERLINE + "Block: " + result.getBlockPos().getX() + ", " + result.getBlockPos().getY() + ", " + result.getBlockPos().getZ());
			list.add(ChatFormatting.ITALIC + "Targeted Small Block: " + blockpos.getX() + ", " + blockpos.getY() + ", " + blockpos.getZ());
			list.add(ChatFormatting.ITALIC + "World: " + level.dimension().location() + "|" + space.regionPos.x + "|" + space.regionPos.y + "|" + space.regionPos.z + "|");
			list.add(ChatFormatting.ITALIC + "Scale: 1/" + space.unitsPerBlock);
			list.add(String.valueOf((Object) Registry.BLOCK.getKey(state.getBlock())));
			
			for (Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet()) {
				Property<?> property = entry.getKey();
				Comparable<?> comparable = entry.getValue();
				String s = Util.getPropertyName(property, comparable);
				if (Boolean.TRUE.equals(comparable)) {
					s = ChatFormatting.GREEN + s;
				} else if (Boolean.FALSE.equals(comparable)) {
					s = ChatFormatting.RED + s;
				}
				
				boolean isNumber = true;
				for (char c : s.toCharArray()) {
					if (!Character.isDigit(c)) {
						isNumber = false;
						break;
					}
				}
				if (isNumber) s = ChatFormatting.GOLD + s;
				
				list.add(property.getName() + ": " + s);
			}
			
			for (Object o : state.getTags().toArray()) {
				list.add("#" + ((TagKey<Block>) o).location());
			}
		}
	}
	
	public static boolean scaleRender(double vd, AABB renderBox, ITickerLevel tickerWorld, BlockPos pPos, Vec3 pCameraPos) {
		double sd = ResizingUtils.getActualSize(Minecraft.getInstance().player);
		double divisor = tickerWorld.getUPB();
		
		if (sd > (1d / divisor)) sd = 1;
//			vd /= sd;
		
		vd *= divisor;
		divisor *= sd;
		
		if (divisor <= 1.001) {
			divisor = tickerWorld.getUPB();
			double sz = Math.max(Math.max(renderBox.getXsize(), renderBox.getYsize()), renderBox.getZsize());
			if (sz <= 1)
				return Vec3.atCenterOf(pPos).closerThan(pCameraPos, vd / Math.cbrt(divisor));
			else
				return Vec3.atCenterOf(pPos).closerThan(pCameraPos, vd);
		}
		
		double sz = renderBox.getSize();
		
		if (sz < 1) sz = 1;
		divisor /= sz;
		if (divisor < 1) divisor = 1;
		
		// TODO: check for a better scaling algo?
		return Vec3.atCenterOf(pPos).closerThan(pCameraPos, vd / Math.sqrt(divisor));
	}
	
	public static void scaleVert(SUTesselator.TranslatingBufferBuilder translatingBufferBuilder, double pX, double pY, double pZ, float scl, MutableVec3 coords, MutableVec3 offset) {
		Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		coords.set(
				(pX + camera.getPosition().x) * scl - camera.getPosition().x + offset.x,
				(pY + camera.getPosition().y) * scl - camera.getPosition().y + offset.y,
				(pZ + camera.getPosition().z) * scl - camera.getPosition().z + offset.z
		);
	}
	
	
	public static void handleRenderOutline(Consumer<VoxelShape> renderShape, Level level, PoseStack pPoseStack, VertexConsumer pConsumer, Entity pEntity, double pCamX, double pCamY, double pCamZ, BlockPos pPos, BlockState pState, CallbackInfo ci) {
		if (pState.getBlock() instanceof UnitSpaceBlock) {
			VoxelShape shape = pState.getShape(level, pPos, CollisionContext.of(pEntity));
			if (shape instanceof UnitShape) {
				ci.cancel();
				HitResult result = Minecraft.getInstance().hitResult;
				
				if (result instanceof UnitHitResult) {
					BlockPos pos = ((UnitHitResult) result).geetBlockPos();
					LevelChunk chnk = level.getChunkAt(pPos);
					UnitSpace space = SUCapabilityManager.getCapability(chnk).getUnit(pPos);
					BlockState state = space.getBlock(pos.getX(), pos.getY(), pos.getZ());
					
					pPoseStack.pushPose();
					pPoseStack.translate(
							(double) pPos.getX() - pCamX,
							(double) pPos.getY() - pCamY,
							(double) pPos.getZ() - pCamZ
					);
					
					// TODO: better handling
					BlockPos pz = space.getOffsetPos(((UnitHitResult) result).geetBlockPos());
					VoxelShape shape1 = state.getShape(space.getMyLevel(), pz, CollisionContext.of(pEntity));
					if (state.getBlock() instanceof UnitSpaceBlock) {
						pPoseStack.scale(1f / space.unitsPerBlock, 1f / space.unitsPerBlock, 1f / space.unitsPerBlock);
						BlockPos ps = ((UnitHitResult) result).geetBlockPos();
						pPoseStack.translate(
								-pz.getX() + ps.getX(),
								-pz.getY() + ps.getY(),
								-pz.getZ() + ps.getZ()
						);
						
						/* calculate reach distance */
						double reach = 7;
						if (pEntity instanceof LivingEntity le) {
							if (le.getAttributes().hasAttribute(ForgeMod.REACH_DISTANCE.get()))
								reach = le.getAttributeValue(ForgeMod.REACH_DISTANCE.get());
						}
						
						Vec3 start = new Vec3(pCamX, pCamY, pCamZ);
						Vec3 end = start.add(pEntity.getViewVector(1).scale(reach));
						HitboxScaling.scale(start, ((ITickerLevel) space.getMyLevel()));
						HitboxScaling.scale(end, ((ITickerLevel) space.getMyLevel()));
						
						/* render recursive */
						Minecraft.getInstance().hitResult = shape1.clip(start, end, pz);
						handleRenderOutline(
								renderShape,
								space.getMyLevel(), pPoseStack, pConsumer,
								pEntity, 0, 0, 0,
								pz, state, ci
						);
						Minecraft.getInstance().hitResult = result;
					} else if (shape1.isEmpty() || state.getBlock() instanceof UnitEdge) {
						/* draw edge */
						int x = pos.getX();
						int y = pos.getY();
						int z = pos.getZ();
						
						double upbDouble = space.unitsPerBlock;
						AABB box = ((UnitHitResult) result).getSpecificBox();
						if (box == null) {
							box = new AABB(
									x / upbDouble, y / upbDouble, z / upbDouble,
									(x + 1) / upbDouble, (y + 1) / upbDouble, (z + 1) / upbDouble
							);
						}
						
						shape1 = Shapes.create(box);
						renderShape.accept(shape1);
					} else {
						/* draw block */
						pPoseStack.scale(1f / space.unitsPerBlock, 1f / space.unitsPerBlock, 1f / space.unitsPerBlock);
						pPoseStack.translate(pos.getX(), pos.getY(), pos.getZ());
						renderShape.accept(shape1);
					}
					
					pPoseStack.popPose();
				}
			}
		}
	}
	
	public static void drawIndicatorsRecursive(UnitSpace unit, BlockPos origin, boolean hammerHeld, PoseStack stk, VanillaFrustum SU$Frustum) {
		TileRendererHelper.drawUnit(
				SU$Frustum,
				new BlockPos(0, 0, 0), unit.unitsPerBlock, unit.isNatural,
				hammerHeld, unit.isEmpty(), null, stk,
				LightTexture.pack(0, 0),
//				origin.getX(), origin.getY(), origin.getZ()
				-unit.pos.getX(), -unit.pos.getY(), -unit.pos.getZ()
		);
	}
	
	public static void setupMatrix(UnitSpace space, PoseStack stk) {
		stk.scale(1f / space.unitsPerBlock, 1f / space.unitsPerBlock, 1f / space.unitsPerBlock);
	}
	
	public static boolean isInSection(UnitSpace space, BlockPos origin) {
		int y = space.pos.getY();
		if (y < origin.getY() + 16 &&
				y >= origin.getY()) {
			return true;
		}
		return false;
	}
}

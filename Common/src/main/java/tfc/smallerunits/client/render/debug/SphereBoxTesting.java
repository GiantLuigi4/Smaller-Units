package tfc.smallerunits.client.render.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tfc.smallerunits.utils.selection.MutableVec3;
import tfc.smallerunits.utils.spherebox.Box;
import tfc.smallerunits.utils.spherebox.SphereBox;
import tfc.smallerunits.utils.spherebox.VecMath;
import tfc.smallerunits.utils.vr.player.SUVRPlayer;
import tfc.smallerunits.utils.vr.player.VRController;
import tfc.smallerunits.utils.vr.player.VRPlayerManager;

import java.util.ArrayList;

public class SphereBoxTesting {
	public static void render(PoseStack pPoseStack, float pPartialTick, RenderBuffers renderBuffers, long gameTime, Matrix4f pProjectionMatrix) {
//		SUVRPlayer player = VRPlayerManager.getPlayer(Minecraft.getInstance().player);
		SUVRPlayer player = VRPlayerManager.getPlayer(Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayers().get(0));
		if (player == null) return;
		VRController pose = player.getHand(InteractionHand.OFF_HAND);
		
		VertexConsumer consumer = renderBuffers.bufferSource().getBuffer(RenderType.LINES);
		
		Quaternion quat = pose.getQuaternion();
		quat.conj();
		
		ArrayList<Vec3> points = new ArrayList<>();
		float sz = 0.05f;
		float len = 6;
		points.add(new Vec3(-sz, -sz, 0));
		points.add(new Vec3(sz, -sz, 0));
		points.add(new Vec3(-sz, sz, 0));
		points.add(new Vec3(-sz, -sz, sz * len));
		points.add(new Vec3(sz, -sz, sz * len));
		points.add(new Vec3(sz, sz, 0));
		points.add(new Vec3(-sz, sz, sz * len));
		points.add(new Vec3(sz, sz, sz * len));
		
		Vec3[] vecs = new Vec3[points.size()];
		Vector4f worker = new Vector4f();
		for (int i = 0; i < vecs.length; i++) {
			VecMath.rotate(points.get(i), quat, worker);
			vecs[i] = new Vec3(worker.x() * player.worldScale, worker.y() * player.worldScale, worker.z() * player.worldScale);
		}
		
		quat.conj();
		Box bx = new Box(vecs, quat, worker, pose.getPosition());
		
		pPoseStack.pushPose();
		pPoseStack.translate(
				-Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().x,
				-Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().y,
				-Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().z
		);
		
		Matrix4f matr = pPoseStack.last().pose();
		Matrix3f norm = pPoseStack.last().normal();
		
		for (Vec3 vec : vecs) {
			consumer.vertex(matr, (float) vec.x, (float) vec.y, (float) vec.z).color(1f, 0, 0, 1).normal(1, 0, 0).endVertex();
//			consumer.vertex(matr, (float) vec.x, (float) vec.y, (float) vec.z + 0.01f).color(1f, 0, 0, 1).normal(1, 0, 0).endVertex();
			
			VecMath.rotate(vec, quat, worker);
			consumer.vertex(matr, (float) worker.x(), (float) worker.y(), (float) worker.z()).color(0f, 1, 0, 1).normal(1, 0, 0).endVertex();
//			consumer.vertex(matr, (float) worker.x(), (float) worker.y(), (float) worker.z() + 0.01f).color(0f, 1, 0, 1).normal(1, 0, 0).endVertex();
		}
		
		AABB wsBounds = bx.getWsAABB(worker);
		AABB bounds = bx.getLsAABB(worker);
		LevelRenderer.renderLineBox(pPoseStack, consumer, wsBounds, 0, 1, 0, 1f);
		LevelRenderer.renderLineBox(pPoseStack, consumer, bounds, 0, 1, 1, 1f);
		
		pPoseStack.pushPose();
		pPoseStack.mulPose(quat);
		LevelRenderer.renderLineBox(pPoseStack, consumer, bounds, 1, 0, 0, 1f);
		pPoseStack.popPose();
		
		Vec3 localCenter = new Vec3(
				(bounds.minX + bounds.maxX) / 2,
				(bounds.minY + bounds.maxY) / 2,
				(bounds.minZ + bounds.maxZ) / 2
		);
		LevelRenderer.renderLineBox(pPoseStack, consumer, new AABB(
				localCenter.x - 0.1,
				localCenter.y - 0.1,
				localCenter.z - 0.1,
				localCenter.x + 0.1,
				localCenter.y + 0.1,
				localCenter.z + 0.1
		), 1, 1, 1, 1);
		
		bx.lsVec(localCenter, worker);
		Vec3 center = new Vec3(worker.x(), worker.y(), worker.z());
		
		float rad = 0.5f;
		LevelRenderer.renderLineBox(pPoseStack, consumer, new AABB(
				center.x - 0.1,
				center.y - 0.1,
				center.z - 0.1,
				center.x + 0.1,
				center.y + 0.1,
				center.z + 0.1
		), 1, 1, 1, 1);
		
		MutableVec3 mutableVec = new MutableVec3(0, 0, 0);
		MutableVec3 mutableEnd = new MutableVec3(0, 0, 0);
		Vector3f offsetV = new Vector3f();
		int scl = 10;
		float divisor = 1f / scl;
		rad = divisor / 4;
		boolean intersect;
		
		int minX = (int) (wsBounds.minX * scl) - 1;
		int maxX = (int) Math.ceil((wsBounds.maxX * scl)) + 1;
		
		int minY = (int) (wsBounds.minY * scl) - 1;
		int maxY = (int) Math.ceil((wsBounds.maxY * scl)) + 1;
		
		int minZ = (int) (wsBounds.minZ * scl) - 1;
		int maxZ = (int) Math.ceil((wsBounds.maxZ * scl)) + 1;
		
		for (int x = minX; x < maxX; x++) {
			for (int y = minY; y < maxY; y++) {
				for (int z = minZ; z < maxZ; z++) {
					mutableVec.set((x + 0.5) * divisor, (y + 0.5) * divisor, (z + 0.5) * divisor);
					
					offsetV.set((float) center.x, (float) center.y, (float) center.z);
					offsetV.set(
							offsetV.x() - (float) mutableVec.x,
							offsetV.y() - (float) mutableVec.y,
							offsetV.z() - (float) mutableVec.z
					);
					offsetV.normalize();
					offsetV.mul(rad);
					mutableEnd.set(mutableVec.x + offsetV.x(), mutableVec.y + offsetV.y(), mutableVec.z + offsetV.z());
					
					intersect = SphereBox.intersects(worker, bx, mutableVec, rad);
					consumer.vertex(matr, (float) mutableVec.x, (float) mutableVec.y, (float) mutableVec.z).color(intersect ? 0f : 1f, intersect ? 1f : 0f, 0, 1).normal(norm, 1, 0, 0).endVertex();
					consumer.vertex(matr, (float) mutableEnd.x, (float) mutableEnd.y, (float) mutableEnd.z).color(intersect ? 0f : 1f, intersect ? 1f : 0f, 0, 1).normal(norm, 1, 0, 0).endVertex();
				}
			}
		}
		
		pPoseStack.popPose();
		
		consumer = renderBuffers.bufferSource().getBuffer(RenderType.solid());
	}
}

package tfc.smallerunits.helpers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import tfc.smallerunits.api.placement.UnitPos;
import tfc.smallerunits.block.UnitTileEntity;
import tfc.smallerunits.utils.accessor.IAmContainer;
import tfc.smallerunits.utils.compat.RaytraceUtils;
import tfc.smallerunits.utils.world.server.FakeServerWorld;

public class ContainerMixinHelper {
//	private static final Method setClosable;
//	private static final Method getCanCloseNaturally;
//
//	static {
//		Method m0;
//		Method m1;
//		try {
//			m0 = Container.class.getMethod("setCanCloseNaturally", boolean.class);
//			m1 = Container.class.getMethod("canCloseNaturally");
//		} catch (Throwable err) {
//			try {
//				m0 = Container.class.getDeclaredMethod("setCanCloseNaturally", boolean.class);
//				m1 = Container.class.getDeclaredMethod("canCloseNaturally");
//			} catch (Throwable err1) {
//				try {
//					m0 = ContainerAccessor.class.getMethod("setCanCloseNaturally", boolean.class);
//					m1 = ContainerAccessor.class.getMethod("canCloseNaturally");
//				} catch (Throwable err2) {
//					try {
//						m0 = ContainerAccessor.class.getDeclaredMethod("setCanCloseNaturally", boolean.class);
//						m1 = ContainerAccessor.class.getDeclaredMethod("canCloseNaturally");
//					} catch (Throwable err3) {
//						throw new RuntimeException(err);
//					}
//				}
//			}
//		}
//		setClosable = m0;
//		getCanCloseNaturally = m1;
//	}
//
//	public static void setNaturallyClosable(Container container, boolean naturallyClosable) {
//		if (container instanceof ContainerAccessor) {
//			((ContainerAccessor) container).setCanCloseNaturally(naturallyClosable);
//		} else {
//			try {
//				setClosable.invoke(container, naturallyClosable);
//			} catch (Throwable ignored) {
//			}
//		}
//	}
//
//	public static boolean getNaturallyClosable(Container container) {
//		if (container instanceof ContainerAccessor) {
//			return ((ContainerAccessor) container).canCloseNaturally();
//		} else {
//			try {
//				return (boolean) getCanCloseNaturally.invoke(container);
//			} catch (Throwable ignored) {
//				return true;
//			}
//		}
//	}
	
	public static boolean isVanilla(Container container) {
		return container instanceof PlayerContainer ||
				container instanceof ChestContainer ||
				container instanceof AbstractFurnaceContainer ||
				container instanceof EnchantmentContainer ||
				container instanceof HopperContainer ||
				container instanceof LoomContainer ||
				container instanceof WorkbenchContainer ||
				container instanceof AbstractRepairContainer ||
				container instanceof CartographyContainer ||
				container instanceof GrindstoneContainer ||
				container instanceof StonecutterContainer ||
				container instanceof DispenserContainer ||
				container instanceof LecternContainer ||
				container instanceof BrewingStandContainer;
	}
	
	public static boolean checkReach(PlayerEntity p_75145_1_, BlockPos pos) {
		double reach = RaytraceUtils.getReach(p_75145_1_); // get player reach
		double scalar = reach / 5.0D; // default reach distance is 5, so divide player reach by 5 to get a scalar
		Vector3d wldPos = getWorldPos(pos);
		double dist = p_75145_1_.getDistanceSq(wldPos.x, wldPos.y, wldPos.z);
		// squaring it makes it so that it actually scales the reach correctly
		// don't fully know why, just know it does
		// 64 is the vanilla radius for reach
		// due to the above, multiplying 64 by the squared reach scalar makes it so that if the distance is <= 1, the container is within reach
		dist /= (64 * scalar * scalar);
		return dist <= 1;
	}
	
	public static Vector3d getWorldPos(BlockPos pos) {
		if (pos instanceof UnitPos) {
			int scale = ((UnitPos) pos).scale;
			UnitPos uPos = (UnitPos) pos;
			return new Vector3d(
					(pos.getX() / (float) scale) + uPos.realPos.getX(),
					((pos.getY() - 64) / (float) scale) + uPos.realPos.getY(),
					(pos.getZ() / (float) scale) + uPos.realPos.getZ()
			);
		} else {
			return new Vector3d(
					pos.getX(),
					pos.getY(),
					pos.getZ()
			);
		}
	}
	
	public static UnitTileEntity getOwner(World world) {
		if (world instanceof FakeServerWorld) return ((FakeServerWorld) world).owner;
		else if (ClientUtils.checkFakeClientWorld(world)) return ClientUtils.getOwner(world);
		return null;
	}
	
	public static void setNaturallyClosable(Container openContainer, boolean b) {
		if (openContainer instanceof IAmContainer)
			((IAmContainer) openContainer).SmallerUnits_setCanCloseNaturally(b);
	}
	
	public static boolean getNaturallyClosable(Container openContainer) {
		if (openContainer instanceof IAmContainer)
			return ((IAmContainer) openContainer).SmallerUnits_canCloseNaturally();
		return true;
	}
}

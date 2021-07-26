import net.minecraft.util.math.BlockPos;
import tfc.smallerunits.api.placement.UnitPos;

public class UnitPosTest {
	public static void main(String[] args) {
		UnitPos pos0 = new UnitPos(0, 0, 0, new BlockPos(0, 0, 0), 2);
		UnitPos pos1 = new UnitPos(0, 0, 0, new BlockPos(1, 0, 0), 2);
		System.out.println(pos0);
		System.out.println(pos1);
		System.out.println(pos0.getRelativePos(pos1));
		System.out.println(pos0.getRelativePos(pos1).adjustRealPosition());
	}
}

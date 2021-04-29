import com.tfc.smallerunits.utils.MathUtils;

public class ChunkModTest {
	public static void main(String[] args) {
		System.out.println(MathUtils.chunkMod(5, 15));
		System.out.println(MathUtils.chunkMod(-5, 15));
		System.out.println(MathUtils.chunkMod(15, 15));
		System.out.println(MathUtils.chunkMod(-15, 15));
		System.out.println(MathUtils.chunkMod(32, 15));
		System.out.println(MathUtils.chunkMod(-32, 15));
		System.out.println(MathUtils.chunkMod(0, 15));
		
		System.out.println();
		
		System.out.println((int) MathUtils.getChunkOffset(15, 15));
		System.out.println((int) MathUtils.getChunkOffset(-15, 15));
		System.out.println((int) MathUtils.getChunkOffset(-1, 15));
		System.out.println((int) MathUtils.getChunkOffset(0, 15));
	}
}

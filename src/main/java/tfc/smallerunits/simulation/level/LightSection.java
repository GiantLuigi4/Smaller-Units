package tfc.smallerunits.simulation.level;

public class LightSection {
	// TODO: check
	protected final byte[] values = new byte[16 * 16 * 16];
	
	public byte get(int x, int y, int z) {
		return values[(((x * 16) + y) * 16) + z];
	}
	
	public byte set(int x, int y, int z, byte v) {
		return values[(((x * 16) + y) * 16) + z] = v;
	}
}

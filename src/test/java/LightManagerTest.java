import java.util.ArrayList;

public class LightManagerTest {
	public static final byte[][] lightMap = new byte[64][];
	public static final boolean[][] tiles = new boolean[64][];
	public static final boolean[][] sources = new boolean[64][];
	
	public static LightOffset[] kernel;
	
	public static void main(String[] args) {
		for (int i = 0; i < lightMap.length; i++) {
			lightMap[i] = new byte[64];
			tiles[i] = new boolean[64];
			sources[i] = new boolean[64];
		}
		
		tiles[30][32] = true;
		tiles[30][33] = true;
		tiles[30][34] = true;
		tiles[29][34] = true;
		for (int i = 0; i < 3; i++) {
			tiles[28 - i][34] = true;
			tiles[28 - i][32] = true;
		}
		tiles[25][34] = true;
		tiles[24][34] = true;
		tiles[24][33] = true;
		tiles[24][32] = true;
		tiles[24][31] = true;
		tiles[25][31] = true;
		tiles[26][31] = true;
		
		int size = 15;
		ArrayList<LightOffset> positions = new ArrayList<>();
		for (int xOff = -size; xOff <= size; xOff++) {
			for (int yOff = -size; yOff <= size; yOff++) {
				if (xOff == 0 && yOff == 0) continue;
				int dist = (Math.abs(xOff) + Math.abs(yOff));
				if (dist > 15) continue;
				positions.add(new LightOffset(xOff, yOff, dist));
			}
		}
		positions.sort((self, other) -> {
			int left = Math.abs(self.x) + Math.abs(self.y);
			int right = Math.abs(other.x) + Math.abs(other.y);
			return Integer.compare(left, right);
		});
		kernel = positions.toArray(new LightOffset[0]);
		
		long start = System.nanoTime();
		addLight(63, 63, 15);
		addLight(32, 32, 15);
		addLight(0, 0, 15);
		addLight(32, 27, 15);
		long end = System.nanoTime();
		System.out.println(end - start);
		
		start = System.nanoTime();
		tiles[32][27] = true;
		removeLight(32, 27);
		end = System.nanoTime();
		System.out.println(end - start);
		
		for (byte[] byteArrays : lightMap) {
			for (byte aByte : byteArrays) {
				String text = "" + aByte;
				if (text.length() < 2) text = " " + text;
				if (text.equals(" 0")) text = "  ";
				text += " ";
				System.out.print(text);
			}
			System.out.println();
		}
	}
	
	public static void removeLight(int x, int y) {
		sources[x][y] = false;
		
		int originalLight = lightMap[x][y];
		lightMap[x][y] = 0;
		ArrayList<LightOffset> existingLights = new ArrayList<>();
		for (LightOffset lightOffset : kernel) {
			if (lightOffset.dist > originalLight) break;
			
			int xPos = x + lightOffset.x;
			if (xPos < 0) continue;
			if (xPos >= 64) continue;
			int yPos = y + lightOffset.y;
			if (yPos < 0) continue;
			if (yPos >= 64) continue;
			
			if (sources[xPos][yPos]) {
				existingLights.add(new LightOffset(xPos, yPos, lightMap[xPos][yPos]));
				continue;
			}
			
			lightMap[xPos][yPos] = 0;
		}
		
		for (LightOffset existingLight : existingLights)
			updateLight(existingLight.x, existingLight.y, existingLight.dist);
	}
	
	public static void addLight(int x, int y, int size) {
		if (lightMap[x][y] >= size) return;
		
		sources[x][y] = true;
		lightMap[x][y] = (byte) size;
		updateLight(x, y, size);
	}
	
	private static void updateLight(int x, int y, int size) {
		boolean updated = true;
		while (updated) {
			updated = false;
			for (LightOffset lightOffset : kernel) {
				if (lightOffset.dist > size) break;
				byte light = 0;
				int xPos = x + lightOffset.x;
				if (xPos < 0) continue;
				if (xPos >= 64) continue;
				int yPos = y + lightOffset.y;
				if (yPos < 0) continue;
				if (yPos >= 64) continue;
				if (!tiles[xPos][yPos]) {
					light = (byte) (Math.max(
							Math.max(
									getLight(xPos - 1, yPos),
									getLight(xPos + 1, yPos)
							),
							Math.max(
									getLight(xPos, yPos - 1),
									getLight(xPos, yPos + 1)
							)
					) - 1);
					if (light < 0) light = 0;
				}
				int srcLight = lightMap[xPos][yPos];
				if (light > srcLight) {
					updated = true;
					lightMap[xPos][yPos] = light;
				}
			}
		}
	}
	
	protected static byte getLight(int x, int y) {
		if (x < 0 || y < 0 || x >= 64 || y >= 64) return 0;
		return lightMap[x][y];
	}
}

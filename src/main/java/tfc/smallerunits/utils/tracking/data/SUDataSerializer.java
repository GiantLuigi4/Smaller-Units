package tfc.smallerunits.utils.tracking.data;

import net.minecraft.util.math.BlockPos;

public abstract class SUDataSerializer<T> {
	// TODO: serializable serializer
	
	public static final SUDataSerializer<BlockPos> BLOCK_POS = new SUDataSerializer<BlockPos>() {
		@Override
		public byte[] serialize(BlockPos obj) {
			return writeInts(obj.getX(), obj.getY(), obj.getZ());
		}
		
		@Override
		public BlockPos read(byte[] bytes) {
			return new BlockPos(
					readInt(0, bytes),
					readInt(4, bytes),
					readInt(8, bytes)
			);
		}
	};
	
	public static final SUDataSerializer<Byte> BYTE = new SUDataSerializer<Byte>() {
		@Override
		public byte[] serialize(Byte obj) {
			return new byte[obj];
		}
		
		@Override
		public Byte read(byte[] bytes) {
			return bytes[0];
		}
	};
	
	// https://stackoverflow.com/a/7619315
	public static int readInt(int indx, byte[] bytes) {
		return ((bytes[0] & 0xFF) << 24) |
				((bytes[1] & 0xFF) << 16) |
				((bytes[2] & 0xFF) << 8) |
				((bytes[3] & 0xFF) << 0);
	}
	
	// https://stackoverflow.com/a/7619315
	public static byte[] writeInts(int... values) {
		byte[] bytes1 = new byte[values.length * 4];
		for (int i = 0; i < values.length; i++) {
			int v = values[i];
			bytes1[i * 4] = (byte)(v >> 24);
			bytes1[i * 4 + 1] = (byte)(v >> 16);
			bytes1[i * 4 + 2] = (byte)(v >> 8);
			bytes1[i * 4 + 3] = (byte)(v);
		}
		return bytes1;
	}
	
	public abstract byte[] serialize(T obj);
	
	public abstract T read(byte[] bytes);
}
a
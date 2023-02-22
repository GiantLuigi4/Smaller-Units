package tfc.smallerunits.simulation.level.server.saving;

import com.mojang.datafixers.DataFixer;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.io.File;

public class SUDimStorage extends DimensionDataStorage {
	public SUDimStorage(File p_78149_, DataFixer p_78150_) {
		super(p_78149_, p_78150_);
	}
	
//	public File getDataFile(String pName) {
//		return new File(this.dataFolder, pName + ".dat");
//	}
	
//	@Override
//	public CompoundTag readTagFromDisk(String pName, int pLevelVersion) throws IOException {
//		File file1 = this.getDataFile(pName);
//
//		FileInputStream fileinputstream = new FileInputStream(file1);
//		CompoundTag tag = NbtIo.readCompressed(fileinputstream);
//		fileinputstream.close();
//		return tag;
//	}
}

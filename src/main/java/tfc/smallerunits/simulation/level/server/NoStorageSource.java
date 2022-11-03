package tfc.smallerunits.simulation.level.server;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;
import tfc.smallerunits.mojangpls.NoPath;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Optional;

public class NoStorageSource extends LevelStorageSource {
	private static Unsafe theUnsafe;
	
	static {
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			theUnsafe = (Unsafe) f.get(null);
		} catch (Throwable e) {
			RuntimeException ex = new RuntimeException(e.getMessage(), e);
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
	}
	
	public NoStorageSource(DataFixer p_78201_) {
		super(new NoPath(), new NoPath(), p_78201_);
	}
	
	public static NoStorageSource make() {
		try {
			return (NoStorageSource) theUnsafe.allocateInstance(NoStorageSource.class);
		} catch (Throwable e) {
			RuntimeException ex = new RuntimeException(e.getMessage(), e);
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
	}
	
	@Override
	public LevelStorageAccess createAccess(String pSaveName) throws IOException {
		try {
			return (LevelStorageAccess) theUnsafe.allocateInstance(UnitStorageAccess.class);
		} catch (InstantiationException e) {
			RuntimeException ex = new RuntimeException(e.getMessage(), e);
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
	}
	
	public class UnitStorageAccess extends LevelStorageAccess {
		public UnitStorageAccess(String p_78276_) throws IOException {
			super(p_78276_);
		}
		
		@Override
		public String getLevelId() {
			return super.getLevelId();
		}
		
		@Override
		public Path getLevelPath(LevelResource pFolderName) {
			return new NoPath();
		}
		
		@Override
		public Path getDimensionPath(ResourceKey<Level> pDimensionPath) {
			return new NoPath();
		}
		
		@Override
		public PlayerDataStorage createPlayerStorage() {
			return super.createPlayerStorage();
		}
		
		@Nullable
		@Override
		public LevelSummary getSummary() {
			return super.getSummary();
		}
		
		@Nullable
		@Override
		public WorldData getDataTag(DynamicOps<Tag> p_211748_, DataPackConfig p_211749_, Lifecycle p_211750_) {
			return null;
		}
		
		@Override
		public void readAdditionalLevelSaveData() {
		}
		
		@Nullable
		@Override
		public DataPackConfig getDataPacks() {
			return super.getDataPacks();
		}
		
		@Override
		public void saveDataTag(RegistryAccess pRegistries, WorldData pServerConfiguration) {
		}
		
		@Override
		public void saveDataTag(RegistryAccess pRegistries, WorldData pServerConfiguration, @Nullable CompoundTag pHostPlayerNBT) {
		}
		
		@Override
		public Optional<Path> getIconFile() {
			return super.getIconFile();
		}
		
		@Override
		public Path getWorldDir() {
			return super.getWorldDir();
		}
		
		@Override
		public void deleteLevel() throws IOException {
		}
		
		@Override
		public void renameLevel(String pSaveName) throws IOException {
		}
		
		@Override
		public long makeWorldBackup() throws IOException {
			return 0;
		}
		
		@Override
		public void close() throws IOException {
			super.close();
		}
	}
	
	;
}

package tfc.smallerunits.mojangpls;

import net.minecraftforge.fml.CrashReportCallables;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;

public class NoFSProvider extends FileSystemProvider {
	public static final NoFSProvider INSTANCE = new NoFSProvider();
	protected static final Iterator<Path> pathIterator = Collections.emptyIterator();
	private static final FileStore store = new FileStore() {
		@Override
		public String name() {
			return "no";
		}
		
		@Override
		public String type() {
			return "null";
		}
		
		@Override
		public boolean isReadOnly() {
			return false;
		}
		
		@Override
		public long getTotalSpace() throws IOException {
			return 0;
		}
		
		@Override
		public long getUsableSpace() throws IOException {
			return 0;
		}
		
		@Override
		public long getUnallocatedSpace() throws IOException {
			return 0;
		}
		
		@Override
		public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
			return false;
		}
		
		@Override
		public boolean supportsFileAttributeView(String name) {
			return false;
		}
		
		@Override
		public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
			try {
				return type.newInstance();
			} catch (Throwable ignored) {
				return null;
			}
		}
		
		@Override
		public Object getAttribute(String attribute) throws IOException {
			return "";
		}
	};
	
	@Override
	public String getScheme() {
		return "nope";
	}
	
	@Override
	public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
		return NoFileSystem.INSTANCE;
	}
	
	@Override
	public FileSystem getFileSystem(URI uri) {
		return NoFileSystem.INSTANCE;
	}
	
	@NotNull
	@Override
	public Path getPath(@NotNull URI uri) {
		return new NoPath();
	}
	
	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
		return new SeekableByteChannel() {
			@Override
			public int read(ByteBuffer dst) throws IOException {
				return -1;
			}
			
			@Override
			public int write(ByteBuffer src) throws IOException {
				return 0;
			}
			
			@Override
			public long position() throws IOException {
				return 0;
			}
			
			@Override
			public SeekableByteChannel position(long newPosition) throws IOException {
				return this;
			}
			
			@Override
			public long size() throws IOException {
				return 0;
			}
			
			@Override
			public SeekableByteChannel truncate(long size) throws IOException {
				return this;
			}
			
			@Override
			public boolean isOpen() {
				return false;
			}
			
			@Override
			public void close() throws IOException {
			
			}
		};
	}
	
	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
		return new DirectoryStream<>() {
			@Override
			public Iterator<Path> iterator() {
				return pathIterator;
			}
			
			@Override
			public void close() throws IOException {
			}
		};
	}
	
	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
	
	}
	
	@Override
	public void delete(Path path) throws IOException {
	
	}
	
	@Override
	public void copy(Path source, Path target, CopyOption... options) throws IOException {
	
	}
	
	@Override
	public void move(Path source, Path target, CopyOption... options) throws IOException {
	
	}
	
	@Override
	public boolean isSameFile(Path path, Path path2) throws IOException {
		return path.equals(path2);
	}
	
	@Override
	public boolean isHidden(Path path) throws IOException {
		return true;
	}
	
	@Override
	public FileStore getFileStore(Path path) {
		return store;
	}
	
	@Override
	public void checkAccess(Path path, AccessMode... modes) throws IOException {
	
	}
	
	@Override
	public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
		try {
			return type.newInstance();
		} catch (Throwable ignored) {
			StringBuilder builder = new StringBuilder();
			builder.append("-- INFO --\n");
			builder.append(type.getName());
			CrashReportCallables.registerCrashCallable("Smaller Units", builder::toString);
			throw new RuntimeException("Attribute could not be created, game will most likely crash regardless");
		}
	}
	
	@Override
	public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
		try {
			if (type.equals(BasicFileAttributes.class)) {
				return (A) new BasicFileAttributes() {
					@Override
					public FileTime lastModifiedTime() {
						return FileTime.fromMillis(0);
					}
					
					@Override
					public FileTime lastAccessTime() {
						return lastModifiedTime();
					}
					
					@Override
					public FileTime creationTime() {
						return lastModifiedTime();
					}
					
					@Override
					public boolean isRegularFile() {
						return false;
					}
					
					@Override
					public boolean isDirectory() {
						return true;
					}
					
					@Override
					public boolean isSymbolicLink() {
						return false;
					}
					
					@Override
					public boolean isOther() {
						return false;
					}
					
					@Override
					public long size() {
						return 0;
					}
					
					@Override
					public Object fileKey() {
						return this;
					}
				};
			}
			return type.newInstance();
		} catch (Throwable ignored) {
			StringBuilder builder = new StringBuilder();
			builder.append("-- INFO --\n");
			builder.append(type.getName());
			CrashReportCallables.registerCrashCallable("Smaller Units", builder::toString);
			throw new RuntimeException("Attribute could not be created, game will most likely crash regardless");
		}
	}
	
	@Override
	public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
		return new HashMap<>();
	}
	
	@Override
	public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
	
	}
}

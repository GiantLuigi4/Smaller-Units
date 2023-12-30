package tfc.smallerunits.mojangpls;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class NoFileSystem extends FileSystem {
	public static final NoFileSystem INSTANCE = new NoFileSystem();
	protected static final Iterable<Path> pathIterable = Collections.emptyList();
	Iterable<FileStore> stores = Arrays.asList(NoFSProvider.INSTANCE.getFileStore(null));
	
	public NoFileSystem() {
	}
	
	@Override
	public FileSystemProvider provider() {
		return NoFSProvider.INSTANCE;
	}
	
	@Override
	public void close() throws IOException {
	
	}
	
	@Override
	public boolean isOpen() {
		return false;
	}
	
	@Override
	public boolean isReadOnly() {
		return false;
	}
	
	@Override
	public String getSeparator() {
		return "\\";
	}
	
	@Override
	public Iterable<Path> getRootDirectories() {
		return pathIterable;
	}
	
	@Override
	public Iterable<FileStore> getFileStores() {
		return stores;
	}
	
	@Override
	public Set<String> supportedFileAttributeViews() {
		return Collections.emptySet();
	}
	
	@NotNull
	@Override
	public Path getPath(@NotNull String first, @NotNull String... more) {
		return new NoPath();
	}
	
	@Override
	public PathMatcher getPathMatcher(String syntaxAndPattern) {
		return new PathMatcher() {
			@Override
			public boolean matches(Path path) {
				return true;
			}
		};
	}
	
	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService() {
		return new UserPrincipalLookupService() {
			@Override
			public UserPrincipal lookupPrincipalByName(String name) throws IOException {
				return new UserPrincipal() {
					@Override
					public String getName() {
						return name;
					}
				};
			}
			
			@Override
			public GroupPrincipal lookupPrincipalByGroupName(String group) throws IOException {
				return new GroupPrincipal() {
					@Override
					public String getName() {
						return group;
					}
				};
			}
		};
	}
	
	@Override
	public WatchService newWatchService() throws IOException {
		return new WatchService() {
			WatchKey dummyKey = new WatchKey() {
				@Override
				public boolean isValid() {
					return false;
				}
				
				@Override
				public List<WatchEvent<?>> pollEvents() {
					return Collections.emptyList();
				}
				
				@Override
				public boolean reset() {
					return false;
				}
				
				@Override
				public void cancel() {
				
				}
				
				@Override
				public Watchable watchable() {
					return new Watchable() {
						@Override
						public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
							return dummyKey;
						}
						
						@Override
						public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
							return dummyKey;
						}
					};
				}
			};
			
			@Override
			public void close() throws IOException {
			
			}
			
			@Override
			public WatchKey poll() {
				return dummyKey;
			}
			
			@Override
			public WatchKey poll(long timeout, TimeUnit unit) throws InterruptedException {
				return dummyKey;
			}
			
			@Override
			public WatchKey take() throws InterruptedException {
				return dummyKey;
			}
		};
	}
}

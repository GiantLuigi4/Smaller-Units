package tfc.smallerunits.mojangpls;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;

public class NoPath implements Path {
	@NotNull
	@Override
	public FileSystem getFileSystem() {
		// TODO:
		return null;
	}
	
	@Override
	public boolean isAbsolute() {
		return false;
	}
	
	@Override
	public Path getRoot() {
		return this;
	}
	
	@Override
	public Path getFileName() {
		return this;
	}
	
	@Override
	public Path getParent() {
		return this;
	}
	
	@Override
	public int getNameCount() {
		return 0;
	}
	
	@NotNull
	@Override
	public Path getName(int index) {
		return this;
	}
	
	@NotNull
	@Override
	public Path subpath(int beginIndex, int endIndex) {
		return this;
	}
	
	@Override
	public boolean startsWith(@NotNull Path other) {
		return false;
	}
	
	@Override
	public boolean endsWith(@NotNull Path other) {
		return false;
	}
	
	@Override
	public Path normalize() {
		return this;
	}
	
	@NotNull
	@Override
	public Path resolve(@NotNull Path other) {
		return this;
	}
	
	@NotNull
	@Override
	public Path resolve(@NotNull String other) {
		return this;
	}
	
	@NotNull
	@Override
	public Path resolveSibling(@NotNull Path other) {
		return this;
	}
	
	@NotNull
	@Override
	public Path resolveSibling(@NotNull String other) {
		return this;
	}
	
	@NotNull
	@Override
	public File toFile() {
		return new NoFile();
	}
	
	@NotNull
	@Override
	public Path relativize(@NotNull Path other) {
		return this;
	}
	
	@NotNull
	@Override
	public URI toUri() {
		return null;
	}
	
	@NotNull
	@Override
	public Path toAbsolutePath() {
		return this;
	}
	
	@NotNull
	@Override
	public Path toRealPath(@NotNull LinkOption... options) throws IOException {
		return this;
	}
	
	@Override
	public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
		return null;
	}
	
	@Override
	public int compareTo(Path other) {
		return 0;
	}
}

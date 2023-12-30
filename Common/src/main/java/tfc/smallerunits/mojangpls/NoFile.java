package tfc.smallerunits.mojangpls;

import java.io.File;

public class NoFile extends File {
	public NoFile() {
		super("");
	}
	
	@Override
	public boolean mkdirs() {
		return false;
	}
	
	@Override
	public boolean mkdir() {
		return false;
	}
}

package tfc.smallerunits.utils.config.annoconfg;

import java.io.File;

public abstract class Config {
	public abstract void write(File fl);
	public abstract void read(File fl);
}

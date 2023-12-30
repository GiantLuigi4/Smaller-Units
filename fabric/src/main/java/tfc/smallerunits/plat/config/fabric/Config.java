package tfc.smallerunits.plat.config.fabric;

import java.io.File;

public abstract class Config {
	public abstract void write(File fl);
	public abstract void read(File fl);
}
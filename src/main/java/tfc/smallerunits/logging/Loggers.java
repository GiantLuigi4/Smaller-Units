package tfc.smallerunits.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Loggers {
	public static final Logger WORLD_LOGGER;
	
	static {
		WORLD_LOGGER = LoggerFactory.getLogger("SU:WorldMixin");
	}
}

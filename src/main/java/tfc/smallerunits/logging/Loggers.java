package tfc.smallerunits.logging;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Loggers {
	public static final Logger WORLD_LOGGER;
	public static final Logger UNITSPACE_LOGGER;
	
	static {
		WORLD_LOGGER = LogManager.getLogger("SU:WorldMixin");
		UNITSPACE_LOGGER = LogManager.getLogger("SU:UnitSpace");
	}
}

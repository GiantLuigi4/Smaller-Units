package tfc.smallerunits.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Loggers {
	public static final Logger WORLD_LOGGER;
	public static final Logger UNITSPACE_LOGGER;
	public static final Logger PACKET_HACKS_LOGGER;
	public static final Logger UNIT_PALLET_LOGGER;
	public static final Logger SU_LOGGER;
	public static final Logger CHUNK_CACHE;
	
	static {
		WORLD_LOGGER = LoggerFactory.getLogger("SU:WorldMixin");
		UNITSPACE_LOGGER = LoggerFactory.getLogger("SU:UnitSpace");
		PACKET_HACKS_LOGGER = LoggerFactory.getLogger("SU:PacketHacks");
		UNIT_PALLET_LOGGER = LoggerFactory.getLogger("SU:UnitPallet");
		SU_LOGGER = LoggerFactory.getLogger("SU:Mod");
		CHUNK_CACHE = LoggerFactory.getLogger("SU:ChunkCache");
	}
}

package tfc.smallerunits.simulation.world;

import net.minecraft.world.entity.Entity;

import java.util.UUID;

public interface ITickerWorld {
	int getUPB();
	
	void handleRemoval();
	
	void SU$removeEntity(Entity pEntity);
	
	void SU$removeEntity(UUID uuid);
}

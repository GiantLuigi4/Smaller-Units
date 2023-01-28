package tfc.smallerunits.mixin.dangit;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import tfc.smallerunits.client.access.workarounds.ParticleEngineHolder;

@Mixin(ClientLevel.class)
public class ClientLevelMixin implements ParticleEngineHolder {
	ParticleEngine engine;
	
	@Override
	public ParticleEngine myEngine() {
		return engine;
	}
	
	@Override
	public void setParticleEngine(ParticleEngine engine) {
		this.engine = engine;
	}
}

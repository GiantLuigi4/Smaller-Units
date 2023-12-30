package tfc.smallerunits.mixin.dangit;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import tfc.smallerunits.client.access.workarounds.ParticleEngineHolder;

// due to create deciding to make PonderWorld extend Level instead of ClientLevel, I need to target Level
@Mixin(Level.class)
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

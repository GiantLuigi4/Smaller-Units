package com.tfc.smallerunits.utils.world;

import net.minecraft.command.TimerCallbackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.storage.IServerWorldInfo;

import java.util.UUID;

public class FakeServerWorldInfo implements IServerWorldInfo {
	public FakeServerWorld owner;
	
	public FakeServerWorldInfo(FakeServerWorld owner) {
		this.owner = owner;
	}
	
	@Override
	public String getWorldName() {
		return "Smaller Units Ticking World";
	}
	
	@Override
	public int getRainTime() {
		return getParentInfo() == null ? 0 : getParentInfo().getRainTime();
	}
	
	@Override
	public void setRainTime(int time) {
	}
	
	public IServerWorldInfo getParentInfo() {
		if (owner instanceof IServerWorldInfo)
			return (IServerWorldInfo) owner.getWorldInfo();
		return null;
	}
	
	@Override
	public int getThunderTime() {
		return getParentInfo() == null ? 0 : getParentInfo().getThunderTime();
	}
	
	@Override
	public void setThunderTime(int time) {
	}
	
	@Override
	public int getClearWeatherTime() {
		return getParentInfo() == null ? 0 : getParentInfo().getClearWeatherTime();
	}
	
	@Override
	public void setClearWeatherTime(int time) {
	}
	
	@Override
	public int getWanderingTraderSpawnDelay() {
		return 1000;
	}
	
	@Override
	public void setWanderingTraderSpawnDelay(int delay) {
	}
	
	@Override
	public int getWanderingTraderSpawnChance() {
		return 0;
	}
	
	@Override
	public void setWanderingTraderSpawnChance(int chance) {
	}
	
	@Override
	public void setWanderingTraderID(UUID id) {
	}
	
	@Override
	public GameType getGameType() {
		return getParentInfo() == null ? GameType.NOT_SET : getParentInfo().getGameType();
	}
	
	@Override
	public void setGameType(GameType type) {
	}
	
	@Override
	public WorldBorder.Serializer getWorldBorderSerializer() {
		return getParentInfo() == null ? null : getParentInfo().getWorldBorderSerializer();
	}
	
	@Override
	public void setWorldBorderSerializer(WorldBorder.Serializer serializer) {
	}
	
	@Override
	public boolean isInitialized() {
		return true;
	}
	
	@Override
	public void setInitialized(boolean initializedIn) {
	}
	
	@Override
	public boolean areCommandsAllowed() {
		return getParentInfo() != null && getParentInfo().areCommandsAllowed();
	}
	
	@Override
	public TimerCallbackManager<MinecraftServer> getScheduledEvents() {
		return getParentInfo() == null ? null : getParentInfo().getScheduledEvents();
	}
	
	@Override
	public int getSpawnX() {
		return 0;
	}
	
	@Override
	public void setSpawnX(int x) {
	}
	
	@Override
	public int getSpawnY() {
		return 0;
	}
	
	@Override
	public void setSpawnY(int y) {
	}
	
	@Override
	public int getSpawnZ() {
		return 0;
	}
	
	@Override
	public void setSpawnZ(int z) {
	}
	
	@Override
	public float getSpawnAngle() {
		return 0;
	}
	
	@Override
	public void setSpawnAngle(float angle) {
	}
	
	@Override
	public long getGameTime() {
		return getParentInfo() == null ? 0 : getParentInfo().getGameTime();
	}
	
	@Override
	public void setGameTime(long time) {
	
	}
	
	@Override
	public long getDayTime() {
		return getParentInfo() == null ? 0 : getParentInfo().getDayTime();
	}
	
	@Override
	public void setDayTime(long time) {
	
	}
	
	@Override
	public boolean isThundering() {
		return getParentInfo() != null && getParentInfo().isThundering();
	}
	
	@Override
	public void setThundering(boolean thunderingIn) {
	}
	
	@Override
	public boolean isRaining() {
		return getParentInfo() != null && getParentInfo().isRaining();
	}
	
	@Override
	public void setRaining(boolean isRaining) {
	}
	
	@Override
	public boolean isHardcore() {
		return getParentInfo() != null && getParentInfo().isHardcore();
	}
	
	@Override
	public GameRules getGameRulesInstance() {
		return getParentInfo() == null ? null : getParentInfo().getGameRulesInstance();
	}
	
	@Override
	public Difficulty getDifficulty() {
		return getParentInfo() == null ? null : getParentInfo().getDifficulty();
	}
	
	@Override
	public boolean isDifficultyLocked() {
		return getParentInfo() != null && getParentInfo().isDifficultyLocked();
	}
}

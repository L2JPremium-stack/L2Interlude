package net.sf.l2j.solofarm.holder;

import java.util.List;

import net.sf.l2j.gameserver.templates.StatsSet;

public class SoloFarmConfig
{
    private final StatsSet _set;

    public SoloFarmConfig(StatsSet set)
    {
        _set = set;
    }

    public boolean isEnabled()
    {
        return _set.getBool("enabled", false);
    }

    public String getName()
    {
        return _set.getString("name", "SoloFarm");
    }

    public int getEntryNpcId()
    {
        return _set.getInteger("entryNpcId", 0);
    }

    public int getEntryX() { return _set.getInteger("entryX", 0); }
    public int getEntryY() { return _set.getInteger("entryY", 0); }
    public int getEntryZ() { return _set.getInteger("entryZ", 0); }

    public int getExitX() { return _set.getInteger("exitX", 0); }
    public int getExitY() { return _set.getInteger("exitY", 0); }
    public int getExitZ() { return _set.getInteger("exitZ", 0); }

    public int getPriceItemId()
    {
        return _set.getInteger("priceItemId", 57);
    }

    public int getPricePerMob()
    {
        return _set.getInteger("pricePerMob", 1000);
    }

    public int getMinBuy()
    {
        return _set.getInteger("minBuy", 100);
    }

    public int getMaxBuy()
    {
        return _set.getInteger("maxBuy", 1000);
    }

    public boolean isCustomBuy()
    {
        return _set.getBool("customBuy", true);
    }

    public int getInstanceMinutes()
    {
        return _set.getInteger("instanceMinutes", 30);
    }

    public int getMaxAliveMonsters()
    {
        return _set.getInteger("maxAliveMonsters", 20);
    }

    public int getRespawnOnKillDelay()
    {
        return _set.getInteger("respawnOnKillDelay", 2);
    }

    public List<SoloFarmReward> getRewards()
    {
        return _set.getList("rewards");
    }

    public long calculatePrice(int amount)
    {
        return (long) amount * getPricePerMob();
    }
}
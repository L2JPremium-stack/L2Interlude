package net.sf.l2j.event.tvt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.Player;

public class TvTEventTeam
{
    private final String _name;
    private int[] _coordinates = new int[3];
    private List<Location> _spawnPoints = null;
    private short _points;
    private Map<Integer, Player> _participatedPlayers = new HashMap<>();
    private Map<Integer, Integer> _pointPlayers = new HashMap<>();

    // Construtor antigo (único local fixo)
    public TvTEventTeam(String name, int[] coordinates)
    {
        _name = name;
        _coordinates = coordinates;
        _points = 0;
    }

    // Novo construtor (multi-spawns)
    public TvTEventTeam(String name, List<Location> spawnPoints)
    {
        _name = name;
        _spawnPoints = spawnPoints;
        _points = 0;
    }

    public boolean addPlayer(Player playerInstance)
    {
        if (playerInstance == null)
            return false;

        synchronized (_participatedPlayers)
        {
            _participatedPlayers.put(playerInstance.getObjectId(), playerInstance);
        }
        return true;
    }

    public void removePlayer(int playerObjectId)
    {
        synchronized (_participatedPlayers)
        {
            _participatedPlayers.remove(playerObjectId);
        }
    }

    public void increasePoints()
    {
        ++_points;
    }

    public void increasePoints(int charId)
    {
        synchronized (_pointPlayers)
        {
            _pointPlayers.put(charId, _pointPlayers.getOrDefault(charId, 0) + 1);
        }
    }

    public void cleanMe()
    {
        _participatedPlayers.clear();
        _participatedPlayers = new HashMap<>();
        _pointPlayers.clear();
        _pointPlayers = new HashMap<>();
        _points = 0;
    }

    public boolean containsPlayer(int playerObjectId)
    {
        synchronized (_participatedPlayers)
        {
            return _participatedPlayers.containsKey(playerObjectId);
        }
    }

    public String getName()
    {
        return _name;
    }

    public int[] getCoordinates()
    {
        if (_spawnPoints != null && !_spawnPoints.isEmpty())
        {
            Location loc = getRandomSpawn();
            return new int[] { loc.getX(), loc.getY(), loc.getZ() };
        }

        return _coordinates;
    }

    public Location getRandomSpawn()
    {
        if (_spawnPoints == null || _spawnPoints.isEmpty())
            return new Location(_coordinates[0], _coordinates[1], _coordinates[2]);

        return _spawnPoints.get(Rnd.get(_spawnPoints.size()));
    }

    public short getPoints()
    {
        return _points;
    }

    public Map<Integer, Player> getParticipatedPlayers()
    {
        synchronized (_participatedPlayers)
        {
            return _participatedPlayers;
        }
    }

    public int getParticipatedPlayerCount()
    {
        synchronized (_participatedPlayers)
        {
            return _participatedPlayers.size();
        }
    }

    public Map<Integer, Integer> getScoredPlayers()
    {
        synchronized (_pointPlayers)
        {
            return _pointPlayers;
        }
    }

    public int getScoredPlayerCount()
    {
        synchronized (_pointPlayers)
        {
            return _pointPlayers.size();
        }
    }

    public boolean onScoredPlayer(int charId)
    {
        synchronized (_pointPlayers)
        {
            return _pointPlayers.getOrDefault(charId, 0) > 0;
        }
    }
}

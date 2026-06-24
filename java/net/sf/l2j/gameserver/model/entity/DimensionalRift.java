package net.sf.l2j.gameserver.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.Earthquake;

/**
 * Thanks to L2Fortress and balancer.ru - kombat
 */
public class DimensionalRift
{
	protected byte _type;
	protected L2Party _party;
	protected List<Byte> _completedRooms = new ArrayList<>();
	private static final long seconds_5 = 5000L;
	protected byte _currentJumps = 0;
	
	private Timer teleporterTimer;
	private TimerTask teleporterTimerTask;
	private Timer spawnTimer;
	private TimerTask spawnTimerTask;
	
	private Future<?> earthQuakeTask;
	
	protected byte _choosenRoom = -1;
	private boolean _hasJumped = false;
	protected List<Player> deadPlayers = new ArrayList<>();
	protected List<Player> revivedInWaitingRoom = new ArrayList<>();
	
	private boolean isBossRoom = false;
	
	public DimensionalRift(L2Party party, byte type, byte room)
	{
		DimensionalRiftManager.getInstance().getRoom(type, room).setPartyInside(true);
		
		_type = type;
		_party = party;
		_choosenRoom = room;
		
		int[] coords = getRoomCoord(room);
		party.setDimensionalRift(this);
		
		for (Player p : party.getPartyMembers())
			p.teleToLocation(coords[0], coords[1], coords[2], 0);
		
		createSpawnTimer(_choosenRoom);
		createTeleporterTimer(true);
	}
	
	public byte getType()
	{
		return _type;
	}
	
	public byte getCurrentRoom()
	{
		return _choosenRoom;
	}
	
	protected void createTeleporterTimer(final boolean reasonTP)
	{
		if (teleporterTimerTask != null)
		{
			teleporterTimerTask.cancel();
			teleporterTimerTask = null;
		}
		
		if (teleporterTimer != null)
		{
			teleporterTimer.cancel();
			teleporterTimer = null;
		}
		
		if (earthQuakeTask != null)
		{
			earthQuakeTask.cancel(false);
			earthQuakeTask = null;
		}
		
		teleporterTimer = new Timer();
		teleporterTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				if (_choosenRoom > -1)
					DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).unspawn().setPartyInside(false);
				
				if (reasonTP && _currentJumps < getMaxJumps() && _party.getMemberCount() > deadPlayers.size())
				{
					_currentJumps++;
					
					_completedRooms.add(_choosenRoom);
					_choosenRoom = -1;
					
					for (Player p : _party.getPartyMembers())
						if (!revivedInWaitingRoom.contains(p))
							teleportToNextRoom(p, false);
					
					createTeleporterTimer(true);
					createSpawnTimer(_choosenRoom);
				}
				else
				{
					for (Player p : _party.getPartyMembers())
						if (!revivedInWaitingRoom.contains(p))
							teleportToWaitingRoom(p);
					
					killRift();
					cancel();
				}
			}
		};
		
		if (reasonTP)
		{
			long jumpTime = calcTimeToNextJump();
			teleporterTimer.schedule(teleporterTimerTask, jumpTime); // Teleporter task, 8-10 minutes
			
			earthQuakeTask = ThreadPool.schedule(new Runnable()
			{
				@Override
				public void run()
				{
					for (Player p : _party.getPartyMembers())
						if (!revivedInWaitingRoom.contains(p))
							p.sendPacket(new Earthquake(p.getX(), p.getY(), p.getZ(), 65, 9));
				}
			}, jumpTime - 7000);
		}
		else
			teleporterTimer.schedule(teleporterTimerTask, seconds_5); // incorrect party member invited.
	}
	
	public void createSpawnTimer(final byte room)
	{
		if (spawnTimerTask != null)
		{
			spawnTimerTask.cancel();
			spawnTimerTask = null;
		}
		
		if (spawnTimer != null)
		{
			spawnTimer.cancel();
			spawnTimer = null;
		}
		
		spawnTimer = new Timer();
		spawnTimerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				DimensionalRiftManager.getInstance().getRoom(_type, room).spawn();
			}
		};
		
		spawnTimer.schedule(spawnTimerTask, Config.RIFT_SPAWN_DELAY);
	}
	
	public void partyMemberInvited()
	{
		createTeleporterTimer(false);
	}
	
	public void partyMemberExited(Player player)
	{
		if (deadPlayers.contains(player))
			deadPlayers.remove(player);
		
		if (revivedInWaitingRoom.contains(player))
			revivedInWaitingRoom.remove(player);
		
		if (_party.getMemberCount() < Config.RIFT_MIN_PARTY_SIZE || _party.getMemberCount() == 1)
		{
			for (Player p : _party.getPartyMembers())
				teleportToWaitingRoom(p);
			
			killRift();
		}
	}
	
	public void manualTeleport(Player player, L2Npc npc)
	{
		final L2Party party = player.getParty();
		if (party == null || !party.isInDimensionalRift())
			return;
		
		if (!party.isLeader(player))
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
			return;
		}
		
		if (_currentJumps == Config.RIFT_MAX_JUMPS)
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/UsedAllJumps.htm", npc);
			return;
		}
		
		if (_hasJumped)
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/AlreadyTeleported.htm", npc);
			return;
		}
		_hasJumped = true;
		
		DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).unspawn().setPartyInside(false);
		_completedRooms.add(_choosenRoom);
		_choosenRoom = -1;
		
		for (Player p : _party.getPartyMembers())
			teleportToNextRoom(p, true);
		
		DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).setPartyInside(true);
		
		createSpawnTimer(_choosenRoom);
		createTeleporterTimer(true);
	}
	
	public void manualExitRift(Player player, L2Npc npc)
	{
		if (!player.isInParty() || !player.getParty().isInDimensionalRift())
			return;
		
		if (player.getObjectId() != player.getParty().getPartyLeaderOID())
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
			return;
		}
		
		for (Player p : player.getParty().getPartyMembers())
			teleportToWaitingRoom(p);
		
		killRift();
	}
	
	/**
	 * This method allows to jump from one room to another. It calculates the next roomId.
	 * @param player to teleport
	 * @param cantJumpToBossRoom if true, Anakazel room can't be choosen (case of manual teleport).
	 */
	protected void teleportToNextRoom(Player player, boolean cantJumpToBossRoom)
	{
		if (_choosenRoom == -1)
		{
			List<Byte> emptyRooms;
			
			do
			{
				emptyRooms = DimensionalRiftManager.getInstance().getFreeRooms(_type);
				
				// Do not tp in the same room a second time
				emptyRooms.removeAll(_completedRooms);
				
				// If no room left, find any empty
				if (emptyRooms.isEmpty())
					emptyRooms = DimensionalRiftManager.getInstance().getFreeRooms(_type);
				
				// Pickup a random room
				_choosenRoom = emptyRooms.get(Rnd.get(1, emptyRooms.size()) - 1);
				
				// This code handles Anakazel's room special behavior.
				if (cantJumpToBossRoom)
				{
					while (_choosenRoom == 9)
						_choosenRoom = emptyRooms.get(Rnd.get(1, emptyRooms.size()) - 1);
				}
			}
			while (DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).isPartyInside());
		}
		
		DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).setPartyInside(true);
		int[] coords = getRoomCoord(_choosenRoom);
		player.teleToLocation(coords[0], coords[1], coords[2], 0);
	}
	
	protected void teleportToWaitingRoom(Player player)
	{
		DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
	}
	
	public void killRift()
	{
		_completedRooms = null;
		
		if (_party != null)
			_party.setDimensionalRift(null);
		
		_party = null;
		revivedInWaitingRoom = null;
		deadPlayers = null;
		
		if (earthQuakeTask != null)
		{
			earthQuakeTask.cancel(false);
			earthQuakeTask = null;
		}
		
		DimensionalRiftManager.getInstance().getRoom(_type, _choosenRoom).unspawn().setPartyInside(false);
		DimensionalRiftManager.getInstance().killRift(this);
	}
	
	public Timer getTeleportTimer()
	{
		return teleporterTimer;
	}
	
	public TimerTask getTeleportTimerTask()
	{
		return teleporterTimerTask;
	}
	
	public Timer getSpawnTimer()
	{
		return spawnTimer;
	}
	
	public TimerTask getSpawnTimerTask()
	{
		return spawnTimerTask;
	}
	
	public void setTeleportTimer(Timer t)
	{
		teleporterTimer = t;
	}
	
	public void setTeleportTimerTask(TimerTask tt)
	{
		teleporterTimerTask = tt;
	}
	
	public void setSpawnTimer(Timer t)
	{
		spawnTimer = t;
	}
	
	public void setSpawnTimerTask(TimerTask st)
	{
		spawnTimerTask = st;
	}
	
	private long calcTimeToNextJump()
	{
		int time = Rnd.get(Config.RIFT_AUTO_JUMPS_TIME_MIN, Config.RIFT_AUTO_JUMPS_TIME_MAX) * 1000;
		
		if (isBossRoom)
			return (long) (time * Config.RIFT_BOSS_ROOM_TIME_MUTIPLY);
		
		return time;
	}
	
	public void memberDead(Player player)
	{
		if (!deadPlayers.contains(player))
			deadPlayers.add(player);
	}
	
	public void memberRessurected(Player player)
	{
		if (deadPlayers.contains(player))
			deadPlayers.remove(player);
	}
	
	public void usedTeleport(Player player)
	{
		if (!revivedInWaitingRoom.contains(player))
			revivedInWaitingRoom.add(player);
		
		if (!deadPlayers.contains(player))
			deadPlayers.add(player);
		
		if (_party.getMemberCount() - revivedInWaitingRoom.size() < Config.RIFT_MIN_PARTY_SIZE)
		{
			for (Player p : _party.getPartyMembers())
				if (p != null && !revivedInWaitingRoom.contains(p))
					teleportToWaitingRoom(p);
			
			killRift();
		}
	}
	
	public List<Player> getDeadMemberList()
	{
		return deadPlayers;
	}
	
	public List<Player> getRevivedAtWaitingRoom()
	{
		return revivedInWaitingRoom;
	}
	
	public void checkBossRoom(byte room)
	{
		isBossRoom = DimensionalRiftManager.getInstance().getRoom(_type, room).isBossRoom();
	}
	
	public int[] getRoomCoord(byte room)
	{
		return DimensionalRiftManager.getInstance().getRoom(_type, room).getTeleportCoords();
	}
	
	public byte getMaxJumps()
	{
		if (Config.RIFT_MAX_JUMPS <= 8 && Config.RIFT_MAX_JUMPS >= 1)
			return (byte) Config.RIFT_MAX_JUMPS;
		
		return 4;
	}
}
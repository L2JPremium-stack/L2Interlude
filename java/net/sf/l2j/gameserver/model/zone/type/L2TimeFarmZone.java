package net.sf.l2j.gameserver.model.zone.type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.instancemanager.custom.TimeZoneManager;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.L2SpawnZone;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;
import net.sf.l2j.timezone.TimeFarmZoneManager;

/**
 * Time Farm Zone - Zona especial para rotação de farm.
 * @author Sarada
 */
public class L2TimeFarmZone extends L2SpawnZone
{
	private String _zoneName = "Unknown";
	private boolean _inviteParty = true;
	private final Set<String> _classes = new HashSet<>();
	private boolean _noDropProtection = false;
	private boolean _blockMultiplePlayers = false;
	private int _centerX;
	private int _centerY;
	private int _centerZ;
	 private final List<Location> _spawns = new ArrayList<>();
	private boolean loseBuff = true;
	private boolean _enabled = true; // por padrão, a zona estará ativa

	
	public void setZoneCenter(int x, int y, int z)
	{
	    _centerX = x;
	    _centerY = y;
	    _centerZ = z;
	}
	public L2TimeFarmZone(int id)
	{
		super(id);
		_inviteParty = false;
	}
	private boolean _registered = false;

	@Override
	public void setParameter(String name, String value)
	{
	    if (name.equalsIgnoreCase("name"))
	    {
	        setZoneName(value);
	        if (!_registered)
	        {
	            TimeFarmZoneManager.registerZone(this);
	            _registered = true;
	       //     System.out.println("ZoneManager: Setando nome da zona TimeFarmZone para: " + _zoneName);
	        }
	    }
	    else if (name.equalsIgnoreCase("EnabledZone"))
	    {
	        _enabled = Boolean.parseBoolean(value);
	    }

	    else if (name.equalsIgnoreCase("LoseBuff")) 
	    {
            loseBuff = Boolean.parseBoolean(value);
        }
	    else  if (name.equalsIgnoreCase("InviteParty"))
        {
            _inviteParty = Boolean.parseBoolean(value);
        }
	    else if (name.equalsIgnoreCase("BlockedClasses"))
	    {
	        for (String classId : value.split(";"))
	        {
	            _classes.add(classId.trim());
	        }
	    }
	    else if (name.equalsIgnoreCase("NoDropProtection"))
	    {
	        _noDropProtection = Boolean.parseBoolean(value);
	    }
	    else if (name.equalsIgnoreCase("BlockMultiplePlayers"))
	    {
	        _blockMultiplePlayers = Boolean.parseBoolean(value);
	    }

	    else
	    {
	        super.setParameter(name, value);
	    }
	}
	public boolean isEnabled()
	{
	    return _enabled;
	}

	public boolean isBlockMultiplePlayers()
	{
		return _blockMultiplePlayers;
	}
	// Verifica se a zona deve fazer o jogador perder buffs
    public boolean canLoseBuff() 
    {
        return loseBuff;
    }
    
	public boolean isInvitePartyAllowed()
	{
		return _inviteParty;
	}
	public boolean isNoDropProtection()
	{
		return _noDropProtection;
	}

	@Override
	protected void onEnter(Creature character)
	{
		if (!isEnabled())
			return;
		
		if (character instanceof Player)
		{
			final Player player = (Player) character;

			character.setInsideZone(ZoneId.TIME_FARM_ZONE, true);
			player.updatePvPFlag(1);
			player.broadcastUserInfo();
			player.sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
			 // Checa se a classe é bloqueada
	        String classIdStr = String.valueOf(player.getClassId().getId());
	        if (!player.isGM() && _classes.contains(classIdStr))
	        {
	            player.sendMessage("Your class is not allowed in this zone.");
	            ThreadPool.schedule(() -> {
	                character.teleToLocation(83597, 147888, -3405, 0);
	            }, 1000);
	            return;
	        }
	        if (_blockMultiplePlayers)
	        {
	            MaxPlayersOnArea(player);
	        }
			if (!_inviteParty)
			{
				if (player.getParty() != null)
				{
					player.leaveParty();
				}
			}
		}
		
	}

	@Override
	protected void onExit(Creature character)
	{
		if (!isEnabled())
			return;		
		
		if (character instanceof Player)
		{
			final Player activeChar = (Player) character;

			character.setInsideZone(ZoneId.TIME_FARM_ZONE, false);
			activeChar.updatePvPFlag(0);
			activeChar.broadcastUserInfo();

			PvpFlagTaskManager.getInstance().add(activeChar, Config.PVP_NORMAL_TIME);
			character.sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
		}
	}
	public boolean MaxPlayersOnArea(Player activeChar)
	{
	    return TimeZoneManager.getInstance().checkPlayersArea(activeChar, 1, this);
	}


	@Override
	public void onDieInside(Creature character)
	{
		// Pode adicionar lógica ao morrer dentro da zona
	}

	@Override
	public void onReviveInside(Creature character)
	{
		// Pode adicionar lógica ao reviver dentro da zona
	}

	// Getter e Setter para o nome da zona (lido do XML)
	public void setZoneName(String name)
	{
		_zoneName = name;
	}

	public String getZoneName()
	{
		return _zoneName;
	}
	public void parseZoneNode(org.w3c.dom.Node node)
	{
	    // Esse método deve ser chamado ao carregar o XML da zona, para capturar <node X= Y= Z= />
	    if (node.getNodeName().equals("node"))
	    {
	        org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
	        int x = Integer.parseInt(attrs.getNamedItem("X").getNodeValue());
	        int y = Integer.parseInt(attrs.getNamedItem("Y").getNodeValue());
	        int z = 0; // padrão se não houver Z
	        if (attrs.getNamedItem("Z") != null)
	        {
	            z = Integer.parseInt(attrs.getNamedItem("Z").getNodeValue());
	        }
	        setZoneCenter(x, y, z);
	    }
	}

	public Location getZoneCenter()
	{
	    return new Location(_centerX, _centerY, _centerZ);
	}
	// Método para adicionar spawn (chamado no loader XML)
    public void addSpawn(Location loc)
    {
        _spawns.add(loc);
    }

    @Override
	public List<Location> getSpawns()
    {
        return _spawns;
    }

    // Método que retorna um spawn aleatório da lista
    public Location getRandomSpawn()
    {
        if (_spawns.isEmpty())
        {
            // Retorna o centro da zona como fallback, ou null, dependendo do seu uso
            return getZoneCenter();
        }
        int idx = (int) (Math.random() * _spawns.size());
        return _spawns.get(idx);
    }
}

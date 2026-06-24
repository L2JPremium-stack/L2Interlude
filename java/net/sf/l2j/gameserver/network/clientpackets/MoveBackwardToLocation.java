package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.network.serverpackets.StopMove;

/**
 * Client request to move backward/to location.
 *
 * This version keeps the client packet permissive enough for normal L2 movement,
 * but the server remains authoritative: origin, max distance and geodata are
 * validated before the AI receives the destination.
 */
public class MoveBackwardToLocation extends L2GameClientPacket
{
	private static final int MAX_MOVE_DISTANCE = 9900;
	private static final int MAX_ORIGIN_DESYNC = 750;
	private static final int MAX_Z_DIFF_WITHOUT_GEO = 500;
	private static final int MIN_MOVE_DISTANCE = 8;
	private static final int MIN_BLOCKED_MOVE_DISTANCE = 32;
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	
	private int _originX;
	private int _originY;
	private int _originZ;
	
	private boolean _malformed;
	
	@Override
	protected void readImpl()
	{
		try
		{
			_targetX = readD();
			_targetY = readD();
			_targetZ = readD();
			
			_originX = readD();
			_originY = readD();
			_originZ = readD();
			
			// Reserved/unknown. Retail clients send it, but movement must not trust it.
			readD();
		}
		catch (BufferUnderflowException e)
		{
			_malformed = true;
		}
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (_malformed)
		{
			rejectMove(player, true);
			return;
		}
		
		if (player.isOutOfControl() || player.isMovementDisabled())
		{
			rejectMove(player, false);
			return;
		}
		
		cancelEnchant(player);
		
		if (handleTeleportMode(player))
			return;
		
		if (isSameLocation() || isTooCloseToCurrentPosition(player))
		{
			stopMoveClean(player);
			return;
		}
		
		// Do not trust movement packets coming from a very different origin.
		if (!isValidOrigin(player))
		{
			rejectMove(player, true);
			return;
		}
		
		// Prevent malicious clients from requesting huge one-packet movement.
		if (!isValidDistance())
		{
			rejectMove(player, true);
			return;
		}
		
		final Location destination = buildValidatedDestination(player);
		if (destination == null)
		{
			// Não pune como walker quando foi apenas click bloqueado por parede.
			stopMoveClean(player);
			return;
		}
		
		// Se a geodata cortou para praticamente o mesmo lugar, não força o player colar na parede.
		if (isNear(player.getX(), player.getY(), player.getZ(), destination.getX(), destination.getY(), destination.getZ(), MIN_MOVE_DISTANCE))
		{
			stopMoveClean(player);
			return;
		}
		
		player.getAI().setIntention(CtrlIntention.MOVE_TO, destination);
	}
	
	private static void stopMoveClean(Player player)
	{
		player.getAI().setIntention(CtrlIntention.ACTIVE);
		player.sendPacket(new StopMove(player));
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private static void rejectMove(Player player, boolean violation)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		if (violation && Config.L2WALKER_PROTECTION)
			player.increaseMovePacketViolation();
	}
	
	private static void cancelEnchant(Player player)
	{
		if (player.getActiveEnchantItem() == null)
			return;
		
		player.setActiveEnchantItem(null);
		player.sendPacket(EnchantResult.CANCELLED);
		player.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED);
	}
	
	private boolean isSameLocation()
	{
		return _targetX == _originX && _targetY == _originY && _targetZ == _originZ;
	}
	
	private boolean isTooCloseToCurrentPosition(Player player)
	{
		return isNear(player.getX(), player.getY(), player.getZ(), _targetX, _targetY, _targetZ, MIN_MOVE_DISTANCE);
	}
	
	private boolean handleTeleportMode(Player player)
	{
		if (player.getTeleMode() <= 0)
			return false;
		
		if (!player.isGM())
		{
			player.setTeleMode(0);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}
		
		if (player.getTeleMode() == 1)
			player.setTeleMode(0);
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.teleToLocation(_targetX, _targetY, _targetZ, 0);
		return true;
	}
	
	private boolean isValidOrigin(Player player)
	{
		return getDistanceSq(_originX, _originY, _originZ, player.getX(), player.getY(), player.getZ()) <= (long) MAX_ORIGIN_DESYNC * MAX_ORIGIN_DESYNC;
	}
	
	private boolean isValidDistance()
	{
		final long dx = (long) _targetX - _originX;
		final long dy = (long) _targetY - _originY;
		return dx * dx + dy * dy <= (long) MAX_MOVE_DISTANCE * MAX_MOVE_DISTANCE;
	}
	
	private Location buildValidatedDestination(Player player)
	{
		final int targetX = _targetX;
		final int targetY = _targetY;
		final int targetZ = _targetZ;
		
 
		if (isFlyingMount(player))
			return new Location(targetX, targetY, targetZ);
		
		if (!Config.ENABLE_GEODATA)
		{
			if (Math.abs(targetZ - player.getZ()) > MAX_Z_DIFF_WITHOUT_GEO)
				return null;
			
			return new Location(targetX, targetY, targetZ);
		}
		
		final GeoEngine geo = GeoEngine.getInstance();
		
		 
		if (!geo.hasGeo(player.getX(), player.getY()) || !geo.hasGeo(targetX, targetY))
			return new Location(targetX, targetY, targetZ);
		
		final int normalizedTargetZ = geo.getHeight(targetX, targetY, targetZ);
		
 
		if (geo.canMoveToTarget(player.getX(), player.getY(), player.getZ(), targetX, targetY, targetZ))
			return new Location(targetX, targetY, normalizedTargetZ);
		
 
		if (Config.PATHFINDING)
			return new Location(targetX, targetY, normalizedTargetZ);
		
		 
		final Location validLoc = geo.canMoveToTargetLoc(player.getX(), player.getY(), player.getZ(), targetX, targetY, targetZ);
		if (validLoc == null)
			return null;
		
		if (isNear(player.getX(), player.getY(), player.getZ(), validLoc.getX(), validLoc.getY(), validLoc.getZ(), MIN_BLOCKED_MOVE_DISTANCE))
			return null;
		
		return validLoc;
	}
	
	private static boolean isFlyingMount(Player player)
	{
		return player.isMounted() && player.getMountType() == 2;
	}
	
	private static boolean isNear(int ax, int ay, int az, int bx, int by, int bz, int distance)
	{
		return getDistanceSq(ax, ay, az, bx, by, bz) <= (long) distance * distance;
	}
	
	private static long getDistanceSq(int ax, int ay, int az, int bx, int by, int bz)
	{
		final long dx = (long) ax - bx;
		final long dy = (long) ay - by;
		final long dz = (long) az - bz;
		return dx * dx + dy * dy + dz * dz;
	}
}

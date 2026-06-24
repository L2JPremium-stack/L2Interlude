package net.sf.l2j.gameserver.geoengine.pathfinding;

import net.sf.l2j.gameserver.geoengine.geodata.GeoLocation;

/**
 * Pathfinding node.
 * 
 * Safe version for Java 11 / aCis 360:
 * - keeps the original public API;
 * - avoids stale child/parent/cost data between buffer uses;
 * - keeps allocation low by reusing Node instances from NodeBuffer.
 */
public class Node
{
	private GeoLocation _loc;
	private Node _parent;
	private Node _child;
	private double _cost = -1000;

	public void setLoc(int x, int y, int z)
	{
		_loc = new GeoLocation(x, y, z);
	}

	public GeoLocation getLoc()
	{
		return _loc;
	}

	public void setParent(Node parent)
	{
		_parent = parent;
	}

	public Node getParent()
	{
		return _parent;
	}

	public void setChild(Node child)
	{
		_child = child;
	}

	public Node getChild()
	{
		return _child;
	}

	public void setCost(double cost)
	{
		_cost = cost;
	}

	public double getCost()
	{
		return _cost;
	}

	public boolean isUsed()
	{
		return _loc != null;
	}

	public boolean isClosed()
	{
		return _cost >= 0;
	}

	public void free()
	{
		_loc = null;
		_parent = null;
		_child = null;
		_cost = -1000;
	}
}

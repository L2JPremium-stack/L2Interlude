package net.sf.l2j.event.tournament;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;

public class InstanceHolder
{
	private int id;
	private List<L2DoorInstance> doors;
	
	public InstanceHolder(int id)
	{
		this.id = id;
		doors = new ArrayList<>();
	}
	
	public void openDoors()
	{
		for (L2DoorInstance door : doors)
			door.openMe();
	}
	
	public void closeDoors()
	{
		for (L2DoorInstance door : doors)
			door.closeMe();
	}
	
	public void addDoor(L2DoorInstance door)
	{
		doors.add(door);
	}
	
	public List<L2DoorInstance> getDoors()
	{
		return doors;
	}
	
	public int getId()
	{
		return id;
	}
}

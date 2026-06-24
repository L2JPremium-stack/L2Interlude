package net.sf.l2j.gameserver.model.actor.knownlist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.util.Util;

public class ObjectKnownList
{
	protected final L2Object _activeObject;
	protected final Map<Integer, L2Object> _knownObjects;
	
	public ObjectKnownList(L2Object activeObject)
	{
		_activeObject = activeObject;
		_knownObjects = new ConcurrentHashMap<>();
	}
	
 
	protected boolean canKnow(L2Object object)
	{
		if (object == null || object == _activeObject)
			return false;
		
		if (_activeObject == null || !_activeObject.isVisible() || !object.isVisible())
			return false;
		
		if (object.getInstance() != _activeObject.getInstance())
			return false;
		
		return true;
	}
	
	/**
	 * Add object to known list.<br>
	 * <b>Is overridden by children in most cases.</b>
	 * @param object : {@link L2Object} to be added.
	 * @return boolean : True, when object was successfully added.
	 */
	public boolean addKnownObject(L2Object object)
	{
		if (!canKnow(object))
			return false;
		
		if (_knownObjects.containsKey(object.getObjectId()))
			return false;
		
		if (!Util.checkIfInShortRadius(getDistanceToWatchObject(object), _activeObject, object, true))
			return false;
		
		return _knownObjects.put(object.getObjectId(), object) == null;
	}
	
	/**
	 * Remove object from known list.<br>
	 * <b>Is overridden by children in most cases.</b>
	 * @param object : {@link L2Object} to be removed.
	 * @return boolean : True, when object was successfully removed.
	 */
	public boolean removeKnownObject(L2Object object)
	{
		if (object == null)
			return false;
		
		return _knownObjects.remove(object.getObjectId()) != null;
	}
	
	/**
	 * Remove object from known list, which are invalid or beyond distance to forget.
	 */
	public final void forgetObjects()
	{
		if (_activeObject == null || !_activeObject.isVisible())
		{
			removeAllKnownObjects();
			return;
		}
		
		for (L2Object object : _knownObjects.values())
		{
			if (object == null || object == _activeObject || !object.isVisible() || object.getInstance() != _activeObject.getInstance() || !Util.checkIfInShortRadius(getDistanceToForgetObject(object), _activeObject, object, true))
				removeKnownObject(object);
		}
	}
	
	/**
	 * Remove all objects from known list.
	 */
	public void removeAllKnownObjects()
	{
		_knownObjects.clear();
	}
	
	/**
	 * Current known list size, used for diagnostics and adaptive ranges.
	 * @return int : Known objects count.
	 */
	public final int getKnownObjectsCount()
	{
		return _knownObjects.size();
	}
	
	/**
	 * Check if object is in known list.
	 * @param object : {@link L2Object} to be checked.
	 * @return boolean : True, when object is in known list.
	 */
	public final boolean knowsObject(L2Object object)
	{
		if (object == null)
			return false;
		
		return _activeObject == object || _knownObjects.containsKey(object.getObjectId());
	}
	
	/**
	 * Return the known list.
	 * @return Collection<L2Object> : The known list.
	 */
	public final Collection<L2Object> getKnownObjects()
	{
		return _knownObjects.values();
	}
	
	/**
	 * Return the known list of given object type.
	 * @param <A> : Object type must be instance of {@link L2Object}.
	 * @param type : Class specifying object type.
	 * @return List<A> : Known list of given object type.
	 */
	@SuppressWarnings("unchecked")
	public final <A> List<A> getKnownType(Class<A> type)
	{
		List<A> result = new ArrayList<>();
		
		for (L2Object obj : _knownObjects.values())
		{
			if (obj == null || !obj.isVisible())
				continue;
			
			if (type.isAssignableFrom(obj.getClass()))
				result.add((A) obj);
		}
		
		return result;
	}
	
	/**
	 * Return the known list of given object type within specified radius.
	 * @param <A> : Object type must be instance of {@link L2Object}.
	 * @param type : Class specifying object type.
	 * @param radius : Radius to in which object must be located.
	 * @return List<A> : Known list of given object type.
	 */
	@SuppressWarnings("unchecked")
	public final <A> List<A> getKnownTypeInRadius(Class<A> type, int radius)
	{
		List<A> result = new ArrayList<>();
		
		for (L2Object obj : _knownObjects.values())
		{
			if (obj == null || !obj.isVisible())
				continue;
			
			if (type.isAssignableFrom(obj.getClass()) && Util.checkIfInRange(radius, _activeObject, obj, true))
				result.add((A) obj);
		}
		
		return result;
	}
	
	/**
	 * Returns the distance to watch object, aka distance to add object to known list.<br>
	 * <b>Is overridden by children in most cases.</b>
	 * @param object : {@link L2Object} to be checked.
	 * @return int : Distance.
	 */
	public int getDistanceToWatchObject(L2Object object)
	{
		return 0;
	}
	
	/**
	 * Returns the distance to forget object, aka distance to remove object from known list.<br>
	 * <b>Is overridden by children in most cases.</b>
	 * @param object : {@link L2Object} to be checked.
	 * @return int : Distance.
	 */
	public int getDistanceToForgetObject(L2Object object)
	{
		return 0;
	}
}

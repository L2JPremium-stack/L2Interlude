package net.sf.l2j.gameserver.model.base;

import net.sf.l2j.Config;

/**
 * Character Sub-Class Definition <BR>
 * Used to store key information about a character's sub-class.
 * @author Tempy
 */
public final class SubClass
{
	private ClassId _class;
	private final int _classIndex;
//	private long _exp;
	private long _exp = Experience.LEVEL[Config.CUSTOM_SUBCLASS_LVL];
	private int _sp;
//	private byte _level;
	private byte _level = (byte)Config.CUSTOM_SUBCLASS_LVL;
	
	/**
	 * Implicit constructor with all parameters to be set.
	 * @param classId : Class ID of the subclass.
	 * @param classIndex : Class index of the subclass.
	 * @param exp : Exp of the subclass.
	 * @param sp : Sp of the subclass.
	 * @param level : Level of the subclass.
	 */
	public SubClass(int classId, int classIndex, long exp, int sp, byte level)
	{
		_class = ClassId.VALUES[classId];
		_classIndex = classIndex;
		_exp = exp;
		_sp = sp;
		_level = level;
	}
	
	/**
	 * Implicit constructor with default EXP, SP and level parameters.
	 * @param classId : Class ID of the subclass.
	 * @param classIndex : Class index of the subclass.
	 */
	public SubClass(int classId, int classIndex)
	{
		_class = ClassId.VALUES[classId];
		_classIndex = classIndex;
	//	_exp = Experience.LEVEL[40];
		_exp = Experience.LEVEL[Config.CUSTOM_SUBCLASS_LVL];
		_sp = 0;
	//	_level = 40;
		_level = (byte) Config.CUSTOM_SUBCLASS_LVL;
	}
	
	public ClassId getClassDefinition()
	{
		return _class;
	}
	
	public int getClassId()
	{
		return _class.getId();
	}
	
	public void setClassId(int classId)
	{
		_class = ClassId.VALUES[classId];
	}
	
	public int getClassIndex()
	{
		return _classIndex;
	}
	
	public long getExp()
	{
		return _exp;
	}
	
	public void setExp(long exp)
	{
		if (exp > Experience.LEVEL[Experience.MAX_LEVEL])
			exp = Experience.LEVEL[Experience.MAX_LEVEL];
		
		_exp = exp;
	}
	
	public int getSp()
	{
		return _sp;
	}
	
	public void setSp(int sp)
	{
		_sp = sp;
	}
	
	public byte getLevel()
	{
		return _level;
	}
	
/*	public void setLevel(byte level)
	{
		if (level > (Experience.MAX_LEVEL - 1))
			level = (Experience.MAX_LEVEL - 1);
		else if (level < 40)
			level = 40;
		
		_level = level;
	}*/
	public void setLevel(byte levelValue)
	{
		if (levelValue > (Experience.MAX_LEVEL - 1))
			levelValue = (Experience.MAX_LEVEL - 1);
		else if (levelValue < Config.CUSTOM_SUBCLASS_LVL)
			levelValue = (byte)Config.CUSTOM_SUBCLASS_LVL;
		
		_level = levelValue;
	}
	public void incLevel()
	{
		if (getLevel() == (Experience.MAX_LEVEL - 1))
			return;
		
		_level++;
		setExp(Experience.LEVEL[getLevel()]);
	}
	
	public void decLevel()
	{
		if (getLevel() == Config.CUSTOM_SUBCLASS_LVL)
			return;
		
		_level--;
		setExp(Experience.LEVEL[getLevel()]);
	}
}
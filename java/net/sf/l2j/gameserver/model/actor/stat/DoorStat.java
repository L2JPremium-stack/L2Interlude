package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.gameserver.instancemanager.SevenSigns;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;

public class DoorStat extends CharStat
{
	private int _upgradeHpRatio;
	
	public DoorStat(L2DoorInstance activeChar)
	{
		super(activeChar);
		
		_upgradeHpRatio = 1;
	}
	
	@Override
	public L2DoorInstance getActiveChar()
	{
		return (L2DoorInstance) super.getActiveChar();
	}
	
	@Override
	public int getMDef(Creature target, L2Skill skill)
	{
		double defense = getActiveChar().getTemplate().getBaseMDef();
		
		final int sealOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE);
		if (sealOwner == SevenSigns.CABAL_DAWN)
			defense *= 1.2;
		else if (sealOwner == SevenSigns.CABAL_DUSK)
			defense *= 0.3;
		
		return (int) defense;
	}
	
	@Override
	public int getPDef(Creature target)
	{
		double defense = getActiveChar().getTemplate().getBasePDef();
		
		final int sealOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE);
		if (sealOwner == SevenSigns.CABAL_DAWN)
			defense *= 1.2;
		else if (sealOwner == SevenSigns.CABAL_DUSK)
			defense *= 0.3;
		
		return (int) defense;
	}
	
	@Override
	public int getMaxHp()
	{
		return super.getMaxHp() * _upgradeHpRatio;
	}
	
	public final void setUpgradeHpRatio(int hpRatio)
	{
		_upgradeHpRatio = hpRatio;
	}
	
	public final int getUpgradeHpRatio()
	{
		return _upgradeHpRatio;
	}
}
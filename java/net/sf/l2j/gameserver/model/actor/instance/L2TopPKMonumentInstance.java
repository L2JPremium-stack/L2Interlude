package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.instancemanager.custom.CharacterKillingManager;
import net.sf.l2j.gameserver.model.actor.PcPolymorph;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;

/**
 * @author Christian
 *
 */
public class L2TopPKMonumentInstance extends PcPolymorph
{
	public L2TopPKMonumentInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		if (Config.CKM_ENABLED)
		{
			CharacterKillingManager.getInstance().addPKMorphListener(this);
		}
	}
	
	@Override
	public void deleteMe()
	{
		super.deleteMe();
		if (Config.CKM_ENABLED)
		{
			CharacterKillingManager.getInstance().removePKMorphListener(this);
		}
	}
}
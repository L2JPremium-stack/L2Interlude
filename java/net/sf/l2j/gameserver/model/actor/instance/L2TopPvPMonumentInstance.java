package net.sf.l2j.gameserver.model.actor.instance;

/**
 * @author Christian
 *
 */
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.instancemanager.custom.CharacterKillingManager;
import net.sf.l2j.gameserver.model.actor.PcPolymorph;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;

public class L2TopPvPMonumentInstance extends PcPolymorph
{
	public L2TopPvPMonumentInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		if (Config.CKM_ENABLED)
		{
			CharacterKillingManager.getInstance().addPvPMorphListener(this);
		}
	}
	
	@Override
	public void deleteMe()
	{
		super.deleteMe();
		if (Config.CKM_ENABLED)
		{
			CharacterKillingManager.getInstance().removePvPMorphListener(this);
		}
	}
}
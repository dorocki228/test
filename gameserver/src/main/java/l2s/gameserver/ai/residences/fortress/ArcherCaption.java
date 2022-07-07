package l2s.gameserver.ai.residences.fortress;

import l2s.gameserver.ai.residences.SiegeGuardRanger;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.utils.Functions;

/**
 * @author VISTALL
 * @date 16:39/17.04.2011
 */
public class ArcherCaption extends SiegeGuardRanger
{
	public ArcherCaption(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public void onEvtDead(Creature killer)
	{
		Functions.npcShout(getActor(), NpcString.YOU_MAY_HAVE_BROKEN_OUR_ARROWS_BUT_YOU_WILL_NEVER_BREAK_OUR_WILL_ARCHERS_RETREAT);

		super.onEvtDead(killer);
	}
}

package l2s.gameserver.ai.residences.fortress;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.utils.Functions;

/**
 * @author VISTALL
 * @date 16:43/17.04.2011
 */
public class GuardCaption extends GuardFighter
{
	public GuardCaption(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public void onEvtDead(Creature killer)
	{
		Functions.npcShout(getActor(), NpcString.AIIEEEE_COMMAND_CENTER_THIS_IS_GUARD_UNIT_WE_NEED_BACKUP_RIGHT_AWAY);

		super.onEvtDead(killer);
	}
}

package l2s.gameserver.ai.residences.fortress;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.utils.Functions;

/**
 * @author VISTALL
 * @date 16:41/17.04.2011
 */
public class SupportUnitCaption extends GuardMystic
{
	public SupportUnitCaption(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		super.onEvtAttacked(attacker, skill, damage);

		if(Rnd.chance(1))
			Functions.npcShout(getActor(), NpcString.SPIRIT_OF_FIRE_UNLEASH_YOUR_POWER_BURN_THE_ENEMY);
	}

	@Override
	public void onEvtDead(Creature killer)
	{
		Functions.npcShout(getActor(), NpcString.AT_LAST_THE_MAGIC_FIELD_THAT_PROTECTS_THE_FORTRESS_HAS_WEAKENED_VOLUNTEERS_STAND_BACK);
		super.onEvtDead(killer);
	}
}

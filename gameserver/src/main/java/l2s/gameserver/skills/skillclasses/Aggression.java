package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.StatsSet;

public class Aggression extends Skill
{
	private final boolean _unaggring;
	private final boolean _silent;

	public Aggression(StatsSet set)
	{
		super(set);
		_unaggring = set.getBool("unaggroing", false);
		_silent = set.getBool("silent", false);
	}

	@Override
	protected void useSkill(Creature activeChar, Creature target, boolean reflected)
	{
		if(!target.isAutoAttackable(activeChar))
			return;

		double effect = getPower() != 0 ? getPower() : getEffectPoint();

		if(isSSPossible() && isMagic())
			effect = (int) (effect * ((100.0 + activeChar.getChargedSpiritshotPower()) / 100.0));

		if(target.isNpc())
		{
			if(_unaggring && target.isNpc() && activeChar.isPlayable())
				target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, -effect);
			else
			{
				target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, effect);

				if(!_silent)
					target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar, null, 2);

			}
		}
		else if(target.isPlayable() && !target.isDebuffImmune())
			target.setTarget(activeChar);
	}
}

package l2s.gameserver.skills.effects;

import l2s.gameserver.listener.actor.player.OnPlayerSummonServitorListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.stats.funcs.FuncTemplate;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectServitorShare extends Abnormal
{
	private final OnPlayerSummonServitorListener _listener;

	public EffectServitorShare(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_listener = new OnPlayerSummonServitorListenerImpl();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		getEffected().addListener(_listener);
		for(Servitor servitor : getEffected().getServitors())
			_listener.onSummonServitor(null, servitor);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		getEffected().removeListener(_listener);
		for(Servitor servitor : getEffected().getServitors())
		{
			servitor.removeStatsOwner(this);
			servitor.updateStats();
		}
	}

	@Override
	public Func[] getStatFuncs()
	{
		return Func.EMPTY_FUNC_ARRAY;
	}

	@Override
	public int getDisplayId()
	{
		if(_effected.isSummon())
			return -1;
		return super.getDisplayId();
	}

	public static class FuncShare extends Func
	{
		public FuncShare(Stats stat, int order, Object owner, double value)
		{
			super(stat, order, owner, value);
		}

		@Override
		public double calc(Creature creature, Creature target, Skill skill, double value)
		{
			double val = 0.0;
			switch(stat)
			{
				case MAX_HP:
				{
					val = creature.getPlayer().getMaxHp();
					break;
				}
				case MAX_MP:
				{
					val = creature.getPlayer().getMaxMp();
					break;
				}
				case POWER_ATTACK:
				{
					val = creature.getPlayer().getPAtk(null);
					break;
				}
				case MAGIC_ATTACK:
				{
					val = creature.getPlayer().getMAtk(null, null);
					break;
				}
				case POWER_DEFENCE:
				{
					val = creature.getPlayer().getPDef(null);
					break;
				}
				case MAGIC_DEFENCE:
				{
					val = creature.getPlayer().getMDef(null, null);
					break;
				}
				case POWER_ATTACK_SPEED:
				{
					val = creature.getPlayer().getPAtkSpd();
					break;
				}
				case MAGIC_ATTACK_SPEED:
				{
					val = creature.getPlayer().getMAtkSpd();
					break;
				}
				case BASE_P_CRITICAL_RATE:
				{
					val = creature.getPlayer().getPCriticalHit(null);
					break;
				}
				case BASE_M_CRITICAL_RATE:
				{
					val = creature.getPlayer().getMCriticalHit(null, null);
					break;
				}
				default:
				{
					val = creature.getPlayer().calcStat(stat, stat.getInit());
					break;
				}
			}
			return value + val * this.value;
		}
	}

	private class OnPlayerSummonServitorListenerImpl implements OnPlayerSummonServitorListener
	{
		@Override
		public void onSummonServitor(Player player, Servitor servitor)
		{
			FuncTemplate[] funcTemplates = getTemplate().getAttachedFuncs();
			Func[] funcs = new Func[funcTemplates.length];
			for(int i = 0; i < funcs.length; ++i)
				funcs[i] = new FuncShare(funcTemplates[i]._stat, funcTemplates[i]._order, EffectServitorShare.this, funcTemplates[i]._value);
			servitor.addStatFuncs(funcs);
			servitor.updateStats();
		}
	}
}

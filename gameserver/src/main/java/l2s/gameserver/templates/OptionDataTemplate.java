package l2s.gameserver.templates;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author VISTALL
 * @date 19:17/19.05.2011
 */
public class OptionDataTemplate extends StatTemplate
{
	private final List<SkillEntry> _skills = new ArrayList<SkillEntry>(0);
	private final List<EffectHandler> effects = new ArrayList<>(0);
	private final int _id;

	public OptionDataTemplate(int id)
	{
		_id = id;
	}

	public int getId()
	{
		return _id;
	}

	public void addSkill(SkillEntry skill)
	{
		_skills.add(skill);
	}

	public List<SkillEntry> getSkills()
	{
		return _skills;
	}

	public void addEffect(EffectHandler effect) {
		effects.add(effect);
	}

	public List<EffectHandler> getEffects() {
		return effects;
	}

	public boolean hasEffects()
	{
		return !effects.isEmpty();
	}

	public void apply(Player player) {
		//player.sendDebugMessage("Activating option id: " + _id, DebugType.OPTIONS);
		if (hasEffects())
		{
			final Abnormal info = new Abnormal(player, player, this, EffectUseType.NORMAL, false, false);
			AtomicBoolean soulShotUsed = new AtomicBoolean();
			for (EffectHandler effect : effects)
			{
				if (effect.calcSuccess(info.getEffector(), info.getEffected(), info.getSkill()))
				{
					effect.instantUse(info.getEffector(), info.getEffected(), soulShotUsed, false, null);
					//player.sendDebugMessage("Appling instant effect: " + effect.getClass().getSimpleName(), DebugType.OPTIONS);
					if (effect.checkPumpConditionImpl(info, info.getEffector(), info.getEffected()))
					{
						info.addEffect(effect);
						//player.sendDebugMessage("Appling continious effect: " + effect.getClass().getSimpleName(), DebugType.OPTIONS);
					}
				}
			}
			if (!info.getEffects().isEmpty())
			{
				player.getAbnormalList().add(info);
			}
		}
		/*if (hasActiveSkills())
		{
			for (SkillHolder holder : getActiveSkills())
			{
				addSkill(player, holder.getSkill());
				player.sendDebugMessage("Adding active skill: " + getActiveSkills(), DebugType.OPTIONS);
			}
		}
		if (hasPassiveSkills())
		{
			for (SkillHolder holder : getPassiveSkills())
			{
				addSkill(player, holder.getSkill());
				player.sendDebugMessage("Adding passive skill: " + getPassiveSkills(), DebugType.OPTIONS);
			}
		}
		if (hasActivationSkills())
		{
			for (OptionsSkillHolder holder : _activationSkills)
			{
				player.addTriggerSkill(holder);
				player.sendDebugMessage("Adding trigger skill: " + holder, DebugType.OPTIONS);
			}
		}

		player.sendSkillList();*/
	}

	public void remove(Player player) {
		//player.sendDebugMessage("Deactivating option id: " + _id, DebugType.OPTIONS);
		if (hasEffects())
		{
			for (Abnormal info : player.getAbnormalList())
			{
				OptionDataTemplate option = info.getOption();
				if (option == this)
				{
					//player.sendDebugMessage("Removing effects: " + info.getEffects(), DebugType.OPTIONS);
					player.getAbnormalList().remove(info);
				}
			}
		}
		/*if (hasActiveSkills())
		{
			for (SkillHolder holder : getActiveSkills())
			{
				player.removeSkill(holder.getSkill(), false, false);
				player.sendDebugMessage("Removing active skill: " + getActiveSkills(), DebugType.OPTIONS);
			}
		}
		if (hasPassiveSkills())
		{
			for (SkillHolder holder : getPassiveSkills())
			{
				player.removeSkill(holder.getSkill(), false, true);
				player.sendDebugMessage("Removing passive skill: " + getPassiveSkills(), DebugType.OPTIONS);
			}
		}
		if (hasActivationSkills())
		{
			for (OptionsSkillHolder holder : _activationSkills)
			{
				player.removeTriggerSkill(holder);
				player.sendDebugMessage("Removing trigger skill: " + holder, DebugType.OPTIONS);
			}
		}

		player.getStat().recalculateStats(true);
		player.sendSkillList();*/
	}
}

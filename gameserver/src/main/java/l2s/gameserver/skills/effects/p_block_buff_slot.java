package l2s.gameserver.skills.effects;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.AbnormalType;
import l2s.gameserver.templates.skill.EffectTemplate;

public class p_block_buff_slot extends Abnormal
{
	private final TIntSet _blockedAbnormalTypes;

	public p_block_buff_slot(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_blockedAbnormalTypes = new TIntHashSet();
		String[] split;
		String[] types = split = template.getParam().getString("abnormal_types", "").split(";");
		for(String type : split)
			_blockedAbnormalTypes.add(AbnormalType.valueOf(type).ordinal());
	}

	@Override
	public boolean checkBlockedAbnormalType(AbnormalType abnormal)
	{
		return !_blockedAbnormalTypes.isEmpty() && _blockedAbnormalTypes.contains(abnormal.ordinal());
	}
}

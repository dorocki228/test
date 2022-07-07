package l2s.gameserver.skills.effects;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo.MagicAttackType;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.skill.EffectTemplate;

public class i_spoil extends i_abstract_effect
{
	public i_spoil(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean checkCondition()
	{
		return getEffector().isPlayer() && !getEffected().isDead() && getEffected().isMonster() && super.checkCondition();
	}

	@Override
	public void instantUse()
	{
		MonsterInstance monster = (MonsterInstance) getEffected();
		if(monster.isSpoiled())
		{
			getEffector().sendPacket(SystemMsg.IT_HAS_ALREADY_BEEN_SPOILED);
			return;
		}
		Player player = getEffector().getPlayer();
		int monsterLevel = monster.getLevel();
		int modifier = Math.abs(monsterLevel - player.getLevel());
		double rateOfSpoil = Config.BASE_SPOIL_RATE;
		if(modifier > 8)
			rateOfSpoil -= rateOfSpoil * (modifier - 8) * 9.0 / 100.0;
		rateOfSpoil = rateOfSpoil * getSkill().getMagicLevel() / monsterLevel;
		if(rateOfSpoil < Config.MINIMUM_SPOIL_RATE)
			rateOfSpoil = Config.MINIMUM_SPOIL_RATE;
		else if(rateOfSpoil > 99.0)
			rateOfSpoil = 99.0;
		if(player.isGM())
			player.sendMessage(new CustomMessage("l2s.gameserver.skills.skillclasses.Spoil.Chance").addNumber((long) rateOfSpoil));
		doSpoil(Rnd.chance(rateOfSpoil));
	}

	protected void doSpoil(boolean success)
	{
		if(success)
		{
			((MonsterInstance) getEffected()).setSpoiled(getEffector().getPlayer());
			getEffector().sendPacket(SystemMsg.THE_SPOIL_CONDITION_HAS_BEEN_ACTIVATED);
		}
		else
		{
			ExMagicAttackInfo.packet(getEffector(), getEffected(), MagicAttackType.RESISTED);

			getEffector().sendPacket(new SystemMessagePacket(SystemMsg.S1_HAS_FAILED).addSkillName(getSkill()));
		}
	}
}

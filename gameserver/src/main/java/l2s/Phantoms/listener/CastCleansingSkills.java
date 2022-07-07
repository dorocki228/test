package  l2s.Phantoms.listener;


import  l2s.Phantoms.ai.abstracts.PhantomDefaultPartyAI;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.model.Skill;
import  l2s.gameserver.model.Skill.SkillType;

public class CastCleansingSkills implements PhantomAddSkillEffectListener
{
	@Override
	public void addSkillEffect(Player player, Creature attacker, Skill skill)
	{
		if(attacker.isSiegeGuard())
			return;
		
		player.phantom_params.getPhantomAI().castCleansingSkills(player, skill.getId());
		// если фантом в группе оповещаем группу о дебафе
		PhantomDefaultPartyAI party_ai = player.phantom_params.getPhantomPartyAI();
		if (party_ai != null)
		{
			if (skill.getSkillType() == SkillType.BUFF)
				return;
			party_ai.onPartyMemberAttacked(player, attacker);
			if (skill.getSkillType() == SkillType.DEBUFF)
			party_ai.onPartyMemberDebuffed(player, skill);
		}
		
	}
}

package  l2s.Phantoms.listener;


import  l2s.gameserver.listener.PlayerListener;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.model.Skill;

@FunctionalInterface
public interface PhantomAddSkillEffectListener extends PlayerListener
{
	void addSkillEffect(Player player, Creature attacker, Skill skill);
}

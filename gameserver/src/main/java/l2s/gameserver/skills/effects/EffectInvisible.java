package l2s.gameserver.skills.effects;

import l2s.gameserver.model.*;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

import java.util.List;

public final class EffectInvisible extends Abnormal
{
	public EffectInvisible(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(_effected.isPlayer())
		{
			Player player = _effected.getPlayer();
			if(player.getActiveWeaponFlagAttachment() != null)
				return false;
		}
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(!_effected.isPlayer())
			return;
		Player player = _effected.getPlayer();
		player.addInvisibleEffect(this);
		World.removeObjectFromPlayers(player);
		for(Servitor servitor : player.getServitors())
			World.removeObjectFromPlayers(servitor);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(_effected.isPlayer())
		{
			Player player = _effected.getPlayer();
			if(!player.isInvisible(null))
				return;
			player.removeInvisibleEffect(this);
			player.sendUserInfo(true);
			List<Player> players = World.getAroundObservers(player);
			for(Player p : players)
				p.sendPacket(p.addVisibleObject(player, null));
			for(Servitor servitor : player.getServitors())
			{
				servitor.getAbnormalList().stopEffects(getSkill());
				for(Player p2 : players)
					p2.sendPacket(p2.addVisibleObject(servitor, null));
			}
		}
		else if(_effected.isServitor())
			_effected.getPlayer().getAbnormalList().stopEffects(getSkill());
	}
}

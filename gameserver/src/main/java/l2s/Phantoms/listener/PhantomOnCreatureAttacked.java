package  l2s.Phantoms.listener;


import java.util.List;
import java.util.stream.Collectors;

import  l2s.Phantoms.Utils.PhantomUtils;
import  l2s.gameserver.listener.actor.OnCurrentHpDamageListener;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.model.Skill;

public class PhantomOnCreatureAttacked implements OnCurrentHpDamageListener
{

	@Override
	public void onCurrentHpDamage(Creature actor, double damage, Creature attacker, Skill skill,boolean sharedDamage)
	{
		Player player = attacker.getPlayer();
		Player phantom = actor.getPlayer();
		if (phantom != null && player != null && phantom.isPhantom() && player.isPlayer())
		{	
			// атаковал игрок - оповестим фантомов союзников
			notifyFriends(player, phantom);

			// не трогать пати
			if (phantom.getParty() != null && phantom.getParty() == player.getParty())
				return;
			// не трогать клан
			if (phantom.getClan() != null && phantom.getClan() == player.getClan())
				return;
			
			Creature lock_target = phantom.phantom_params.getLockedTarget();
			// таргент моб - меняем цель на атакующего игрока
			if (lock_target != null && lock_target.isMonster())
				phantom.getPlayer().phantom_params.setLockedTarget(player);
		}
	}
	
	private void notifyFriends(Player attacker, Player phantom)
	{
		if (System.currentTimeMillis()-phantom.phantom_params.getLastClanNotifyTime() > phantom.phantom_params.getMinClanNotifyInterval())
		{
			phantom.phantom_params.setLastClanNotifyTime(System.currentTimeMillis());
			
			List <Player> members = phantom.getAroundPlayers(1500, 500).stream().filter(d->d != null && d.isPhantom() &&d.getFraction() == phantom.getFraction() && PhantomUtils.availabilityCheck(d, attacker.getLoc())).collect(Collectors.toList());
			for(Player member : members)
			{
				if (member.phantom_params.getLockedTarget() != attacker)
					member.phantom_params.setNextLockedTarget(attacker);
			}
			
		}
	}

}
package  l2s.Phantoms.listener;


import  l2s.Phantoms.ai.abstracts.PhantomDefaultPartyAI;
import  l2s.gameserver.listener.actor.OnAttackHitListener;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;

public class PhantomAttacked implements OnAttackHitListener
{
	@Override
	public void onAttackHit(Creature actor, Creature attacker)
	{
		// TODO дописать другие типы фантомов
		if (actor.isPlayer() && actor.isPhantom())
		{
			Player phantom = actor.getPlayer();
			switch (phantom.getPhantomType())
			{
				case PHANTOM:
				case PHANTOM_HARD:
				{
					if (phantom.getParty()!=null && attacker.isPlayer() && attacker.getPlayer().getParty() == phantom.getParty()) // не атакуем пати
						return;
					if (phantom.getClan()!=null && attacker.isPlayer() && attacker.getPlayer().getClan() == phantom.getClan()) // не атакуем клан
						return;
					phantom.phantom_params.setSubTarget(attacker);
				}
					break;
				case PHANTOM_BOT_HUNTER:
				case PHANTOM_PARTY:
				case PHANTOM_CLAN_MEMBER:
				{
					// если фантом в группе оповещаем группу о атаке
					PhantomDefaultPartyAI party_ai = phantom.phantom_params.getPhantomPartyAI();
					if (party_ai != null)
						party_ai.onPartyMemberAttacked(phantom, attacker);
				}
					break;
				default:
					break;
				
			}
			
		}
	}
}

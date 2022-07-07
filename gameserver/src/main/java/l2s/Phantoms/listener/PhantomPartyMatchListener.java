package  l2s.Phantoms.listener;


import  l2s.gameserver.listener.PlayerListener;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;

@FunctionalInterface
public interface PhantomPartyMatchListener extends PlayerListener
{
	void groupEntry(Player player, Creature attacker);
}

package  l2s.Phantoms.listener;


import  l2s.gameserver.listener.PlayerListener;
import  l2s.gameserver.model.Player;

@FunctionalInterface
public interface StopEffectListener extends PlayerListener
{
	void stopEffect(Player player, int skill);
}

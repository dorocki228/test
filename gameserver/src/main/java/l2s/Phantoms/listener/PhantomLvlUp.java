package  l2s.Phantoms.listener;


import  l2s.Phantoms.ai.tasks.other.ChangeClothesAndProfession;
import  l2s.Phantoms.enums.PhantomType;
import  l2s.commons.util.Rnd;
import l2s.gameserver.listener.actor.player.OnLevelChangeListener;
import  l2s.gameserver.model.Player;

public class PhantomLvlUp implements OnLevelChangeListener
{
	@Override
	public void onLevelChange(Player player, int oldLvl, int newLvl)
	{
		if (!player.isPhantom() || player.getPhantomType() == PhantomType.PHANTOM_BOT_HUNTER || player.getPhantomType() == PhantomType.PHANTOM_INSTANCES)
			return;
		
		switch (player.getLevel())
		{
			case 10:
			case 20:
			case 40:
			case 52:
			case 61:
			case 76:
			case 80:
			case 84:
				if (player.phantom_params.getСhangeClothesTask()==null)
					player.phantom_params.startСhangeClothesTask(Rnd.get(5000, 15000), new ChangeClothesAndProfession(player));
		}
		
	}


}

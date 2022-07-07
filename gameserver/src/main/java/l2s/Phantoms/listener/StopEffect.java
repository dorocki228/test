package  l2s.Phantoms.listener;

import  l2s.gameserver.Config;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Race;

public class StopEffect implements StopEffectListener
{
	@Override
	public void stopEffect(Player player, int skill)
	{
		if (player.isPhantom() && !player.phantom_params.isNeedRebuff())
		{
			if (player.phantom_params.getClassAI().getBuffList().size() > 1) // баф с аи
			{
				for (Skill buff : player.phantom_params.getClassAI().getBuffList())
				{
					if (buff.getId() == skill)
					{
						player.phantom_params.setNeedRebuff(true);
						continue;
					}
				}
			}
			else // альтернативный баф с конфига
			{
				// баф воинов
				if (!player.getClassId().isMage() || player.getRace() == Race.ORC)
				{
					for (int[] buff : Config.PHANTOM_TOP_BUFF)
					{
						if (buff[0] == skill)
						{
							player.phantom_params.setNeedRebuff(true);
							continue;
						}
					}
				}
				// баф магов
				if (player.getClassId().isMage() && player.getRace() !=  Race.ORC)
				{
					for (int[] buff : Config.PHANTOM_TOP_BUFF_MAGE)
					{
						if (buff[0] == skill)
						{
							player.phantom_params.setNeedRebuff(true);
							continue;
						}
					}
				}
			}
		}
	}

}

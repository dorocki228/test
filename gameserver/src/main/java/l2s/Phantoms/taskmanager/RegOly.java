package  l2s.Phantoms.taskmanager;


import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import  l2s.Phantoms.enums.PhantomType;
import  l2s.commons.threading.RunnableImpl;
import  l2s.commons.util.Rnd;
import  l2s.gameserver.model.GameObjectsStorage;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.model.entity.olympiad.CompType;
import  l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;

	public class RegOly extends RunnableImpl
	{
		@Override
		protected void runImpl() throws Exception
		{
		/*	int _PhantomsCount = 0;
			if (!Olympiad.isOlympiadEnd() && (!Olympiad.isOlympiadEnd() || Olympiad.inCompPeriod()))
			{
				List <Player> phantom85 = GameObjectsStorage.getPlayerStream().filter(d->d != null && d.isPhantom()&& d.getReflectionId() == 0 &&  !d.isInPvPEvent() && !d.isDead() && !d.isInOlympiadMode() && d.getOlympiadGame()==null && d.getPhantomType() !=PhantomType.PHANTOM_TOWNS_PEOPLE && Olympiad.getNoblePoints(d.getObjectId()) >= 4 && (d.getPhantomType() == PhantomType.PHANTOM_HARD || d.getPhantomType() == PhantomType.PHANTOM) && d.isNoble() && d.getLevel() == 85 && d.phantom_params.getPhantomPartyAI() == null && d.phantom_params.getTrafficScheme() == null).collect(Collectors.toList());
				if (phantom85 == null || phantom85.isEmpty())
					return;
				Collections.shuffle(phantom85);
				for(Player player : phantom85)
				{
					if (!player.isDead())
					{
						if (_PhantomsCount >= 10)
							continue;
						if (!player.isNoble())
						{
							player.setNoble(false);
							Olympiad.addNoble(player);
						}
						if (Olympiad.registerNoble(player, CompType.NON_CLASSED))
						{
							if (player.getPhantomType() == PhantomType.PHANTOM_HARD )
							{
								if (Olympiad.getNoblePoints(player.getObjectId()) < 10)
									Olympiad.manualSetNoblePoints(player.getObjectId(), Rnd.get(20, 30));
							}
							player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
							player.setCurrentCp(player.getMaxCp());
							
							player.broadcastPacket(new MagicSkillUse(player, player, 2036, 1, 0, 0));
							player.teleToClosestTown();
							_PhantomsCount++;
						}
					}
				}
			}*/
		}
	}
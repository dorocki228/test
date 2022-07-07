package l2s.Phantoms.manager;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.Phantoms.PhantomPlayers;
import l2s.Phantoms.objects.Clan.ConstantParty;
import l2s.Phantoms.objects.Clan.MemberCP;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.tables.GmListTable;
import l2s.gameserver.utils.Location;

public class OlympiadManager
{
	protected final Logger _log = LoggerFactory.getLogger(OlympiadManager.class);
	private static OlympiadManager _instance;
	protected ScheduledFuture<?> _RegOlyTask = null;

	public static OlympiadManager getInstance()
	{
		if(_instance == null)
			_instance = new OlympiadManager();
		return _instance;
	}

	public OlympiadManager()
	{
		LocalDateTime now = LocalDateTime.now();
		int hour = now.getHour();
		if(hour >= Config.ALT_OLY_START_TIME)
		{
			startRegOlympiadTask(1000, 3 * 60 * 1000);
			_log.info("Start Cp Olymp.");
		}
		else
		{
			LocalDateTime start_olymp = now.withHour(18).withMinute(0).withSecond(0);
			startRegOlympiadTask(start_olymp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - start_olymp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), 3 * 60 * 1000);
			_log.info("Scheduled start Cp Olymp " + start_olymp.toLocalDate());
		}
	}

	public class RegOly extends RunnableImpl
	{
		@Override
		protected void runImpl() throws Exception
		{
			/*			GmListTable.broadcastMessageToGMs("Reg oly");
						int _PhantomsCount = 0;
						if(!Olympiad.isOlympiadEnd() && (!Olympiad.isOlympiadEnd() || Olympiad.inCompPeriod()))
						{
							List<ConstantParty> all_available_cp = ClanManager.getInstance().getAllCP().stream().filter(cp -> cp != null && cp.checkTimeForOlympiad() && !PartyManager.getInstance().alreadySpawned(cp.getCpId())).collect(Collectors.toList());
							List<MemberCP> members = new ArrayList<MemberCP>();
							all_available_cp.forEach(cp -> members.addAll(cp.getMembers()));
							Collections.shuffle(members);
			
							for(MemberCP member : members)
							{
								if(_PhantomsCount >= 10)
									continue;
								Player phantom = GameObjectsStorage.getPlayer(member.getMemberName());
								if(phantom != null) // фантом в игре
								{
									if(!phantom.isDead() && phantom.isPhantom() && phantom.getReflectionId() == 0 && !phantom.isInPvPEvent() && !phantom.isInOlympiadMode() && Olympiad.getNoblePoints(phantom.getObjectId()) >= 4 && phantom.isNoble() && phantom.getLevel() >= 83)
									{
										if(reg(phantom))
											_PhantomsCount++;
									}
								}
								else // спавним
								{
									phantom = PhantomPlayers.getInstance().spawnClanMember(member);
									if(phantom != null)
									{
										NpcInstance npc = Rnd.get(GameObjectsStorage.getAllByNpcId(31688, false));//спавн возле менеджера
										phantom.spawnMe(Location.findPointToStay(npc, 20, 80));
			
										if(reg(phantom))
											_PhantomsCount++;
									}
								}
							}
						}*/
		}
	}

	/*	private boolean reg(Player player)
		{
			if(!player.isNoble())
			{
				player.setNoble(false);
				Olympiad.addNoble(player);
			}
			if(Olympiad.registerNoble(player, CompType.NON_CLASSED))
			{
				if(player.getPhantomType() == PhantomType.PHANTOM_HARD)
				{
					if(Olympiad.getNoblePoints(player.getObjectId()) < 10)
						Olympiad.manualSetNoblePoints(player.getObjectId(), Rnd.get(20, 30));
				}
				player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
				player.setCurrentCp(player.getMaxCp());
	
				player.broadcastPacket(new MagicSkillUse(player, player, 2036, 1, 0, 0));
				player.teleToClosestTown();
				return true;
			}
			return false;
		}*/

	public void startRegOlympiadTask(long initDelay, long delay)
	{
		try
		{
			abortRegOlympiadTask();
			_RegOlyTask = ThreadPoolManager.getInstance().PhantomAiScheduleAtFixedRate(new RegOly(), initDelay, delay);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void abortRegOlympiadTask()
	{
		if(_RegOlyTask != null)
		{
			_RegOlyTask.cancel(true);
			_RegOlyTask = null;
		}
	}

}

package l2s.Phantoms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gve.zones.GveZoneManager;
import gve.zones.model.GveZone;
import l2s.Phantoms.Utils.PhantomTradeUtils;
import l2s.Phantoms.objects.Nickname;
import l2s.Phantoms.Utils.PhantomUtils;
import l2s.Phantoms.ai.chat.ChatHelper;
import l2s.Phantoms.ai.tasks.other.DespawnTask;
import l2s.Phantoms.dao.PhantomsDAO;
import l2s.Phantoms.enums.PhantomType;
import l2s.Phantoms.handler.AdminPhantom;
import l2s.Phantoms.handler.AdminPhantomHzEditor;
import l2s.Phantoms.manager.OlympiadManager;
import l2s.Phantoms.objects.GveZoneParam;
import l2s.Phantoms.objects.LocationPhantom;
import l2s.Phantoms.objects.TrafficScheme.PhantomRoute;
import l2s.Phantoms.parsers.LocationForCraftOrTradeHolder;
import l2s.Phantoms.parsers.LocationForCraftOrTradeParser;
import l2s.Phantoms.parsers.PhantomParser;
import l2s.Phantoms.parsers.PhantomRouteParser;
import l2s.Phantoms.parsers.Clan.PhantomClanParser;
import l2s.Phantoms.parsers.Craft.CraftPhantom;
import l2s.Phantoms.parsers.Craft.ItemsForCraftHolder;
import l2s.Phantoms.parsers.Craft.ItemsForCraftParser;
import l2s.Phantoms.parsers.HuntingZone.HuntingZoneParser;
import l2s.Phantoms.parsers.Items.PhantomAccessoryParser;
import l2s.Phantoms.parsers.Items.PhantomArmorParser;
import l2s.Phantoms.parsers.Items.PhantomJewelParser;
import l2s.Phantoms.parsers.Items.PhantomUnderwearParser;
import l2s.Phantoms.parsers.Items.PhantomWeaponParser;
import l2s.Phantoms.parsers.Nickname.NicknameParser;
import l2s.Phantoms.parsers.Trade.ItemsInfoParser;
import l2s.Phantoms.parsers.Trade.TradePhantom;
import l2s.Phantoms.parsers.ai.ClassAIParser;
import l2s.Phantoms.taskmanager.RegOly;
import l2s.Phantoms.templates.PhantomItem;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.ZoneHolder;
import l2s.gameserver.handler.admincommands.AdminCommandHandler;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.service.LocationBalancerService;
import l2s.gameserver.utils.Location;

/**
 * @author 4ipolino
 */
public class PhantomPlayers
{
	protected final Logger _log = LoggerFactory.getLogger(PhantomPlayers.class);
	protected ScheduledFuture<?> _SpawnTask = null;
	protected ScheduledFuture<?> _RegOlyTask = null;

	private Set<String> _route = new HashSet<String>();
	private boolean _shutdown = false;

	private static PhantomPlayers _instance;

	public static PhantomPlayers getInstance()
	{
		return _instance;
	}

	public Set<String> getRoute()
	{
		return _route;
	}

	public void addRoute(String string)
	{
		_route.add(string);
	}

	public boolean removeRoute(String name)
	{
		if(_route == null || _route.isEmpty())
			return false;
		return _route.remove(name);
	}

	public static void init()
	{
		if(_instance == null)
			_instance = new PhantomPlayers();
		AdminCommandHandler.getInstance().registerAdminCommandHandler(new AdminPhantom()); // админ команды
		AdminCommandHandler.getInstance().registerAdminCommandHandler(new AdminPhantomHzEditor());
		_instance.load();
	}

	public void load()
	{
		_log.info("*************[ Phantom Players System: Loading... ]***************");
		ChatHelper.getInstance().load();
		PhantomRouteParser.getInstance();
		NicknameParser.getInstance().loadNickAndTitle();
		NicknameParser.getInstance().createRecords();
		PhantomParser.getInstance();
		PhantomClanParser.getInstance().reload();
		//HuntingZoneParser.getInstance().reload();
		HuntingZoneParser.getInstance().load();
		PhantomAccessoryParser.getInstance().reload();
		PhantomArmorParser.getInstance().reload();
		PhantomJewelParser.getInstance().reload();
		PhantomWeaponParser.getInstance().reload();
		PhantomUnderwearParser.getInstance().reload();
		LocationForCraftOrTradeParser.getInstance().reload();
		ItemsInfoParser.getInstance().reload();
		ItemsForCraftParser.getInstance().reload();
		ClassAIParser.getInstance().allParse();

		if(!NicknameParser.getInstance().getNicknames().isEmpty())
		{
			if (Config.PHANTOM_SPAWN_ENABLED)
			{
				ThreadPoolManager.getInstance().PhantomSchedule(new CheckCount(), Config.PHANTOM_PLAYERS_DELAY_CHECK_COUNT);
				//if (PhantomUtils.IsActive("PhantomSpawnCraftOrTrade"))
					//FantomSpawnTraderTask();

				//startRegOlympiadTask(120000);
				startDespawnTask(5 * 60 * 1000);
			}
		}
		PhantomsDAO.getInstance().cleaningPhantomClan();
		OlympiadManager.getInstance();
		_log.info("*************[ Phantom Players System: loaded. ]***************");
	}

	// XXX создание рандомного бота
	public Player createPhantom(Nickname nickname, int _level, PhantomType _type, int PhantomClass, GveZone zone)
	{
		if(nickname == null)
		{
			_log.info("nickname == null");
			return null;
		}
		 GveZoneParam param = PhantomUtils.getLocType(zone);
		 
		PhantomType type = _type;

		long exp_add = (long) 0;
		if(_level == -1) /* устанавливаем уровень согласно классу */
			exp_add = Experience.LEVEL[Rnd.get(param.getMinLvl(),param.getMaxLvl())];
		else
			exp_add = Experience.LEVEL[_level];

		// ресторим фантома
		Player fantom = Player.restorePhantom(nickname.getName(), PhantomClass, exp_add);
		if(fantom == null)
		{
			//_log.info("restorePhantom == null " + nickname.getName());
			return null;
		}
		fantom.setPhantomType(type);
		fantom.setOfflineMode(false);
		fantom.setOnlineStatus(true);

		fantom.setCurrentHpMp(fantom.getMaxHp(), fantom.getMaxMp());
		fantom.setCurrentCp(fantom.getMaxCp());

		fantom.phantom_params.setComebackDistance(8000);
		fantom.setNetConnection(null);

		if(!PhantomUtils.initializePhantom(fantom, PhantomClass,zone)) // скилы\аи\шмотки
		{
			fantom.kick();
			_log.info("initializePhantom  false");
			return null;
		}

		switch(type)
		{
		case PHANTOM_CRAFTER:
		{
			int key = ItemsForCraftHolder.getInstance().getRndKey(); // выбираем рандомный список рецов
			if(key == -1)
			{
				fantom.kick();
				_log.info("CRAFTER key=-1");
				return null;
			}
			CraftPhantom im = ItemsForCraftHolder.getInstance().getItem(key); // грузим список
			if(im == null)
			{
				fantom.kick();
				_log.info("CraftPhantom = null");
				return null;
			}
			// локация спавна
			LocationPhantom loc = LocationForCraftOrTradeHolder.getInstance().getLocationCraft(im.getLocation());

			if(loc.getFraction() != Fraction.NONE)
				fantom.setFraction(loc.getFraction());

			Location _loc = PhantomUtils.getFreePoint(loc.getLocation());
			fantom.setXYZ(_loc.getX(), _loc.getY(), _loc.getZ());
			fantom.setHeading(Rnd.get(65536));

			fantom.spawnMe(_loc); // спавним фантома в мир
			ItemsForCraftHolder.getInstance().DeleteItem(key); // удалим список итемов с общего листа
			fantom.broadcastCharInfo();
			return fantom;
		}
		case PHANTOM_TRADER:
		{
			TradePhantom im2 = PhantomTradeUtils.generateTradeList(1, 4, 1, 0);
			if(im2 == null)
			{
				fantom.kick();
				_log.info("TradePhantom = null");
				return null;
			}
			// локация спавна
			LocationPhantom loc = LocationForCraftOrTradeHolder.getInstance().getLocationCraft(im2.getLocation());

			if(loc.getFraction() != Fraction.NONE)
				fantom.setFraction(loc.getFraction());
			Location _loc = PhantomUtils.getFreePoint(loc.getLocation());

			fantom.setXYZ(_loc.getX(), _loc.getY(), _loc.getZ());
			fantom.setHeading(Rnd.get(65536));

			if(!PhantomTradeUtils.TradeBuySellPhantom(fantom, im2))
			{
				fantom.kick();
				_log.info("TradeBuySellPhantom = null");
				return null;
			}
			fantom.spawnMe(_loc); // спавним фантома в мир
			//ItemsForSaleBuyHolder.getInstance().DeleteItem(key); // удалим список итемов с общего листа
			fantom.broadcastCharInfo();

			fantom.phantom_params.setPhantomMerchantsAI();
			if(fantom.phantom_params.getPhantomAI() == null)
			{
				fantom.kick();
				_log.info("getPhantomAI() = null");
				return null;
			}
			fantom.phantom_params.getPhantomAI().startAITask(Rnd.get(1, 3) * Rnd.get(30, 70) * 1000); // 1 минута
			return fantom;
		}
		case PHANTOM_HARD:
		case PHANTOM:
		{
			fantom.setPvpKills(Rnd.get(1,60));

			fantom.phantom_params.setPhantomAI();
			if(fantom.phantom_params.getPhantomAI() == null)
			{
				fantom.kick();
				_log.info("case PHANTOM_HARD: getPhantomAI() = null");
				return null;
			}
			Location loc = null;

			// локация спавна
			List<Location> listloc = new ArrayList<Location>();
			

				// возможный респ возле флагов
				/*	GveOutpost tmp = tmp_zone.getRandomOutpost(fantom.getFraction());
					if (tmp.getStatus() != GveOutpost.DEAD)
						for (Location loc : tmp.getLocations())
							listloc.add(Location.findAroundPosition(loc, 350, fantom.getGeoIndex()));*/
				Location loc_in_zone = zone.getZone().getTerritory().getRandomLoc(0);

				// 10 точек с зоны с проверкой на игроков
				List<MonsterInstance> monsters = World.getAroundMonsters(loc_in_zone);
				listloc.addAll(monsters.stream().filter(m-> World.getAroundRealPlayers(m.getLoc(), 4000, 600).size()==0 && World.getAroundPhantom(m.getLoc(), 1800,200).size() == 0).limit(10).map(l-> l.getLoc()).collect(Collectors.toList()));

			
			List<Location> listloc2 = listloc.stream().filter(l -> l != null && checkZone(l)).collect(Collectors.toList());
			if (listloc2.size() == 0)
			{
				//_log.warn(fantom + " Race:" + fantom.getRace().name() + " level:" + fantom.getLevel() + " loc.size() == 0");
				fantom.kick();
				return null;
			}
			loc = Rnd.get(listloc2);
			if(loc == null)
			{
				//_log.warn(fantom + " Race:" + fantom.getRace().name() + " level:" + fantom.getLevel() + " Location = null");
				fantom.kick();
				return null;
			}
			//}
			fantom.setLoc(loc);
			fantom.setHeading(Rnd.get(1, 360));
			fantom.spawnMe(loc); // спавним фантома в мир
			break;
		}
		case PHANTOM_PARTY:
		case PHANTOM_BOT_HUNTER:
		{
			fantom.phantom_params.setPhantomAI();
			if(fantom.phantom_params.getPhantomAI() == null)
			{
				fantom.kick();
				_log.warn("PHANTOM_PARTY = null");
				return null;
			}
			break;
		}
		default:
			break;
		}

		fantom.entering = false; // без данного параметра будут косяки с отображением визуальных ефектов

		if(fantom.getPhantomType() == PhantomType.PHANTOM_TOWNS_PEOPLE)
			fantom.phantom_params.getPhantomAI().startAITask(250 + fantom.phantom_params.getRndDelayAi());
		else
			fantom.phantom_params.getPhantomAI().startAITask(Config.PHANTOM_AI_DELAY + fantom.phantom_params.getRndDelayAi());
		fantom.getListeners().onEnter();

		for(PhantomItem item : fantom.phantom_params.getClassAI().getItemUse().getAllItems())
			fantom.getInventory().addItem(item.getId(), 10);

		for(PhantomItem item : fantom.phantom_params.getClassAI().getResItem().getAllItems())
			fantom.getInventory().addItem(item.getId(), 10);

		fantom.phantom_params.setPeaceCooldown(Rnd.get(5, 30));
		fantom.setRunning();
		// откат инстов
		fantom.removeAllInstanceReuses();
		if(Rnd.chance(Config.ALLOW_PHANTOMS_PARTY_CHANCE))
			fantom.phantom_params.setPartyInvite(true);
		if(Rnd.chance(30))// установим "женский пол" в чатике
			fantom.phantom_params.setMale(false);
		if(Rnd.chance(Config.CHANCE_PHANTOM_PK))
			fantom.phantom_params.setPK(true);
		if(Rnd.chance(Config.CHANCE_PHANTOM_PVP))
			fantom.phantom_params.setPvP(true);

		fantom.setActive(); // активировать агр мобов
		fantom.broadcastCharInfo();
		fantom.phantom_params.setPauseBtarget(Rnd.get(40, 80));
		switch(type)
		{
		case PHANTOM:
		case PHANTOM_HARD:
		case PHANTOM_TOWNS_PEOPLE:
			fantom.phantom_params.getPhantomAI().startBuffTask(100);
			break;
		case PHANTOM_BOT_HUNTER:
		case PHANTOM_PARTY:
			fantom.phantom_params.getPhantomAI().startBuffTask(100);
			break;
		default:
			break;
		}
		fantom.calcLevelReward();
		fantom.calcItemReward();
		fantom.calcEnchantReward();
		fantom.calcSetReward();
		fantom.calcPvpReward();
		fantom.calcNobleReward();
		fantom.calcHeroReward();
		return fantom;
	}

	String[] peace_zone = {
			"oren_town_peace_p",
			"elf_town_peace_p",
			"[oren_town_peace1]",
			"[elf_town_peace1]",
			"[mdt_harbor_no_trade1]",
			"[momot_mdt_arena_peace]",
	"[MDT_no_summon]"};

	public boolean checkZone(Location loc)
	{
		for (String zone : peace_zone)
		{
			if (ZoneHolder.getInstance().getTemplate(zone)==null)
				return false;
			if (ZoneHolder.getInstance().getTemplate(zone).getTerritory().isInside(loc))
				return false;
		}
		return true;
	}

	public void FantomSpawnTraderTask()
	{
		new Thread(new Runnable(){
			@Override
			public void run()
			{
				int count = 0;
				while(count < PhantomVariables.getInt("PCount_merchants", 0))
				{
					Player fantom = null;

					if(ItemsForCraftHolder.getInstance().size() == 0)
						break;

					if(Rnd.get(100) < 20)
					{
						int crafter_class_id = Rnd.get(Config.Crafter);
						Nickname n = getRandomPhantomNext(crafter_class_id);
						fantom = createPhantom(n, -1, PhantomType.PHANTOM_CRAFTER, crafter_class_id, null);
					}
					else
					{
						int[] pclassArr = PhantomUtils.getPhantomClass(null);
						if(pclassArr != null && pclassArr.length > 0)
						{
							int pclass = Rnd.get(pclassArr);
							Nickname n = getRandomPhantomNext(pclass);
							fantom = createPhantom(n, -1, PhantomType.PHANTOM_TRADER, pclass, null);
						}
					}
					if(fantom == null)
						continue;
					count++;
				}
				_log.info("PhantomPlayers: spawned " + count + " crafter,trader phantom.");
			}

		}).start();
	}

	public class FantomTask implements Runnable
	{
		@Override
		public void run()
		{
			List<GveZone> lst = GveZoneManager.getInstance().getActiveZones().stream().filter(z->
					{
						if(PhantomUtils.IsActive("Status_" + z.getName()))
						{
							if(z.getZone().getInsidePhantoms().size() > PhantomVariables.getInt(z.getName(), 0))
								return false;
							return true;
						}
					return false;
					}
			).filter(z-> z.getZone().getType()!=ZoneType.gve_pvp && !LocationBalancerService.getInstance().isLocationLimit(z)).collect(Collectors.toList());

			if (lst==null || lst.size()==0)
				return;
			
			GveZone zone = Rnd.get(lst);

			for(int i = 1; i <= 10; i++)
			{

				int[] class_id = PhantomUtils.getPhantomClass(zone);
				if(class_id.length == 0)
					continue;

				int pclass = Rnd.get(class_id);
				Nickname nickName = getRandomPhantomNext(pclass);
				if(nickName != null)
				{
					createPhantom(nickName, -1, PhantomType.PHANTOM, pclass,zone);
				}
			}
		}

	}

	public void getZoneToSpawn()
	{
		GveZoneManager.getInstance().getZones().values().forEach(z->
		{
			
		});
	}
	public class CheckCount implements Runnable
	{
		@Override
		public void run()
		{
			if(_shutdown)
				return;
			int limit = PhantomUtils.getPhantomLimit();
			if(limit != 0)
			{
				List<Player> phantoms = GameObjectsStorage.getAllPhantoms();
				if(phantoms.size() < limit && PhantomUtils.IsActive("PhantomBasicSpawn"))
					startSpawnTask(Config.PHANTOM_PLAYERS_DELAY_SPAWN);
				if(phantoms.size() > limit || !PhantomUtils.IsActive("PhantomBasicSpawn"))
					abortSpawnTask();
			}
			ThreadPoolManager.getInstance().PhantomSchedule(new CheckCount(), Config.PHANTOM_PLAYERS_DELAY_CHECK_COUNT);
		}

	}

	public void startSpawnTask(long delay)
	{
		try
		{
			abortSpawnTask();
			_SpawnTask = ThreadPoolManager.getInstance().PhantomSpawnSchedeleAtFixedRate(new FantomTask(), delay, delay);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void abortSpawnTask()
	{
		if(_SpawnTask != null)
		{
			_SpawnTask.cancel(true);
			_SpawnTask = null;
		}
	}

	private ScheduledFuture<?> DespawnTask;

	public void startDespawnTask(long delay)
	{
		try
		{
			abortDespawnTask();
			DespawnTask = ThreadPoolManager.getInstance().PhantomOtherSchedeleAtFixedRate(new DespawnTask(), delay, delay);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void abortDespawnTask()
	{
		if(DespawnTask != null)
		{
			DespawnTask.cancel(true);
			DespawnTask = null;
		}
	}

	public void startRegOlympiadTask(long delay)
	{
		try
		{
			abortRegOlympiadTask();
			_RegOlyTask = ThreadPoolManager.getInstance().PhantomAiScheduleAtFixedRate(new RegOly(), delay, delay);
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

	// поиск свободного маршрута для бота
	public PhantomRoute getRandomTrafficScheme(Player phantom, PhantomRoute oldscheme)
	{
		List<PhantomRoute> scheme = PhantomRouteParser.getInstance().getAllPhantomSoloRoute();
		List<PhantomRoute> valid = scheme.stream().filter(s -> s != null&& checkZone(s.getPointsFirstTask().get(0).getLoc())/* && (s.getClassId() != 0 && phantom.getClassId().getId() == s.getClassId())*/ && !_route.contains(s.getName()) && (oldscheme != null ? !PhantomUtils.equals(s.getName(), oldscheme.getName()) : true) && (s.getLvl() > 0 ? s.getLevels().contains(phantom.getLevel()) : true)).collect(Collectors.toList());

		if(valid != null && !valid.isEmpty())
			return Rnd.get(valid);
		return null;
	}

	// поиск подходящего ника
	public Nickname getRandomPhantomNext(int class_id)
	{
		List<Nickname> keys = NicknameParser.getInstance().getNicknames(class_id).filter(k -> GameObjectsStorage.getPlayer(k.getName()) == null).collect(Collectors.toList());
		if(keys == null || keys.size() == 0)
		{
			_log.warn("not found nick " + class_id);
			return null;
		}
		return Rnd.get(keys);
	}

	private void disconnectAllPhantom()
	{
		for(Player player : GameObjectsStorage.getPlayers())
		{
			try
			{
				if(player.isPhantom())
					player.kick();
			}
			catch(Exception e)
			{
				System.out.println("Error while disconnecting: " + player + "!");
				e.printStackTrace();
			}
		}
	}

	public void shutdown()
	{
		_shutdown = true;
		abortSpawnTask();
		abortRegOlympiadTask();
		System.out.println("Disconnecting phantoms...");
		disconnectAllPhantom();

		PhantomRouteParser.getInstance().SavePhantomRoute();
		HuntingZoneParser.getInstance().Save();
	}

	/*	final int[] second_profession_support = new int[] { 52, 43, 30, 21, 34, 16, 17, 6, 5, 20, 33 };
		final int[] second_profession_dd = new int[] { 130, 128, 129, 127, 57, 55, 48, 46, 41, 40, 36, 37, 28, 27, 23, 24, 14, 13, 12, 9, 8, 2, 3 };

		public void PartyFormation1()
		{
			if(!PhantomUtils.IsActive("PhantomSpawnP2"))
				return;
			HuntingZone rnp_point = HuntingZoneHolder.getInstance().getRandomPartyPoint();
			if(rnp_point.getLvlMin() > 76)// затычка, проверим только 2 профы
				return;
			Location loc = rnp_point.getRandomPoint();
			if(loc == null)
				return;
			StatsSet stats = new StatsSet();
			stats.set("partyId", PartyManager.getInstance().getPartiesSize() + 1);
			stats.set("partyType", PartyType.dagger);
			stats.set("partyCooldown", 60);
			stats.set("regroupToLeaderChance", 1);
			stats.set("regroupToPlaceChance", 1);
			stats.set("randomMoveChance", 1);
			PhantomPartyObject party_ai = new PhantomWarriorPartyAI(stats);
			PartyManager.getInstance().addParties(party_ai);
			for(int w = 0; w < rnp_point.getMaxPartySize(); w++) // спавним игроков в пати
			{
				int rnd_class = 0;
				if(w == 0)
					rnd_class = Rnd.get(second_profession_support); // первым делом выбрать супорта или танка
				else
					rnd_class = Rnd.get(second_profession_dd); // дальше добиваем пати дд
				// Создаем фантома заного
				Player phantom = PhantomPlayers.getInstance().createPhantom(PhantomPlayers.getInstance().getRandomPhantomNext(rnd_class), rnp_point.getLvlMin(), PhantomType.PHANTOM_PARTY, rnd_class);
				if(phantom == null)
					continue;
				phantom.setXYZ(loc.x + (Rnd.chance(50) ? 1 : -1) * Rnd.get(50), loc.y + (Rnd.chance(50) ? 1 : -1) * Rnd.get(50), loc.z);
				if(party_ai.getPartyLeader() == null)
				{
					party_ai.setPartyLeader(phantom);
					party_ai.getPartyLeader().setParty(new Party(party_ai.getPartyLeader(), 1));
				}
				phantom.phantom_params.setPhantomPartyAI(party_ai); // добавляем фейку аи группы
				party_ai.addPartyMember(phantom);
				phantom.spawnMe(phantom.getLoc()); // спавним фантома в мир
			}
			// заключаем их всех в пати
			for(Player member : party_ai.getAllMembers())
			{
				if(member != party_ai.getPartyLeader())
					party_ai.getPartyLeader().getParty().addPartyMember(member);
				if(member.phantom_params.getPhantomAI().isHealer() || member.phantom_params.getPhantomAI().isSupport())
					member.phantom_params.setfollow(true);
				member.phantom_params.getPhantomAI().startAITask(500);
			}
			party_ai.selectAsister();
			party_ai.changePartyState(PartyState.battle);
			party_ai.startAITask(3000);
		}*/

}
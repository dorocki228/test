package l2s.Phantoms.ai.abstracts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.Phantoms.PhantomVariables;
import l2s.Phantoms.enums.PartyState;
import l2s.Phantoms.enums.PartyType;
import l2s.Phantoms.enums.PhantomType;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geodata.pathfind.PathFind;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.instances.GroupBossInstance;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.PortalInstance;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.tables.GmListTable;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2s.gameserver.utils.Location;

public abstract class PhantomAITask extends RunnableImpl
{
	protected final Logger _log = LoggerFactory.getLogger(PhantomAITask.class);
	public Player phantom;
	final int olympiad_arena_147[][] = {
			{ -87496, -253224, -3328 },
			{ -87512, -252488, -3328 },
			{ -88632, -252488, -3328 },
			{ -88632, -253208, -3328 } };
	final int olympiad_arena_148[][] = {
			{ -75976, -252056, -7744 },
			{ -75992, -252872, -7744 },
			{ -74920, -252856, -7744 },
			{ -74936, -252056, -7744 } };
	final int olympiad_arena_149[][] = {
			{ -87448, -238712, -8448 },
			{ -88744, -238696, -8448 },
			{ -88712, -239992, -8448 },
			{ -87448, -239992, -8448 } };
	final int olympiad_arena_150[][] = {
			{ -75720, -238616, -8200 },
			{ -75128, -238648, -8200 },
			{ -74872, -239448, -8200 },
			{ -75880, -239672, -8200 } };

	public PhantomAITask(Player ph)
	{
		phantom = ph;
	}

	//выполняется только когда есть цель
	public boolean doAction()
	{
		// Пробуем одеть снятое оружие
		//equipDisarmedWeapon();

		if(phantom.phantom_params.isNeedRebuff() && !phantom.isDead() && !phantom.isInSiegeZone() && !phantom.isInOlympiadMode() /*&& !phantom.isInPvPEvent()*/)
			phantom.phantom_params.getPhantomAI().startBuffTask(100);

		if(!phantom.isInPeaceZoneOld())
		{
			if(!phantom.isDead() && !phantom.isInCombat())
			{
				if(phantom.getAbnormalList().containsEffects(312))
					phantom.getAbnormalList().stopEffects(312);
				if(phantom.getAbnormalList().containsEffects(500))
					phantom.getAbnormalList().stopEffects(500);
			}
			if(phantom.getPhantomType() != PhantomType.PHANTOM_PARTY && phantom.getPhantomType() != PhantomType.PHANTOM_BOT_HUNTER && phantom.getPhantomType() != PhantomType.PHANTOM_CLAN_MEMBER)
				doOtherAction(); // прочие действия
		}

		return false;
	}

	public void getTarget()
	{
		if(phantom.isDead())
			return;
		if(phantom.phantom_params.getDelayNewTarget() > System.currentTimeMillis())
			return;
		switch(phantom.getPhantomType())
		{
			case PHANTOM_INSTANCES:
			{
				if(Rnd.chance(phantom.phantom_params.getPauseBtarget()))
					return;
				// если нужно брать таргет, то ищем его сначало рядом, потом все дальше
				for(int radius = 100; radius < phantom.phantom_params.getComebackDistance(); radius += 300)
				{
					if(getAndSetInstLockedTarget(radius))
						break;
				}
				if(phantom.phantom_params.getGmLog())
					GmListTable.broadcastToGMs(new SayPacket2(0, ChatType.HERO_VOICE, "sys:", phantom.getName() + "is_need_to_get_new_target "));
				//Creature target = phantom.phantom_params.getLockedTarget();
				//randomMove(target);
				break;
			}
			case PHANTOM:
			case PHANTOM_HARD:
			{
				if(Rnd.chance(phantom.phantom_params.getPauseBtarget()))
					return;
				if(phantom.phantom_params.getPhantomAI().isHealer())
				{
					getAndSetLockedTargetHealer();
					// Creature target = phantom.phantom_params.getLockedHealerTarget();
					// randomMove(target);
				}
				if(phantom.phantom_params.getLockedTarget() != null)
					return;
				if(phantom.getOlympiadGame() != null)
				{
					//getAndSetLockedTargetOlympiad();
				}
				else
				{
					// если нужно брать таргет, то ищем его сначало рядом, потом все дальше
					for(int radius = 100; radius < phantom.phantom_params.getComebackDistance(); radius += 300)
					{
						if(getAndSetLockedTarget(radius))
							break;
						if(phantom.phantom_params.getPhantomAI().getRouteTask() != null && radius > 2300)
						{
							phantom.phantom_params.changeState(PartyState.route);
							break;
						}
					}
				}
				if(phantom.phantom_params.getGmLog())
					GmListTable.broadcastToGMs(new SayPacket2(0, ChatType.HERO_VOICE, "sys:", phantom.getName() + " is_need_to_get_new_target " + phantom.phantom_params.getLockedTarget() + " HealerTarget: " + phantom.phantom_params.getLockedHealerTarget()));
				//Creature target = phantom.phantom_params.getLockedTarget();
				//randomMove(target);
				break;
			}
	
			case PHANTOM_PARTY:
			case PHANTOM_BOT_HUNTER:
			{
				if(Rnd.chance(phantom.phantom_params.getPauseBtarget()))
					return;
				if(phantom.phantom_params.getPhantomAI().isHealer())
				{
					getAndSetLockedTargetHealer();
					//Creature target = phantom.phantom_params.getLockedHealerTarget();
					//randomMove(target);
				}
				else
				{
					// проверяем кто задает асист
					if(phantom.phantom_params.getPhantomPartyAI().getPartyAssister() == phantom)
					{
						// если нужно брать таргет, то ищем его сначало рядом, потом все дальше
						for(int radius = 100; radius < 900; radius += 50)
						{
							if(getAndSetLockedTarget(radius))
								break;
						}
						Creature target = phantom.phantom_params.getLockedTarget();
						if(target != null)
							phantom.phantom_params.getPhantomPartyAI().setPartyTarget(target);

						if(phantom.phantom_params.getGmLog())
							GmListTable.broadcastToGMs(new SayPacket2(0, ChatType.HERO_VOICE, "sys:", phantom.getName() + "is_need_to_get_new_target "));
					}
				}
				break;
			}
			default:
				break;
		}
	}

	/*	private void randomMove(Creature target)
		{
			int min_distance = 300;
			int max_distance = 600;
			if (Rnd.get(1000000) <= Config.FAKE_WALK_CHANCE)
			{
				if ((target != null && target.isInRange(phantom.getLoc(), 600)) || target == null)
					randomMove(min_distance, max_distance);
			}
		}*/

	public boolean checkMassSpoil()
	{
		List<MonsterInstance> Monsters = phantom.getAroundMonsters(200, 200);
		int count = 0;
		for(Creature monster : Monsters)
		{
			if(!monster.isDead())
				count++;
		}
		Monsters.clear();
		if(count > 2)
			return true;
		return false;
	}

	public void randomMove(int min_range, int max_range)
	{
		if(phantom.isMoveBlocked() || phantom.isMoving())
			return;

		Location loc = new Location(phantom.getX() + (Rnd.chance(50) ? 1 : -1) * Rnd.get(min_range, max_range), phantom.getY() + (Rnd.chance(50) ? 1 : -1) * Rnd.get(min_range, max_range), phantom.getZ(), 0).correctGeoZ();
		if(Math.abs(loc.getZ() - phantom.getZ()) > 150)// проверка точки по высот
			return;
		if(GeoEngine.canMoveToCoord(phantom.getX(), phantom.getY(), phantom.getZ(), loc.x, loc.y, loc.z, phantom.getGeoIndex()))
			phantom.moveToLocation(loc, 0, true);
	}

	public boolean fallBack(int range)
	{
		if(phantom.isMoveBlocked() || phantom.isMoving())
			return false;
		Creature target = phantom.phantom_params.getLockedTarget();
		if(target == null || !target.isInRange(phantom.getLoc(), range)) // если условия минимального ренжа не
			// соблюдены - стоим на месте
			return false;
		if(target.isPlayer() && (phantom.getOlympiadGame() != null || phantom.getPhantomType() == PhantomType.PHANTOM_CLAN_MEMBER))
		{
			if(target.getCurrentHpPercents() > 10)
				if(phantom.getCurrentMpPercents() > 7 && Rnd.chance(80))
				{
					if(target.isMageClass() && target.getPlayer().getClassId().getId() != 115 && target.getPlayer().getClassId().getId() != 116)
						return false;
					if(target.getPlayer().getActiveWeaponInstance() != null && (target.getPlayer().getActiveWeaponInstance().getItemType() == WeaponType.BOW))
						return false;
				}
		}

		Location loc = getFallBackLoc(target, phantom).correctGeoZ();
		if(Math.abs(loc.getZ() - phantom.getZ()) > 150)// проверка точки по высот
			return false;
		if(target.isPlayer() && (phantom.getOlympiadGame() != null || phantom.getPhantomType() == PhantomType.PHANTOM_CLAN_MEMBER)) // TODO на оли проверим патчфиндом, тест
		{
			if(target.getCurrentHpPercents() < 7) // не бегать от лоу хп
				return false;

			if(phantom.getCurrentHpPercents() > 30 && Rnd.chance(80) && target != null)
			{
				if(target.isMageClass() && target.getPlayer().getClassId().getId() != 115 && target.getPlayer().getClassId().getId() != 116)
					return false;
				if(target.getPlayer().getActiveWeaponInstance() != null && (target.getPlayer().getActiveWeaponInstance().getItemType() == WeaponType.BOW))
					return false;
			}

			//List<Location> moveList = PathFind.findPath(phantom.getX(), phantom.getY(), phantom.getZ(), loc.x, loc.y, loc.z, true, phantom.getGeoIndex(),false, true);
			final PathFind moveList = new PathFind(phantom.getX(), phantom.getY(), phantom.getZ(), loc.x, loc.y, loc.z, true, phantom.getGeoIndex());
			/*if (moveList != null && moveList.size() > 0 && target.getDistance(loc) > 100)
			{
				phantom.moveToLocation(loc, 0, true);
				return;
			}*/
			if(moveList != null && moveList.getPath().size() > 0)
			{
				phantom.moveToLocation(loc, 0, true);
				return true;
			}
			else if(phantom.getOlympiadGame() != null)
			{
				if(!phantom.isMoving())
				{
					int[] loc_t;
					if(phantom.isInZone("[olympiad_arena_147]"))
					{
						loc_t = Rnd.get(olympiad_arena_147);
						phantom.moveToLocation(new Location(loc_t[0], loc_t[1], loc_t[0]), 0, true);
						return true;
					}
					if(phantom.isInZone("[olympiad_arena_148]"))
					{
						loc_t = Rnd.get(olympiad_arena_148);
						phantom.moveToLocation(new Location(loc_t[0], loc_t[1], loc_t[0]), 0, true);
						return true;
					}
					if(phantom.isInZone("[olympiad_arena_149]"))
					{
						loc_t = Rnd.get(olympiad_arena_149);
						phantom.moveToLocation(new Location(loc_t[0], loc_t[1], loc_t[0]), 0, true);
						return true;
					}
					if(phantom.isInZone("[olympiad_arena_150]"))
					{
						loc_t = Rnd.get(olympiad_arena_150);
						phantom.moveToLocation(new Location(loc_t[0], loc_t[1], loc_t[0]), 0, true);
						return true;
					}
				}
			}
			else
				return false;
		}
		else if(GeoEngine.canMoveToCoord(phantom.getX(), phantom.getY(), phantom.getZ(), loc.x, loc.y, loc.z, phantom.getGeoIndex()))
		{
			phantom.moveToLocation(loc, 0, true);
			return true;
		}
		return false;
	}

	public Location getFallBackLoc(Creature attacker, Player phantom)
	{
		int posX = phantom.getX();
		int posY = phantom.getY();
		int posZ = phantom.getZ();
		int signx = posX < attacker.getX() ? -1 : 1;
		int signy = posY < attacker.getY() ? -1 : 1;
		int range = (int) (0.71 * phantom.calculateAttackDelay() / 1000 * phantom.getMoveSpeed());
		posX += signx * range;
		posY += signy * range;
		posZ = GeoEngine.getHeight(posX, posY, posZ, phantom.getGeoIndex());
		return new Location(posX, posY, posZ, 0);
	}

	protected void doOtherAction()
	{
		// писдеж в чат
		if(Config.ALLOW_PHANTOM_CHAT)
		{
			if(Rnd.get(100000) < Config.PHANTOM_CHAT_CHANSE)
			{
				switch(Rnd.get(1, 2))
				{
					case 1:
						/*	Say2 cs = new SayPacket2(phantom.getObjectId(), ChatType.SHOUT, phantom.getName(), PhantomParser.getInstance().getRandomSayPhantoms());
							for (Player player : World.getAroundPlayers(phantom, 10000, 3000))
								if (player != null)
									if (!player.isBlockAll())
										player.sendPacket(cs);*/
						break;
					case 2:
						/*Say2 cs3 = new SayPacket2(phantom.getObjectId(), ChatType.ALL, phantom.getName(), PhantomParser.getInstance().getRandomSayPhantoms());
						for (Player player : World.getAroundPlayers(phantom, 1200, 1000))
							if (player != null)
								if (!player.isBlockAll())
									player.sendPacket(cs3);*/
						break;
				}
			}
		}
	}

	/*	protected void equipDisarmedWeapon()
		{
			if (phantom.getActiveWeaponInstance() == null && phantom.getAbnormalList().getEffectByStackType("disarm") == null)
			{
				ItemInstance weapon = phantom.getPhantomWeapon();
				if (weapon != null)
					phantom.getInventory().equipItem(weapon);
			}
		}*/

	/*
	 * Выбор таргента на ивентах
	 */
	public boolean getAndSetLockedTargetEvent(int radius)
	{
		try
		{
			if(phantom.phantom_params.getLockedTarget() != null)
				return true;
			List<Creature> targets = new ArrayList<Creature>();
			targets.clear();
			targets = phantom.getAroundCharacters(radius, 200);
			for(Creature target : targets) // Если кто-то нас уже держит в таргете, значит это будет цель номер 1, а
			// иначе идем дальше искать цель.
			{
				if(phantom.getParty() != null && target.isPlayer() && target.getPlayer().getParty() != null && target.getPlayer().getParty() == phantom.getParty())
					continue;
				if(target.getTarget() == phantom && target.getAggressionTarget() == phantom && !target.isDead() && !target.isFakeDeath() && !target.isMonster())
				{
					if(target.isDoor())
						continue;
					if(target.isInvisible() || target.isDead() || target.isFakeDeath())
						continue;
					if(!phantom.isInZoneBattle() && !target.isInZoneBattle())
						continue;
					if(!GeoEngine.canSeeTarget(phantom, target, false))
						continue;
					if(target.getTeam() == phantom.getTeam())
						continue;
					phantom.phantom_params.setLockedTarget(target);
					phantom.setTarget(target);
					return true;
				}
			}
			// В таргете нас нет, ищем на жопу приключения самостоятельно
			if(targets == null || targets.size() == 0)
				return false;
			Creature target = targets.get(Rnd.get(targets.size()));
			if(target == null)
				return false;
			if(phantom.getParty() != null && target.isPlayer() && target.getPlayer().getParty() != null && target.getPlayer().getParty() == phantom.getParty())
				return false;
			if(target.isDoor())
				return false;
			if(target.getTeam() == phantom.getTeam())
				return false;
			if(target.isInvisible() || target.isDead() || target.isFakeDeath() || target.isRaid() || target.isMinion())
				return false;
			if(target.isMonster())
				return false;
			if(!phantom.isInZoneBattle() && !target.isInZoneBattle())
				return false;
			if(!GeoEngine.canSeeTarget(phantom, target, false))
				return false;
			if(phantom.phantom_params.getGmLog())
				GmListTable.broadcastToGMs(new SayPacket2(0, ChatType.HERO_VOICE, "SYS:", phantom.getName() + "\n" + " LockedTargetEvent 5"));
			phantom.phantom_params.setLockedTarget(target);
			phantom.setTarget(target);
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	//TODO добавить игнорлист для нпс\мобов, вывести в конфиг
	private boolean ignoreCheck(Creature target)
	{
		if (target.isMonster()&& ((NpcInstance)target).getTemplate().getInstanceClass() == GroupBossInstance.class)
		return false;
		
		// проверим фантомов вокруг моба, взят ли он как цель другим ботом
		List<Player> player_list = target.getAroundPhantom(1500, 200).stream().filter(player -> player != null
				&& player != phantom 
				&& player.getFraction() == phantom.getFraction() // бот в нашей фракции
				&&  player.isMoving() //игрок движется 
				&& player.phantom_params.getLockedTarget() == target).collect(Collectors.toList());
		if(player_list.size() > 0)
			return false;

		// проверим фантомов вокруг актора, взят ли моб как цель другим ботом
		List<Player> player_list2 = phantom.getAroundPhantom(1500, 200).stream().filter(player -> player != null
				&& player != phantom 
				&& player.getFraction() == phantom.getFraction() // бот в нашей фракции
				&& player.isMoving() //игрок движется 
				&& player.phantom_params.getLockedTarget() == target).collect(Collectors.toList());
		if(player_list2.size() > 0)
			return false;

		return true;
	}

	private boolean CheckAttackMonster(Creature d)
	{
		if(((Creature) d).getAI() instanceof DefaultAI)
		{
			Creature _AttackTarget = ((DefaultAI) ((Creature) d).getAI()).getAttackTarget();
			if(_AttackTarget == null)
				return true;
			if(_AttackTarget == phantom || !_AttackTarget.isPhantom())
				return true;

		}
		return false;
	}

	public boolean getAndSetInstLockedTarget(int radius)
	{
		try
		{
			if(phantom.phantom_params.getLockedTarget() != null)
				return true;
			// InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(phantom.phantom_params.getIdZone());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public boolean getAndSetPartyLockedTarget(int radius, boolean is_move)
	{
		try
		{
			if(phantom.phantom_params.getLockedTarget() != null)
				return true;

			if(phantom.phantom_params.getPriorityTarget() != null && !phantom.phantom_params.getPriorityTarget().isDead())
			{
				phantom.phantom_params.setLockedTarget(phantom.phantom_params.getPriorityTarget());
				phantom.phantom_params.getPhantomPartyAI().changePartyState(PartyState.battle);
				return true;
			}
			if(is_move) // TODO добавить обработку пк
			{
				//	List<Player> Around_targets = phantom.getAroundPlayers(radius, 200).stream().filter(d -> d != null && !d.isDead() && !d.isInvisible()&& phantom.atMutualWarWith(d)).collect(Collectors.toList());
				List<Creature> Around_targets = phantom.getAroundCharacters(radius, 200).stream().filter(d -> d != null && !d.isDead() && !d.isInvisible() && !d.isDoor() && (
				// игроки
				(d.isPlayer() && phantom.atMutualWarWith(d.getPlayer()) && (phantom.getClan() == null ? true : phantom.getClan() != d.getPlayer().getClan()) // не бить свой клан
						&& (phantom.getAlliance() == null ? true : phantom.getAlliance() != d.getPlayer().getAlliance()) // не бить свой али
						&& (phantom.getParty() == null ? true : phantom.getParty() != d.getPlayer().getParty())) // не бить свою пати 
						//петы
						//	|| (d.isServitor() && !phantom.getSummonList().contains(d))

						|| (d.getNpcId() == 35680 && GeoEngine.canSeeTarget(phantom, d, false)) || (d.isSiegeGuard() && GeoEngine.canSeeTarget(phantom, d, false)))).collect(Collectors.toList());

				if(Around_targets == null || Around_targets.isEmpty())
					return false;

				Creature target = Rnd.get(Around_targets);
				if(target.isPhantom() && target.getPlayer().phantom_params.getPhantomPartyAI() != null)// пати бот 
					phantom.phantom_params.setLockedTarget(getWeakPlayerInParty(target.getPlayer().getParty(), phantom.phantom_params.getPhantomPartyAI().getPartyType()));//TODO реализовать пати игроков 
				else
					phantom.phantom_params.setLockedTarget(target);

				phantom.phantom_params.getPhantomPartyAI().changePartyState(PartyState.battle);
				return true;

			}
			else
			{
				List<Creature> Around_targets = new ArrayList<>(phantom.getAroundCharacters(radius, 300));
				List<Player> Around_players = new ArrayList<>(phantom.getAroundPlayers(950, 300));

				if(Around_players != null && Around_players.size() > 0)
					Around_targets.addAll(Around_players); // добавим в список игроков в радиусе

				if(Around_targets == null || Around_targets.size() <= 0)
					return false;

				List<Creature> Filteredtargets = Around_targets.stream().filter(d -> d != null && !d.isDead() && !d.isInvisible() && !d.isDoor() && !d.isRaid() && (
				// игроки
				(d.isPlayer() && (phantom.getClan() == null ? true : phantom.getClan() != d.getPlayer().getClan()) // не бить свой клан
						&& (phantom.getAlliance() == null ? true : phantom.getAlliance() != d.getPlayer().getAlliance()) // не бить свой али
						&& (phantom.getParty() == null ? true : phantom.getParty() != d.getPlayer().getParty())) // не бить свою пати 
						//петы
						//|| (d.isServitor() && !phantom.getSummonList().contains(d) )
						// мобы
						|| (d.isMonster() && ((MonsterInstance) d).isTargetable() && ignoreCheck(d)))).collect(Collectors.toList());

				for(Player member : phantom.phantom_params.getPhantomPartyAI().getAllMembers())
				{
					List<Creature> newTarget = new ArrayList<>();
					for(Creature enemy : Filteredtargets) // Если кто-то нас уже держит в таргете, значит это будет цель номер 1, а иначе идем дальше искать цель.
					{
						if(enemy.isSiegeGuard() && enemy.getDistance3D(phantom) > 250)
							continue;
						if(enemy.getAI().getAttackTarget() == member)
						{
							if(enemy.isPlayer() && enemy.getPlayer().getParty() != null) // игрок состоит в пати, выбрать лучший асист
							{
								phantom.phantom_params.setLockedTarget(getWeakPlayerInParty(enemy.getPlayer().getParty(), phantom.phantom_params.getPhantomPartyAI().getPartyType()));
								return true;
							}
							if(enemy.isPlayer()) // если игрок - атакуем 
							{
								phantom.phantom_params.setLockedTarget(enemy);
								return true;
							}
							newTarget.add(enemy);
						}
						if(newTarget != null && !newTarget.isEmpty()) // остальные цели
						{
							phantom.phantom_params.setLockedTarget(Rnd.get(newTarget));
							return true;
						}
					}
				}

				// В таргете нас нет, ищем на жопу приключения самостоятельно
				if(Filteredtargets == null || Filteredtargets.size() == 0)
					return false;
				Creature target = Filteredtargets.get(Rnd.get(Filteredtargets.size()));
				if(target == null)
					return false;
				if(phantom.isInZoneBattle() && target.isMonster())
					return false;

				if(phantom.phantom_params.getIgnoreList().containsKey(target.getObjectId()))
					if(phantom.phantom_params.getIgnoreList().get(target.getObjectId()) > System.currentTimeMillis())
						return false;

				if(phantom.isInZone("[phantoms_pvp_arena_1]") && target.isMonster())
					return false;
				int target_type = 0;
				// определяем тип таргета
				if(target.isPlayer())
					target_type = 1;
				if(target.isMonster())
					target_type = 2;
				if(target.isServitor())
					target_type = 3;
				switch(target_type)
				{
					case 1:
					{

						if(target.isInZoneBattle() || phantom.isInZone("[phantoms_pvp_arena_1]") || target.isInZone(ZoneType.SIEGE)) // Если в боевой зоне - бьем игроков.
						{
							if(phantom.getTeam() != TeamType.NONE && phantom.getTeam() == target.getTeam()) // Запрет на атаку своей команды, если это не Ласт Хиро
								return false;
							phantom.phantom_params.setLockedTarget(target);
							return true;
						}
						if(phantom.atMutualWarWith(target.getPlayer())) // убиваем вара без раздумий
						{
							phantom.phantom_params.setLockedTarget(target);
							return true;
						}
						//		if (target.isCursedWeaponEquipped()) // убиваем проклятое оружие
						//{
						//	phantom.phantom_params.setLockedTarget(target);
						//return true;
						//	}
						if(target.getKarma() > 0) // убиваем пк без раздумий
						{
							phantom.phantom_params.setLockedTarget(target);
							return true;
						}
						if(Rnd.chance(PhantomVariables.getFloat("ChanceTargetNoPvpFlag", 6)) && Config.EnablePlayerKiller && target.isAttackingNow()) // Шанс убить в ПК другого игрока/Ну или нарваться на пвп/ Ботов в ПК не льем
						{
							if(target.isPhantom() && !Config.EnablePhantomKiller)
								return false;

							if(!target.isPhantom())
							{
								if(Rnd.chance(0.01) && target.getLevel() > 61)
								{
									phantom.phantom_params.setLockedTarget(target);
									return true;
								}
								else
									return false;
							}

							phantom.phantom_params.setLockedTarget(target);
							return true;
						}
						if(target.getPvpFlag() != 0 && PhantomVariables.getBool("AttackPlayerPvpFlag", false)) // Если рядом пвп игрок - атакуем его c шансом сомнений
						{
							int chance = PhantomVariables.getInt("ChanceTargetPlayerPvpFlag", 0);
							if(Rnd.chance(80) && !target.isPhantom())
							{
								phantom.phantom_params.setLockedTarget(target);
								return true;
							}
							chance = PhantomVariables.getInt("ChanceTargetPhantomPvpFlag", 0);
							if(chance > 0 && Rnd.chance(chance) && target.isPhantom())
							{
								phantom.phantom_params.setLockedTarget(target);
								return true;
							}
						}
						return false;
					}
					case 2:
					{
						if(phantom.isInZoneBattle() && phantom.isInZone("[phantoms_pvp_arena_1]") && !target.isInZoneBattle())
							return false;
						if(target.isMinion())
						{
							final NpcInstance leader = ((MonsterInstance) target).getLeader();
							if(leader.isRaid())
								return false;
						}

						// if (PhantomUtils.availabilityCheck2(phantom, target, radius))
						// {
						phantom.phantom_params.setLockedTarget(target);
						return true;
						// }
						// else
						// return false;
					}
					case 3:
					{
						if(phantom.getParty() != null && target != null)
						{
							List<Playable> member = phantom.getParty().getPartyMembersWithPets();
							if(member != null && !member.isEmpty() && member.contains(target.getPlayer()))
								return false;
						}

						if(phantom.getClan() != null)
						{
							if(target.getPlayer().getClan() == phantom.getClan())
								return false;
						}
						if(target.getPvpFlag() == 0)
							return false;
						phantom.phantom_params.setLockedTarget(target);
						return true;
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public Player getWeakPlayerInParty(Party party, PartyType partyType)
	{
		if(party == null)
			return null;
		List<Player> party_member = party.getPartyMembers();
		Collections.sort(party_member, new TargerComporator(partyType));
		if(!party_member.isEmpty())
			return party_member.get(0);
		return null;
	}

	public class TargerComporator implements Comparator<Player>
	{
		PartyType _partyType;

		public TargerComporator(PartyType partyType)
		{
			_partyType = partyType;
		}

		@Override
		public int compare(Player o1, Player o2)
		{
			/*
			 * возвращает нулевое значение if (x == y), if (x <y), то возвращает значение меньше нуля, и если (x> y), то возвращается значение больше нуля.
			 */
			int result = Integer.compare(o2.getArmorType(), o1.getArmorType());
			if(result != 0)
				return result;
			if(_partyType == PartyType.mage) // если пати тип маг - сравниваем по мдефу
				return result = o1.getMDef(null, null) - o2.getMDef(null, null);
			return result = o1.getPDef(null) - o2.getPDef(null);
		}

	}

	private List<Creature> filterList(List<Creature> Around_targets)
	{
		return Around_targets.stream().filter(d -> d != null && !d.isDead() && !d.isInvisible()&& !d.getAbnormalList().containsEffects(922) && !d.isDoor() && !d.isRaid() && (phantom.phantom_params.getIgnoreList().containsKey(d.getObjectId()) ? phantom.phantom_params.getIgnoreList().get(d.getObjectId()) > System.currentTimeMillis() : true) &&
				// игроки
				((d.isPlayer() && !d.getPlayer().isGMInvisible() && (phantom.getClan() == null ? true : phantom.getClan() != d.getPlayer().getClan()) // не бить свой клан
						&& (phantom.getAlliance() == null ? true : phantom.getAlliance() != d.getPlayer().getAlliance()) // не бить свой али
						&& (phantom.getParty() == null ? true : phantom.getParty() != d.getPlayer().getParty())// не бить свою пати
						&& phantom.getFraction() != d.getFraction()) // своя фракция
						//|| (d.isServitor() && !phantom.getSummonList().contains(d))
						// мобы
						|| (d.isMonster() && ((MonsterInstance) d).isTargetable() && CheckAttackMonster(d) && ignoreCheck(d)))).collect(Collectors.toList());
	}
	public boolean getAndSetLockedTarget(int radius)
	{
		try
		{
			if(phantom.phantom_params.getLockedTarget() != null)
				return true;
			
			//if (phantom.getGveZones().size()==0)
				//return false;

			List<Creature> Around_targets = new ArrayList<>(phantom.getAroundMonsters(radius, 2000));
			List<Creature> Around_players = new ArrayList<>(phantom.getAroundPlayers(1000, 500));
			
			if(phantom.phantom_params.getNextLockedTarget()!=null)
			{
				Around_players.add(phantom.phantom_params.getNextLockedTarget());
				phantom.phantom_params.setNextLockedTarget(null);

			}
			
			if(phantom.phantom_params.getGmLog())
			{
				//GmListTable.broadcastMessageToGMs("isNeedRebuff: " + phantom.phantom_params.isNeedRebuff());
				GmListTable.broadcastMessageToGMs("Around_targets: " + Around_targets.size());
				GmListTable.broadcastMessageToGMs("Around_players: " + Around_players.size());
			}
			List<Creature> tmp = filterList(Around_players);
			
			
			if (tmp  != null && tmp.size() > 0)
			{
				Collections.sort(tmp, new Comparator<Creature>() {
					public int compare(Creature o1, Creature o2) {
						if(phantom.getDistance(o1) < phantom.getDistance(o2))
							return -1;
						else
							return 1;
					}
				});
				phantom.phantom_params.setLockedTarget(tmp.get(0));
				return true;
			}
			
			if(Around_players != null && Around_players.size() > 0)
				Around_targets.addAll(Around_players); // добавим в список игроков в радиусе

			if(Around_targets == null || Around_targets.size() <= 0)
				return false;
			List<Creature> Filteredtargets = Around_targets.stream().filter(d -> d != null && !d.isDead() && !d.isInvisible()&& !d.getAbnormalList().containsEffects(922) && !d.isDoor() && !d.isRaid() && (phantom.phantom_params.getIgnoreList().containsKey(d.getObjectId()) ? phantom.phantom_params.getIgnoreList().get(d.getObjectId()) > System.currentTimeMillis() : true) &&
			// игроки
					((d.isPlayer() && !d.getPlayer().isGMInvisible() && (phantom.getClan() == null ? true : phantom.getClan() != d.getPlayer().getClan()) // не бить свой клан
							&& (phantom.getAlliance() == null ? true : phantom.getAlliance() != d.getPlayer().getAlliance()) // не бить свой али
							&& (phantom.getParty() == null ? true : phantom.getParty() != d.getPlayer().getParty())// не бить свою пати
							&& phantom.getFraction() != d.getFraction()) // своя фракция
							//|| (d.isServitor() && !phantom.getSummonList().contains(d))
							// мобы
							|| (d.isMonster() && ((MonsterInstance) d).isTargetable() && CheckAttackMonster(d) && ignoreCheck(d)))).collect(Collectors.toList());

				List<Creature> newTarget = new ArrayList<>();
				for(Creature enemy : Filteredtargets) // Если кто-то нас уже держит в таргете, значит это будет цель номер 1, а иначе идем дальше искать цель.
				{
					if(enemy == null)
						continue;
					
					if (enemy.isPlayer()&& enemy.getFraction() != phantom.getFraction())
					{
						phantom.phantom_params.setLockedTarget(enemy);
						return true;
					}
					
					if (enemy.isMonster()&&enemy.getFraction() != Fraction.NONE&& enemy.getFraction() != phantom.getFraction())
					{
						phantom.phantom_params.setLockedTarget(enemy);
						return true;
					}
					
					if((enemy.getTarget() == phantom && enemy.isPhantom()) || (enemy.getAI() != null && enemy.getAI().getAttackTarget() == phantom) || (enemy.getAggressionTarget() == phantom))
					{
						if(enemy.isPlayer()) // если игрок - атакуем 
						{
							phantom.phantom_params.setLockedTarget(enemy);
							return true;
						}
						newTarget.add(enemy);
					}
				}
				if(newTarget != null && !newTarget.isEmpty())
				{
					phantom.phantom_params.setLockedTarget(Rnd.get(newTarget));
					return true;
				}
			
			// В таргете нас нет проверяем ребаф и ищем на жопу приключения самостоятельно
			if(Filteredtargets == null || Filteredtargets.size() == 0 || phantom.phantom_params.isNeedRebuff())
				return false;

			if(phantom.phantom_params.getGmLog())
			{
				GmListTable.broadcastMessageToGMs("Filteredtargets: " + Filteredtargets.size());
			}

			Creature target = Filteredtargets.get(Rnd.get(Filteredtargets.size()));
			if(target == null)
				return false;

			int target_type = 0;
			// определяем тип таргета
			if(target.isPlayer())
				target_type = 1;
			if(target.isMonster())
				target_type = 2;
			if(target.isServitor())
				target_type = 3;
			switch(target_type)
			{
				case 1:
				{

					if(target.isInZoneBattle() || phantom.isInZone("[phantoms_pvp_arena_1]") || target.isInZone(ZoneType.SIEGE)) // Если в боевой зоне - бьем игроков.
					{
						if(phantom.getTeam() != TeamType.NONE && phantom.getTeam() == target.getTeam()) // Запрет на атаку своей команды, если это не Ласт Хиро
							return false;
						phantom.phantom_params.setLockedTarget(target);
						return true;
					}
					if(phantom.getFraction() != target.getPlayer().getFraction()) // убиваем вара без раздумий
					{
						phantom.phantom_params.setLockedTarget(target);
						return true;
					}

					if(Rnd.chance(PhantomVariables.getFloat("ChanceTargetNoPvpFlag", 6)) && Config.EnablePlayerKiller && target.isAttackingNow()) // Шанс убить в ПК другого игрока/Ну или нарваться на пвп/ Ботов в ПК не льем
					{
						if(target.isPhantom() && !Config.EnablePhantomKiller)
							return false;

						if(!target.isPhantom())
						{
							if(Rnd.chance(PhantomVariables.getFloat("ChanceTargetPlayerNoPvpFlag", 0.1)) && target.getLevel() > 61)
							{
								phantom.phantom_params.setLockedTarget(target);
								return true;
							}
							else
								return false;
						}

						phantom.phantom_params.setLockedTarget(target);
						return true;
					}

					if(target.getPvpFlag() != 0 && PhantomVariables.getBool("AttackPlayerPvpFlag", false)) // Если рядом пвп игрок - атакуем его c шансом сомнений
					{
						int chance = PhantomVariables.getInt("ChanceTargetPlayerPvpFlag", 0);
						if(Rnd.chance(80) && !target.isPhantom())
						{
							phantom.phantom_params.setLockedTarget(target);
							return true;
						}
						chance = PhantomVariables.getInt("ChanceTargetPhantomPvpFlag", 0);
						if(chance > 0 && Rnd.chance(chance) && target.isPhantom())
						{
							phantom.phantom_params.setLockedTarget(target);
							return true;
						}
					}
					return false;
				}
				case 2:
				{
					if(phantom.phantom_params.getGmLog())
					{
						GmListTable.broadcastMessageToGMs("target mob : " + target);
					}
					if(target.isMinion())
					{
						final NpcInstance leader = ((MonsterInstance) target).getLeader();
						if(leader.isRaid() || leader.getTemplate().getInstanceClass() == GroupBossInstance.class)
							return false;
					}
					if(phantom.phantom_params.getGmLog())
					GmListTable.broadcastMessageToGMs("target mob : " + target + " true");
					phantom.phantom_params.setLockedTarget(target);
					return true;
				}
				case 3:
				{
					if(phantom.getParty() != null)
						if(phantom.getParty().getPartyMembersWithPets().contains(target))
							return false;
					if(phantom.getClan() != null)
					{
						if(target.getPlayer().getClan() == phantom.getClan())
							return false;
					}
					if(target.getPvpFlag() == 0)
						return false;
					phantom.phantom_params.setLockedTarget(target);
					return true;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public void getAndSetLockedTargetHealer()
	{
		Party p_ai = phantom.getParty();
		if(phantom.getParty() != null)
		{
			Player min = minimumLife(phantom, p_ai);
			if(min.getCurrentHpPercents() > 80)
				return;
			if(phantom.getDistance(min) > 700)
				return;
			phantom.phantom_params.setLockedHealerTarget(min);
		}
		else
			phantom.phantom_params.setLockedHealerTarget(phantom);
	}

	private Player minimumLife(Player phantom, Party p_ai)
	{
		Player min = phantom;
		for(Player member : p_ai.getPartyMembers())
		{
			if(min != null && member.getCurrentHpPercents() < min.getCurrentHpPercents() && member.getCurrentHpPercents() < 80 && phantom.getDistance(min) < 600)
				min = member;
		}
		return min;
	}

	public static void moveToCharacter(Player phantom, Creature target, int cast_dist)
	{
		if(phantom.isMoving() || Math.abs(phantom.getZ() - phantom.getImpliedTargetLoc(target).getZ()) > 150)
			return;
		double dist = phantom.getDistance(target); // дистанция до цели
		if(cast_dist > 0)
		{
			double diff = dist - cast_dist;
			double λ = 0;
			if(diff > 0)
			{
				if(diff > cast_dist)
				{
					λ = diff / cast_dist;
				}
				else
				{
					λ = cast_dist / diff;
				}
				double posX = (phantom.getX() + λ * target.getX()) / (1 + λ);
				double posY = (phantom.getY() + λ * target.getY()) / (1 + λ);
				phantom.moveToLocation(new Location((int) posX, (int) posY, phantom.getZ()), 0, true);
			}
		}
	}

	public static void moveToCharacterRnd(Creature phantom, Creature target, int cast_dist)
	{
		if(phantom.isMoving() || Math.abs(phantom.getZ() - phantom.getImpliedTargetLoc(target).getZ()) > 150)
			return;
		double dist = phantom.getDistance(target); // дистанция до цели
		if(cast_dist > 0)
		{
			double diff = dist - cast_dist;
			double λ = 0;
			if(diff > 0)
			{
				if(diff > cast_dist)
				{
					λ = diff / cast_dist;
				}
				else
				{
					λ = cast_dist / diff;
				}
				double posX = (phantom.getX() + λ * target.getX()) / (1 + λ);
				double posY = (phantom.getY() + λ * target.getY()) / (1 + λ);
				phantom.moveToLocation(new Location((int) posX + Rnd.get(10, 40), (int) posY + Rnd.get(10, 40), phantom.getZ()), 0, true);
			}
		}
	}
}

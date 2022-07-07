package  l2s.Phantoms.ai.abstracts;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import org.napile.pair.primitive.IntObjectPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import  l2s.Phantoms.Utils.PhantomUtils;
import l2s.Phantoms.ai.tasks.other.MoveToFreePointTask;
import  l2s.Phantoms.enums.Condition;
import l2s.Phantoms.enums.PartyState;
import  l2s.Phantoms.enums.PhantomType;
import  l2s.Phantoms.objects.PCondition;
import  l2s.Phantoms.objects.PhantomPartyObject;
import l2s.Phantoms.objects.TrafficScheme.Agation;
import l2s.Phantoms.objects.TrafficScheme.Pet;
import l2s.Phantoms.objects.TrafficScheme.PhantomRoute;
import l2s.Phantoms.objects.TrafficScheme.Point;
import l2s.Phantoms.objects.TrafficScheme.RouteTask;
import  l2s.Phantoms.parsers.HuntingZone.HuntingZoneHolder;
import  l2s.Phantoms.templates.ItemsGroup;
import  l2s.Phantoms.templates.PhantomItem;
import  l2s.Phantoms.templates.PhantomSkill;
import  l2s.Phantoms.templates.SkillsGroup;
import l2s.commons.threading.RunnableImpl;
import  l2s.commons.util.Rnd;
import  l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import  l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.ai.PlayableAI;
import l2s.gameserver.ai.PlayableAI.AINextAction;
import  l2s.gameserver.geodata.GeoEngine;
import  l2s.gameserver.geodata.GeoMove;
import  l2s.gameserver.handler.items.IItemHandler;
import  l2s.gameserver.listener.actor.player.OnAnswerListener;
import  l2s.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.GameObject;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.model.Skill;
import  l2s.gameserver.model.Skill.SkillTargetType;
import  l2s.gameserver.model.Skill.SkillType;
import  l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.creature.AbnormalList;
import  l2s.gameserver.model.instances.ChestInstance;
import  l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.instances.StaticObjectInstance;
import l2s.gameserver.model.instances.UpgradingArtifactInstance;
import  l2s.gameserver.model.items.ItemInstance;
import  l2s.gameserver.model.items.attachment.FlagItemAttachment;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.skills.skillclasses.Summon;
import  l2s.gameserver.stats.Stats;
import  l2s.gameserver.tables.GmListTable;
import l2s.gameserver.templates.skill.EffectTemplate;
import  l2s.gameserver.utils.Location;
import  l2s.gameserver.utils.PositionUtils;

public abstract class PhantomDefaultAI extends PhantomAbstractAI
{
	protected final Logger _log = LoggerFactory.getLogger(PhantomDefaultAI.class);

	public final int[] CHECK_BUFF_SUMMON = new int[]{1323,1182,304};
	public final int[] INVINCIBLE = new int[]{5739,5744,1540}; // умения неуязвимости
	public final int[] DEBUFF_RESIST = new int[]{5739,5744,1411,1540}; // целестиал, Мистический Иммунитет и тд

	private PhantomRoute _soloRoute = null;
	private RouteTask _routeTask = null;
	public int currentPointCounter = 0;
	private Point currentPointCoordinate = null;
	private long TimeoutAfterServerStartup = 0;

	private Player lock_target = null;
	private int TraderCount = 0;
	private int targetcount = 0;

	private int CountUseItems = 0;
	private int UseItemCount;
	private String[] UseItemId;
	private int DelayUseItemMin;
	private int DelayUseItemMax;

	private ScheduledFuture <?> _UseItemsTask;
	private ScheduledFuture <?> _MountPetTask;

	private boolean returningBack, superpointWait, checkTrade = false;
	public static Map <Integer,Integer> TraderDelayInSec = new HashMap <Integer,Integer>();

	public abstract void doCast();

	public RouteTask getRouteTask()
	{
		return _routeTask;
	}
	public void doBuffCast()
	{
		Player phantom = getActor();
		try
		{
			if (!phantom.isDead())
			{
				if (phantom.getPhantomType() == PhantomType.PHANTOM_CLAN_MEMBER && !phantom.isMoving())
				{
					PhantomPartyObject party_ai = phantom.phantom_params.getPhantomPartyAI();
					if (party_ai!=null && party_ai.getCurrentPointCoordinate() !=null && party_ai.getCurrentPointCoordinate().getLoc().distance(phantom.getLoc()) >= party_ai.getp4() && !party_ai.isMovingParty())
					{
						//if(!PhantomUtils.availabilityCheck(phantom, party_ai.getPartyAssister()))
						//if (Rnd.nextBoolean())
						//if(!phantom.moveToLocation(Location.coordsRandomize(Rnd.get(phantom.getParty().getMembers(phantom)).getLoc(), 10, party_ai.getp4()),0, true))
						//	phantom.moveToLocation(Location.coordsRandomize(phantom.getLoc(), 10, party_ai.getp4()), 0, true);
						//else
						//phantom.moveToLocation(Location.coordsRandomize(phantom.getLoc(), 10, party_ai.getp4()), 0, true);
						return;
					}
				}

				if(phantom.getOlympiadGame() == null&&phantom.phantom_params.getClassAI().getItemUse()!=null&&phantom.phantom_params.getClassAI().getItemUse().getAllItems().size()>0)				// Юзаем какой-нибудь итем
					CheckAndUseItem(phantom.phantom_params.getClassAI().getItemUse(), phantom);

				// бафаем какой-нибудь селф на себя
				castSelfBuffSkill();

				//TODO вынести к херам снимем "Тайною мудрость" 
				if (phantom.getCurrentHpPercents() < 15 && phantom.phantom_params.getPhantomAI().isHealer() && phantom.getAbnormalList().containsEffects(336))
					phantom.getAbnormalList().stopEffects(336);
			}

		}catch(NullPointerException e)
		{
			_log.error("PhantomAI : "+phantom.phantom_params.getPhantomAI());
			_log.error("PhantomClass : "+phantom.getActiveClassId());
			e.printStackTrace();
		}
	}

	public void setRoute(PhantomRoute soloRoute)
	{
		superpointWait = false;
		currentPointCounter = 0;
		currentPointCoordinate = null;
		TimeoutAfterServerStartup = 0;
		_soloRoute = soloRoute;
		if(soloRoute!=null)
			_routeTask = soloRoute.getTaskGroupId(0).get(0);
	}


	public void doRouteAction()
	{
		if (_soloRoute==null || _routeTask == null)
			return;

		if (getActor().phantom_params.getState()== PartyState.route && !getActor().isMoving()&& (currentPointCoordinate== null ? true: getActor().getDistance(currentPointCoordinate.getLoc()) < 30) && !getActor().isFollowing() && System.currentTimeMillis() > TimeoutAfterServerStartup)
		{
			currentPointCoordinate = _routeTask.getPoints().get(currentPointCounter);
			if (currentPointCoordinate == null)
				return;

			GmListTable.broadcastMessageToGMs(getActor().getName()+"_log:");
			GmListTable.broadcastMessageToGMs("currentPointCounter:"+currentPointCounter);

			if (currentPointCounter >= _routeTask.getPoints().size()-1)
			{
				if (getActor().phantom_params.getGmLog())
					GmListTable.broadcastMessageToGMs("Finish");

				/*getActor().stopMove();

						TrafficScheme oldscheme = getActor().phantom_params.getTrafficScheme();
						if (oldscheme == null)
						{
							getActor().kick();
							return;
						}
						getActor().phantom_params.getPhantomAI().abortAITask();

						if (Rnd.get(0, 100) < 70)
						{
							TrafficScheme scheme = PhantomPlayers.getInstance().getRandomTrafficScheme(getActor(), oldscheme);
							if (scheme == null)
							{
								getActor().kick();
								return;
							}
							getActor().phantom_params.setTrafficScheme(scheme, oldscheme, false);
							getActor().phantom_params.setgetActor()TownsAI();
							getActor().teleToLocation(scheme.getPointsRnd().get(0).getLoc());
							currentPointCounter = 0;
							getActor().phantom_params.getPhantomAI().startAITask(Config.getActor()_AI_DELAY);
							return;
						}
						else
						{
							getActor().phantom_params.setgetActor()AI();
							if (getActor().phantom_params.getgetActor()AI() == null)
							{
								getActor().kick();
								return;
							}
							getActor().phantom_params.setTrafficScheme(null, oldscheme, true);
							getActor()Utils.checkLevelAndSetFarmLoc(getActor(), getActor(), true);
							getActor().phantom_params.getPhantomAI().startAITask(Config.getActor()_AI_DELAY);
							return;
						}*/

			}
			else
			{
				if (getActor().phantom_params.getGmLog())
					GmListTable.broadcastMessageToGMs("loc:"+currentPointCoordinate.getLoc().toXYZString());
				if (!checkTrade)
					getActor().moveToLocation(currentPointCoordinate.getLoc(), 0, true);
			}

			if (currentPointCoordinate.getDelay() != null && currentPointCoordinate.getDelay().getSeconds() > 0 && !superpointWait && System.currentTimeMillis()-TimeoutAfterServerStartup > currentPointCoordinate.getDelay().getSeconds())
			{
				if (getActor().phantom_params.getGmLog())
					GmListTable.broadcastMessageToGMs("Point delay start :"+currentPointCoordinate.getDelay().getSeconds());

				TimeoutAfterServerStartup = System.currentTimeMillis()+(currentPointCoordinate.getDelay().getSeconds()*1000);
				superpointWait = true;
				return;

			}
			if (currentPointCoordinate.getTeleport() != null)
			{
				if (getActor().phantom_params.getGmLog())
					GmListTable.broadcastMessageToGMs("Teleport: "+currentPointCoordinate.getTeleport().toString());
				getActor().teleToLocation(currentPointCoordinate.getTeleport().getLoc());
			}

			if (currentPointCoordinate.getTarget() != null && currentPointCoordinate.getTarget().getNpcId() != 0)
			{
				if (getActor().phantom_params.getGmLog())
					GmListTable.broadcastMessageToGMs("Target: "+currentPointCoordinate.getTarget().getNpcId());

				List <NpcInstance> targets = getActor().getAroundNpc(700, 200).stream().filter(npc->npc != null && npc.getNpcId() == currentPointCoordinate.getTarget().getNpcId()).collect(Collectors.toList());

				if (targets != null && !targets.isEmpty())
				{
					getActor().setTarget(targets.get(0));
					getActor().getAI().Attack(targets.get(0), false, false);
					if (getActor().phantom_params.getGmLog())
						GmListTable.broadcastMessageToGMs("Target: "+currentPointCoordinate.getTarget().getNpcId()+" true");
				}
			}

			/******* агатион ездовой *********/
			if (currentPointCoordinate.getMount_agation() != null && currentPointCoordinate.getMount_agation().size() > 0)
			{
				Agation agation = Rnd.get(currentPointCoordinate.getMount_agation());
				if (agation.getAgationId() == 0 || agation.getAgationSkill() == 0)
					return;

				if (Rnd.get(100) < 30)
				{
					if (getActor().getInventory().getPaperdollItem(18) == null)
					{
						getActor().getInventory().addItem(agation.getAgationId(), 1);

						ItemInstance item = getActor().getInventory().getItemByItemId(agation.getAgationId());
						IItemHandler handler = item.getTemplate().getHandler();
						handler.useItem(getActor(), item, false);
						return;
					}
					Skill skill = getActor().getSkillById(agation.getAgationSkill());
					if (skill != null && Rnd.get(100) < 2)
					{
						getActor().getAI().Cast(skill, getActor(), true, true);
					}
				}
			}

			/******* пет ездовой *********/
			if (currentPointCoordinate.getSummonPet() != null && currentPointCoordinate.getSummonPet().size() != 0)
			{
				Pet item_id = Rnd.get(currentPointCoordinate.getSummonPet());
				if (getActor().getPet() == null)
				{
					if (Rnd.get(100) < 5)
					{
						getActor().getInventory().addItem(item_id.getPetId(), 1);
						ItemInstance item = getActor().getInventory().getItemByItemId(item_id.getPetId());
						IItemHandler handler = item.getTemplate().getHandler();
						handler.useItem(getActor(), item, false);
					}
					return;
				}
				else
				{
					if (item_id.getMount() && Rnd.get(100) < 2)
					{
						if (_MountPetTask != null)
						{
							_MountPetTask.cancel(false);
							_MountPetTask = null;
						}
						_MountPetTask = ThreadPoolManager.getInstance().PhantomOtherSchedule(new MountPetDelay(), Rnd.get(20*1000, 60*1000));
					}
				}

			}
			if (getActor().isMounted() && Rnd.get(100) < 2)
			{
				//getActor().dismount();
			}
			if (getActor().getPet() != null && !getActor().isMounted())
			{
				if (Rnd.get(100) < 2)
				{
					if (_MountPetTask != null)
					{
						_MountPetTask.cancel(false);
						_MountPetTask = null;
					}
					getActor().getPet().unSummon(false);
				}
			}
			/******* использование итемов *********/
			if (currentPointCoordinate.getUseitem() != null && !currentPointCoordinate.getUseitem().isEmpty() && currentPointCoordinate.getUseitem().size() > 0)
			{
				/*
				 * UseItemCount = currentPoint.getUseitem().getCount(); UseItemId = currentPoint.getUseitem().getItemId().split(";"); DelayUseItemMin = currentPoint.getUseitem().getDelayMin(); DelayUseItemMax = currentPoint.getUseitem().getDelayMax(); if (_UseItemsTask != null) { _UseItemsTask.cancel(false);
				 * _UseItemsTask = null; } _UseItemsTask = ThreadPoolManager.getInstance().getActor()OtherSchedule(new UseItemsWithDelay(), Rnd.get(DelayUseItemMin, DelayUseItemMax));
				 */
			}

			/******* беготня между трейдеров *********/
			if (currentPointCoordinate.getTrader() != null && currentPointCoordinate.getTrader().getCount() != 0)
			{
				if (TraderCount == 0)
				{
					checkTrade = true;
					TraderCount = currentPointCoordinate.getTrader().getRndCount();
				}

				if (!TraderDelayInSec.containsKey(currentPointCounter))
				{
					TraderDelayInSec.put(currentPointCounter, Rnd.get(15, 40)*1000);
				}
				if (lock_target != null) // если добежали, сброс таргента
				{
					if (getActor().isInRangeZ(lock_target, 22))
					{
						getActor().setTarget(lock_target);
						getActor().getAI().Attack(lock_target, false, false);
						if (!superpointWait && System.currentTimeMillis()-TimeoutAfterServerStartup > TraderDelayInSec.get(currentPointCounter))
						{
							TimeoutAfterServerStartup = System.currentTimeMillis()+TraderDelayInSec.get(currentPointCounter);
							superpointWait = true;
							return;
						}
						lock_target = null;
						TraderCount = 0;
						targetcount = 0;
						checkTrade = false;
						return;
					}
					getActor().moveToLocation(lock_target.getLoc().getX()+Rnd.get(10, 20), lock_target.getLoc().getY()+Rnd.get(10, 20), lock_target.getLoc().getZ(), 0, true);
					return;
				}
				if (lock_target == null && targetcount <= TraderCount)
				{
					List <Player> trader = getActor().getAroundPlayers(2000, 200).stream().filter(d->d != null && d.isInStoreMode()).collect(Collectors.toList());

					if (trader.size() > 2)
					{
						lock_target = Rnd.get(trader);
						targetcount++;
						superpointWait = false;
					}
					return;
				}

			}
			/*
			 * if (currentPointCoordinate.getSocialId() > 0 && System.currentTimeMillis()-superpointTimeoutAfterServerStartup > currentPointCoordinate.getDelayPoint()) { getActor().broadcastPacket(new SocialAction(getActor().getObjectId(), currentPointCoordinate.getSocialId())); }
			 */

			superpointWait = false;
			/*	if (getActor().phantom_params.getTrafficScheme() == null)
			{
				getActor().kick();
				return;
			}*/

			if (currentPointCounter < _routeTask.getPoints().size()-1)
				currentPointCounter++;
		}

	}

	protected class MountPetDelay extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			if (getActor() == null)
				return;
			PetInstance pet = getActor().getPet();
			if (pet != null && pet.isMountable())
			{
				//getActor().setMount(pet.getTemplate().getId(), pet.getObjectId(), pet.getLevel());
				getActor().getPet().unSummon(false);
			}

		}
	}

	public void doPeaceAction1()
	{
		if (getActor().getPhantomType() == PhantomType.PHANTOM_BOT_HUNTER || getActor().getPhantomType() == PhantomType.PHANTOM_PARTY || getActor().getPhantomType() == PhantomType.PHANTOM_CLAN_MEMBER)
			return;

		Location loc = HuntingZoneHolder.getInstance().getRandomPoint(getActor());
		if (loc != null)
		{
			getActor().phantom_params.getPhantomAI().startBuffTask(100); // ребаф перед телепортом
			getActor().teleToLocation(new Location(loc.getX() + Rnd.get(200), loc.getY() + Rnd.get(200), loc.getZ()));
		}
		else
			getActor().kick();
	}

	public boolean isNeedToGetNewTargetForHealer()
	{
		Player phantom = getActor();

		Creature target = phantom.phantom_params.getLockedHealerTarget();

		// если цели нет, то проверки бессмыленны
		if (target == null)
			return true;

		if(getActor().phantom_params.getResTarget()!=null)
			return true;

		if (target.isDeleted())
		{
			getActor().phantom_params.setLockedHealerTarget(null);
			getActor().setTarget(null);
			return true;
		}

		// если цель ушла в инвиз
		if (target.isInvisible() && target.isPlayer())
		{
			getActor().phantom_params.setLockedHealerTarget(null);
			getActor().setTarget(null);
			return true;
		}

		// у цели достаточно хп
		if (target.getCurrentHpPercents() > 90)
		{
			getActor().phantom_params.setLockedHealerTarget(null);
			getActor().setTarget(null);
			return true;
		}
		// цель дохлая, добавим
		if (target.isDead())
		{
			getActor().phantom_params.setLockedHealerTarget(null);
			getActor().setTarget(null);
			return true;
		}

		// если цель не видно - бежим к ней
		try
		{
			// но если подбежать невозможно, то сбрасываем таргет
			if (!GeoEngine.canSeeTarget(phantom, target, false))
			{
				Location loc = target.getLoc();
				if (GeoEngine.canMoveToCoord(phantom.getX(), phantom.getY(), phantom.getZ(), loc.x, loc.y, loc.z, phantom.getGeoIndex()))
				{
					phantom.moveToLocation(loc, 0, true);
					return false;
				}
				else
				{
					getActor().phantom_params.setLockedHealerTarget(null); // убираем свои цели
					return true;
				}
			}
		}catch(NullPointerException e)
		{
			getActor().phantom_params.setLockedHealerTarget(null);
			getActor().setTarget(null);
			_log.error("Phantom Default AI error."+phantom+" X: "+phantom.getX()+", Y: "+phantom.getY()+", Z: "+phantom.getZ()+", tX: "+target.getX()+", tY: "+target.getY()+", tZ: "+target.getZ()+", geoIndex: "+phantom.getGeoIndex());
			return true;
		}

		return false;
	}

	public boolean isNeedToGetNewTarget()
	{
		Player phantom = getActor();

		Creature target = phantom.phantom_params.getLockedTarget();

		// если цели нет, то проверки бессмыленны
		if (target == null || phantom.isDead())
			return true;

		// если мы убили цель, то освобождаем преследуемую цель
		if (target.isDead())
		{
			removeTargets();
			return true;
		}

		if (target.isPlayer() && target.getPlayer().isGMInvisible())
		{
			removeTargets();
			return true;
		}

		// если откинули копыта то освобождаем преследуемую цель
		if (phantom.isDead())
		{
			removeTargets();
			return false;
		}

		if (target.isDeleted())
		{
			removeTargets();
			return true;
		}
		//TODO реализовать другой вариант
		/*if (!phantom.isOlympiadCompStart() && phantom.phantom_params.getLockedTargetFirstLocation() !=null && phantom.getDistance(phantom.phantom_params.getLockedTargetFirstLocation()) >2000)
		{
			removeTargets();
			return true;
		}*/
		if (!phantom.isOlympiadCompStart())
		{
			if (target.isPlayer() && target.getPvpFlag() == 0 && !target.isInZone(ZoneType.battle_zone) && phantom.getPhantomType() == PhantomType.PHANTOM_PARTY ? true: !Config.EnablePlayerKiller) // не трогать без флага(или конфига) в поле иначе пк
			{
				removeTargets();
				return true;
			}
		}
		// если цель ушла в инвиз
		if (target.isInvisible() || target.getAbnormalList().containsEffects(922))
		{
			// Пробуем выбить из инвиза
			if (castDetectionSkill(target))
				return false;

			// если не вышло, то переключаемся на новый таргет
			removeTargets();
			return true;
		}

		if (/*phantom.isInPvPEvent() &&*/ target.isFakeDeath())
		{
			removeTargets();
			return true;
		}
		if (target.isMonster() && phantom.getPhantomType() == PhantomType.PHANTOM)
		{
			
			if(phantom.getAroundPlayers(900, 200).stream().filter(p-> p.getFraction()!=phantom.getFraction() &&!p.isFakeDeath()&& !p.isDead()&& !p.getAbnormalList().containsEffects(922)&&!p.isInvisible()&&!p.getPlayer().isGMInvisible()).count()>0)
			{
				removeTargets();
				return true;
			}
			
			if (((Creature) target).getAI() instanceof DefaultAI)
			{
				Creature _AttackTarget = ((DefaultAI) ((Creature) target).getAI()).getAttackTarget();
				if (_AttackTarget != null && _AttackTarget != phantom && _AttackTarget.isPhantom())
				{
					removeTargets();
					return true;
				}
			}
		}

		/*if (target.isMonster() && ((MonsterInstance) target).isSpoiled(phantom) && target.isDead())
			{
				if (target != null && solo_sweper != null && !phantom.isSkillDisabled(solo_sweper))
				{
					phantom.getAI().Cast(solo_sweper, target, true, true);
					removeTargets();
					return true;
				}
			}*/
		/*	if (checkMassSweep())
			{
				if (target != null && mass_sweper != null && !phantom.isSkillDisabled(mass_sweper))
				{
					phantom.getAI().Cast(mass_sweper, phantom, true, true);
					removeTargets();
					return true;
				}
			}*/


		if (!phantom.isOlympiadCompStart())
		{
			// Если цель убежала достаточно далеко с момента встречи, то нет мысла гоняться
			if (/*!phantom.isInPvPEvent() && */phantom.phantom_params.getLockedTargetFirstLocation() != null && phantom.phantom_params.getLockedTargetFirstLocation().distance(target.getLoc()) > 1000)
			{
				removeTargets();
				return true;
			}

			// Если цель спряталась в мирной зоне
			if (target.isInPeaceZone() && !target.isMonster())
			{
				removeTargets();
				return true;
			}
		}
		if (phantom.phantom_params.getGmLog())
			GmListTable.broadcastToGMs(new SayPacket2(0, ChatType.HERO_VOICE, "SYS:", phantom.getName()+"\n"+" isNeedToGetNewTarget 4"));
		// Если включен режим ассиста проверяем постоянно цель
		/*if (_master != null && phantom.getPhantomType() == PhantomType.PHANTOM_SERVANTS && target != _master.getTarget() && phantom.phantom_params.getServantMode() == ServantMode.ASSIST)
		{
			removeTargets();
			return true;
		}*/
		// если цель не видно - бежим к ней
		try
		{
			// но если подбежать невозможно, то сбрасываем таргет
			// if(!GeoEngine.canSeeTarget(phantom, target, false))

			if (!target.isDoor() && !phantom.isMoving() && !GeoEngine.canMoveToCoord(actor.getX(), actor.getY(), actor.getZ(), target.getX(), target.getY(), target.getZ(), actor.getGeoIndex())) // Для подстраховки
			{
				if (PhantomUtils.availabilityCheck(actor, target.getLoc()))
				{
					phantom.moveToLocation(target.getLoc(), 0, true);
				}
				else
				{
					if(phantom.getPhantomType() != PhantomType.PHANTOM_CLAN_MEMBER)
						if (target.isMonster())
							phantom.phantom_params.addIgnore(target.getObjectId(), System.currentTimeMillis()+60000);//игнор на 1 минуту

					removeTargets();
					return true;
				}
			}
			if (phantom.phantom_params.getGmLog())
				GmListTable.broadcastToGMs(new SayPacket2(0, ChatType.HERO_VOICE, "SYS:", phantom.getName()+"\n"+" isNeedToGetNewTarget 5"));
		}catch(NullPointerException e)
		{
			removeTargets();
			_log.error("Phantom Default AI error."+phantom+" X: "+phantom.getX()+", Y: "+phantom.getY()+", Z: "+phantom.getZ()+", tX: "+target.getX()+", tY: "+target.getY()+", tZ: "+target.getZ()+", geoIndex: "+phantom.getGeoIndex());
			return true;
		}

		return false;
	}

	/*public boolean checkMassSweep()
	{
		List <MonsterInstance> Monsters = getActor().getAroundMonsters(350, 350);
		int count = 0;
		for(Creature monster : Monsters)
		{
			if (monster.isDead() && ((MonsterInstance) monster).isSpoiled())
			{
				count++;
			}
		}
		Monsters.clear();

		if (count >= 1)
			return true;
		return false;
	}*/

	protected void removeTargets()
	{
		getActor().phantom_params.setLockedTarget(null); // убираем свои цели
		getActor().setTarget(null);
		if(getActor().phantom_params.getPhantomPartyAI() !=null&& getActor().phantom_params.getPhantomPartyAI().getPartyAssister() == getActor())
		{
			getActor().phantom_params.getPhantomPartyAI().setPartyTarget(null);
		}
	}

	public boolean castCleansingSkills(Creature target, int id_debuff)
	{
		if (getActor().isCastingNow())
			return false;

		SkillsGroup cl_skill = getActor().phantom_params.getClassAI().getCleansingSkills();
		if (cl_skill == null)
			return false;

		for(PhantomSkill skill : cl_skill.getAllSkills())
		{
			if (skill.getCondition().containsKey(Condition.SELF_SKILL_EFFECT))
			{
				Skill casting_skill = getSkill(skill);
				if (casting_skill == null)
					continue;
				if (actor.isSkillDisabled(casting_skill))
					continue;
				PCondition pSkill = skill.getCondition().get(Condition.SELF_SKILL_EFFECT);

				if (pSkill == null)
					continue;
				if (pSkill.getList().contains(id_debuff))
					return CastSkill(actor, casting_skill, actor, true)==0 ? true:false;
			}
		}

		return false;
	}

	public void CheckAndUseItem(ItemsGroup items, Player Master)
	{
		if (getActor()==null || items == null || items.getAllItems().size() == 0)
			return;
		for(PhantomItem sk : items.getAllItems())
		{
			if(sk==null||sk.getId() == 0)
				continue;
			long now = System.currentTimeMillis();
			if (getActor().phantom_params.getItemsDelayList().containsKey(sk.getId()))
			{
				if (now > getActor().phantom_params.getItemsDelayList().get(sk.getId()))
				{
					getActor().phantom_params.deleteItemDelay(sk.getId());
					getActor().phantom_params.addItemDelay(sk.getId(), now+sk.getDelay());
					getActor().setTarget(Master);
					useItem(sk, Master);
					break;
				}
			}
			else
			{
				getActor().phantom_params.addItemDelay(sk.getId(), now+sk.getDelay());
				getActor().setTarget(Master);
				useItem(sk, Master);
				break;
			}

		}
	}

	public void useItem(PhantomItem sk, Player target)
	{
		ItemInstance item = actor.getInventory().getItemByItemId(sk.getId());
		if (item != null)
		{
			if (CheckUseItem(sk))
			{
				IItemHandler handler = item.getTemplate().getHandler();
				handler.useItem(actor, item, false);
				if (actor.phantom_params.getGmLog())
					GmListTable.broadcastToGMs(new SayPacket2(0, ChatType.HERO_VOICE, "sys:", "UseItem:"+actor+" item"+item));
			}
		}
	}

	public Boolean CheckUseItem(PhantomItem sk)
	{
		Boolean check = true;
		for(Entry <Condition,PCondition> cond : sk.getCondition().entrySet())
		{
			switch (cond.getKey())
			{
				case CONSUMED_SOULS:
					if (getActor().getConsumedSouls() > cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case CHARGING:

					if (actor.getAbnormalList() == null || actor.getIncreasedForce() >= cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case MIN_CP:
					if (actor.getCurrentCpPercents() > cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case MIN_HP:
					if (actor.getCurrentHpPercents() > cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case MIN_MP:
					if (actor.getCurrentMpPercents() > cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case CHANCE_CAST:
					if (Rnd.chance(cond.getValue().IntParameter()))
					{
						check = false;
						continue;
					}
					break;
				default:
					break;
			}
		}
		return check;
	}

	public Skill getSkill(PhantomSkill sk)
	{
		if (sk.getId() <= 0)
			return null;
		return getActor().getSkillById(sk.getId());
	}

	public Skill getRandomHealSkillFromTheList(Creature target, SkillsGroup sk)
	{
		List <PhantomSkill> all_skill = sk.getAllSkills();
		if (all_skill == null || all_skill.isEmpty() || target == null)
			return null;

		List <Skill> skill_list = new ArrayList <Skill>();
		for(PhantomSkill t_skill : all_skill)
		{
			Skill p_skill = getSkill(t_skill);
			if (p_skill == null)
				continue;
			if (!(p_skill.isActive() || p_skill.isToggle()))
				continue;

			if (this.actor.isSkillDisabled(p_skill))
				continue;

			Creature new_target = p_skill.getAimingTarget(getActor(), target);
			if (new_target == null)
				continue;

			if (!t_skill.getCondition().isEmpty()) // проверяем наличие кондишена
				if (!checkConditionSkill(new_target, t_skill)) // проверяем условия если не подошло отменяем каст
				{
					if (getActor().phantom_params.getGmLog())
						GmListTable.broadcastToGMs(new SayPacket2(0, ChatType.HERO_VOICE, "SYS:", getActor().getName()+"\n"+" skill "+p_skill+" checkConditionSkill false"));
					continue;
				}

			skill_list.add(p_skill);
		}

		if (skill_list == null || skill_list.isEmpty())
			return null;

		Skill fgsd3 = Rnd.get(skill_list);
		if (getActor().phantom_params.getGmLog())
			GmListTable.broadcastToGMs(new SayPacket2(0, ChatType.HERO_VOICE, "SYS:", getActor().getName()+"\n"+" skill "+fgsd3));

		return fgsd3;
	}

	public Skill getRandomSkillFromTheList(Creature attacker, Creature target, SkillsGroup sk, int... skill_priority)
	{
		List <PhantomSkill> all_skill = sk.getAllSkills();
		if (all_skill == null || all_skill.isEmpty())
			return null;

		if (skill_priority != null && skill_priority.length != 0 && !attacker.isSkillDisabled(attacker.getSkillById(skill_priority[0])))
			all_skill = all_skill.stream().filter(s->s != null && s.getId() == skill_priority[0]).collect(Collectors.toList());

		List <Skill> skill_list = new ArrayList <Skill>();
		outer:for(PhantomSkill t_skill : all_skill)
		{
			Skill p_skill = getSkill(t_skill);
			if (p_skill == null)
				continue;

			if ( attacker.isSkillDisabled(p_skill))
				continue;

			if (!p_skill.getWeaponDependancy(attacker))
				continue;

			if (attacker.isSummon() && t_skill.getTargetType() == SkillTargetType.TARGET_OWNER)
				target = attacker.getPlayer();

			if (t_skill.getTargetType() == SkillTargetType.TARGET_SUMMONS)
			{
				/*SummonInstance sum = attacker.getPlayer().getSummonList().getSummon();
					if (sum!=null&& !sum.isDead())
						target = sum;
					else
						continue;*/
			}
			if (t_skill.getTargetType() == SkillTargetType.TARGET_SELF)
				target = attacker.getPlayer();
			//TODO
			if (!t_skill.getCondition().isEmpty()) // проверяем наличие кондишена
				if (!checkConditionSkill(target, t_skill)) // проверяем условия если не подошло отменяем каст
					continue;

			Creature new_target = getAimingTarget(p_skill, attacker, target);

			if (attacker.isSummon() && t_skill.getTargetType() == SkillTargetType.TARGET_OWNER)
				new_target = attacker.getPlayer();

			if (new_target == null)
				continue;

			if (new_target.getAbnormalList().getEffectBySkillId(p_skill.getId()) != null)
				continue;

			if (p_skill.getEffectTemplates(EffectUseType.NORMAL).size() > 0 && !p_skill.isToggle())
				for(Abnormal ef : new_target.getAbnormalList().getEffects())
				{
					if (checkEffect(ef, p_skill))
						continue outer;
				}
			skill_list.add(p_skill);
		}

		if (skill_list == null || skill_list.isEmpty())
			return null;

		Skill fgsd3 = Rnd.get(skill_list);
		if (attacker.getPlayer().phantom_params.getGmLog())
			GmListTable.broadcastToGMs(new SayPacket2(0, ChatType.HERO_VOICE, "SYS:", getActor().getName()+"\n"+" skill "+fgsd3));

		return fgsd3;
	}

	/**
	 * Возвращает true если эффект для скилла уже есть и заново накладывать не надо
	 */
	private boolean checkEffect(Abnormal ef, Skill skill)
	{
		if(ef == null)
			return false;
		if(ef.checkBlockedAbnormalType(skill.getAbnormalType()))
			return true;
		EffectTemplate effectTemplate = skill.getEffectTemplates(EffectUseType.NORMAL).get(0);
		return AbnormalList.checkAbnormalType(ef.getTemplate(), effectTemplate) && ef.getAbnormalLvl() >= effectTemplate.getAbnormalLvl() && (ef.getTimeLeft() > 10 || ef.getNext() != null && checkEffect(ef.getNext(), skill));
	}

	// XXX проверка умений по кондишенах
	public boolean checkConditionSkill(Creature target, PhantomSkill check_skill)
	{
		Boolean check = true;
		Skill skill = getActor().getSkillById(check_skill.getId());

		// не бить в танк рефлект
		if (skill.isMagic() && skill.getSkillType() == SkillType.MDAM && target!=null&& target.isPlayer() && target.getAbnormalList().containsEffects(916))
			return false;

		// затычка сакра паладина, не использовать без заточки 
		if (skill!=null && skill.getId() == 69 && skill.getLevel()< 35)
			return false;

		// затычка умения макс заряда
		if (check_skill.getId() == 919 && getActor().getIncreasedForce() >1 && target!=null && target.isPlayer())
			return false;
		//затычка макс души камаелям
		if (check_skill.getId() == 625 && getActor().getConsumedSouls() >10 || check_skill.getId() == 502 && getActor().getConsumedSouls() >10)
			return false;

		// умения потребляющие заряды
		if(skill !=null && skill.getNumCharges() > 0 && skill.getNumCharges() > getActor().getIncreasedForce())
			return false;

		for(Entry <Condition,PCondition> cond : check_skill.getCondition().entrySet())
		{
			switch (cond.getKey())
			{
				case CONSUMED_SOULS:
					if (getActor().getConsumedSouls() > cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case USE_OLYMPIAD:
					if (!getActor().isInOlympiadMode())
					{
						check = false;
						continue;
					}
					break;
				case NOT_USE_OLYMPIAD:
					if (getActor().isInOlympiadMode())
					{
						check = false;
						continue;
					}
					break;
				case USE_IN_PARTY:
					if (!getActor().isInParty())
					{
						check = false;
						continue;
					}
					break;
				case NOT_USE_IN_PARTY:
					if (getActor().isInParty())
					{
						check = false;
						continue;
					} 
					break;
				case ONLY_MONSTER:
					if (target == null || !target.isMonster())
					{
						check = false;
						continue;
					}
					break;
				case ONLY_PLAYER:
					if (target == null || !target.isPlayer())
					{
						check = false;
						continue;
					}
					break;
				case TARGET_MAGE:
					if (target == null || (target.isPlayer() && !target.isMageClass()))
					{
						check = false;
						continue;
					}
					break;
				case TARGET_FIGHTER:
					if (target == null || (target.isPlayer() && target.isMageClass()))
					{
						check = false;
						continue;
					}
					break;
				case NOT_SPOILED:
					if (target == null || !(target instanceof MonsterInstance) || ((MonsterInstance) target).isSpoiled())
					{
						check = false;
						continue;
					}
					break;
				case SPOILED:
					if (target == null || !(target instanceof MonsterInstance) || !((MonsterInstance) target).isSpoiled())
					{
						check = false;
						continue;
					}
					break;
				case CHANCE_CAST:
					if (!Rnd.chance(cond.getValue().IntParameter()))
					{
						check = false;
						continue;
					}
					break;
				case MIN_DISTANCE: // минимальная дистанция для атаки от param и дальше 
					if (target == null || getActor().getDistance3D(target) < cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case MAX_DISTANCE: // максимальная дистанция для атаки  от param и ближе 
					if (target == null || getActor().getDistance3D(target) > cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case MIN_CP:
					if (getActor().getCurrentCpPercents() > cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case MIN_HP:
					if (getActor().getCurrentHpPercents() > cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case MIN_MP:
					if (getActor().getCurrentMpPercents() > cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case MAX_CP:
					if (getActor().getCurrentCpPercents() < cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case MAX_HP:
					if (getActor().getCurrentHpPercents() < cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case MAX_MP:
					if (getActor().getCurrentMpPercents() < cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case TARGET_MIN_CP:
					if (target == null || target.getCurrentCpPercents() > cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case TARGET_MIN_HP:
					if (target == null || target.getCurrentHpPercents() > cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case TARGET_MIN_MP:
					if (target == null || target.getCurrentMpPercents() > cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case TARGET_MAX_CP:
					if (target == null || target.getCurrentCpPercents() < cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case TARGET_MAX_HP:
					if (target == null || target.getCurrentHpPercents() < cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case TARGET_MAX_MP:
					if (target == null || target.getCurrentMpPercents() < cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case TARGET_MAX_HP_COUNT:
					if (target == null || target.getCurrentHp() < cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case TARGET_MIN_HP_COUNT:
					if (target == null || target.getCurrentHp() > cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case MIN_PARTY_HP:
				{
					if (getActor().isInParty())
					{
						// затычка на умение "баланс жизни" - кардинал
						/*	if (check_skill.getId() == 1335 && getActor().getCurrentHpPercents() < cond.getValue().getList().get(0))
							continue;

						int needAheal = 0;
						for(Player member : getActor().getParty().getMembers())
							if (member.getCurrentHpPercents() < cond.getValue().getList().get(0))
								needAheal++;

						if (needAheal < cond.getValue().getList().get(1))
						{
							check = false;
							continue;
						}*/
					}
					else
					{
						check = false;
						continue;
					}
					break;
				}
				case MASS:
					if (getActor().getOlympiadGame() != null && skill != null) // на олимпе снимем ограничение
						continue;

					if (skill == null || target == null || target.getAroundCharacters(skill.getAffectRange(), 200).stream().filter(p->p != null && getActor() != p && (getActor().getClan() != null && getActor().getClan() != p.getClan()) // исключим клан
							&& (p.isPlayer() && (getActor().getParty() != null && getActor().getParty() != p.getPlayer().getParty())) // исключим пати
							&& (p.isPlayer() && (getActor().isInZoneBattle() || p.getPvpFlag() > 0 || p.getKarma() > 0))).collect(Collectors.toList()).size() < cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case MASS_PLAYERS:
					if (getActor().getOlympiadGame() != null && skill != null) // на олимпе снимем ограничение
						continue;

					if (skill == null || target == null || target.getAroundPlayers(skill.getAffectRange(), 200).stream().filter(p->p != null && getActor() != p && (getActor().getClan() != null && getActor().getClan() != p.getClan()) // исключим клан
							&& (getActor().getParty() != null && getActor().getParty() != p.getParty()) // исключим пати
							&& (getActor().isInZoneBattle() || p.getPvpFlag() > 0 || p.getKarma() > 0)).collect(Collectors.toList()).size() < cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
					/*	case CHARGING:
					if (skill== null || getActor().getIncreasedForce() >= 8 ||  (skill.getEffectTemplates()!=null && skill.getEffectTemplates().length > 0 && skill.getEffectTemplates()[0].getEffectType() == EffectType.Charge &&getActor().getIncreasedForce() >= skill.getEffectTemplates()[0]._value))
					{
						check = false;
						continue;
					}
					break;*/
				case SELF_NOT_SKILL_EFFECT:
					if (getActor().getAbnormalList()!=null&& getActor().getAbnormalList().containEffectFromSkills(cond.getValue().getList().stream().mapToInt(i->i).toArray()))
					{
						check = false;
						continue;
					}
					break;
				case SELF_SKILL_EFFECT:
					if (getActor().getAbnormalList()!=null&& !getActor().getAbnormalList().containEffectFromSkills(cond.getValue().getList().stream().mapToInt(i->i).toArray()))
					{
						check = false;
						continue;
					}
					break;
				case TARGET_CLASS_ID:
					if (target != null && target.isPlayer() && !cond.getValue().getList().contains(target.getPlayer().getClassId().getId()))
					{
						check = false;
						continue;
					}
					break;
				case TARGET_SKILL_EFFECT:
					if (target != null && target.getAbnormalList()!=null&&  !target.getAbnormalList().containEffectFromSkills(cond.getValue().getList().stream().mapToInt(i->i).toArray()))
					{
						check = false;
						continue;
					}
					break;
					/*case TARGET_SUMMON:
					if (target != null)
					{
						if (target.getPlayer().getSummonList().getSummon() != null)
							getActor().phantom_params.setLockedTarget(target.getPlayer().getSummonList().getSummon());
						else
						{
							check = false;
							continue;
						}
					}
					break;*/
				case TARGET_NOT_USE_CLASS_ID:
					if (target != null && target.isPlayer() && cond.getValue().getList().contains(target.getPlayer().getClassId().getId()))
					{
						check = false;
						continue;
					}
					break;
				case TARGET_SKILL_DISABLED:
					if (target != null)
					{
						for (Integer tmp : cond.getValue().getList())
							if (target.getSkillById(tmp) !=null)
								if (!target.isSkillDisabled(target.getSkillById(tmp)))
								{
									check = false;
									continue;
								}
					}
					break;
				case TARGET_WEAPON_TYPE:
					if (target == null || (target.getActiveWeaponInstance() != null && !cond.getValue().getWeaponTypeList().contains(target.getActiveWeaponInstance().getItemType())))
					{
						check = false;
						continue;
					}
					break;
				case TARGET_ARMOR_TYPE:
					if (target == null || (target.isPlayer() && target.getPlayer().getInventory().getPaperdollItem(10) !=null &&  !cond.getValue().getArmorTypeList().contains(target.getPlayer().getInventory().getPaperdollItem(10).getTemplate().getItemType())))
					{
						check = false;
						continue;
					}
					break;	

					/*	case SUMMON_NPC_ID:
					SummonInstance sum = getActor().getSummonList().getSummon();
					if (sum == null||sum.isDead() || cond.getValue().IntParameter() != sum.getNpcId())
					{
						check = false;
						continue;
					}
					break;*/
				case MASS_MONSTER_SPOILED:
				{
					if (skill == null || getActor().getAroundMonsters(skill.getAffectRange(), 200).stream().filter(p->p != null && !((MonsterInstance)p).isSpoiled()).collect(Collectors.toList()).size() < cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				}
				case MASS_MONSTER_SWEEPER:
				{
					if (skill == null || getActor().getAroundMonsters(skill.getAffectRange(), 200).stream().filter(p->p != null && p.isDead() && ((MonsterInstance)p).isSpoiled(getActor())).collect(Collectors.toList()).size() < cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				}
				case MASS_MONSTER:
				{
					if (getActor().getOlympiadGame() != null) // на олимпе не используем
					{
						check = false;
						continue;
					}
					if (cond.getValue().IntParameter() == 0)
					{
						if (target == null || getActor().getAroundMonsters(120, 100).size() >1)
						{
							check = false;
							continue;
						}
					}
					else
					{
						if (skill == null || target == null ||  getActor().getAroundMonsters(skill.getAffectRange(), 200).stream().filter(p->p != null && !p.isDead()).collect(Collectors.toList()).size() < cond.getValue().IntParameter())
						{
							check = false;
							continue;
						}
					}

					break;
				}
				/*case NPC_RACE:
					if (target == null|| !target.isMonster() || (target !=null && (target instanceof MonsterInstance) &&  !cond.getValue().getNpcRace().contains(((MonsterInstance) target).getTemplate().getRace())) )
					{
						check = false;
						continue;
					}
					break;*/

				case TARGET_NOT_RUNNING:
					if (target == null|| target.isMoving() || target.isFollowing())
					{
						check = false;
						continue;
					}
					break;
				case	TARGET_IS_COMING: //TODO
					break;
				case TARGET_RUNS_AWAY:
					if (target == null|| !target.isMoving() || getActor().getDistance(GeoMove.getIntersectPoint(actor.getLoc(), target.getLoc(), target.getMoveSpeed(), Math.max(128, Config.MOVE_TASK_QUANTUM_PC/2)))  <=  getActor().getDistance(target.getLoc()))
					{
						check = false;
						continue;
					}
					break;
				case TARGET_RUNNING:
					if (target == null|| !target.isMoving())
					{
						check = false;
						continue;
					}
					break;
				case NO_TARGET:
					if (target!=null)
					{
						check = false;
						continue;
					}
					break;
					//TODO
				case DEBUFF_CHANCE:
					if(target ==null)
					{
						check = false;
						continue;
					}			
					/*	int success =  Formulas.calcSkillSuccess(getActor(), target, skill, skill.getActivateRate());


					if (success < cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}*/
					break;
				case CUBIC:
					if (getActor().getCubics().size() >= (int) getActor().calcStat(Stats.CUBICS_LIMIT, 1.0))
					{
						check = false;
						continue;
					}
					break;
				case IS_IN_BATTLE:
					if (!getActor().isInCombat())
					{
						check = false;
						continue;
					}
					break;
				case NOT_IN_BATTLE:
					if (getActor().isInCombat())
					{
						check = false;
						continue;
					}
					break;
				case SKILL_DISABLED:
					if (skill == null || getActor().getSkillById(cond.getValue().IntParameter())== null || !getActor().isSkillDisabled(getActor().getSkillById(cond.getValue().IntParameter())))
					{
						check = false;
						continue;
					}
					break;
				case CHECK_WEAPON_ATTRIBUTE:
					if (getActor().getActiveWeaponInstance() == null || !cond.getValue().getElement().contains(getActor().getActiveWeaponInstance().getAttributeElement()))
					{
						check = false;
						continue;
					}
					break;
				case TARGET_CHECK_WEAPON_ATTRIBUTE:
					if (target == null || target.getActiveWeaponInstance() == null || !cond.getValue().getElement().contains(target.getActiveWeaponInstance().getAttributeElement()))
					{
						check = false;
						continue;
					}
					break;
					/*case LIVE_SUMMON:
					if (getActor().getSummonList().getSummon() == null || getActor().getSummonList().getSummon().isDead())
					{
						check = false;
						continue;
					}
					break;*/
				case TARGET_DIRECTION:
					if(target ==null || !cond.getValue().getTargetDirection().contains(PositionUtils.getDirectionTo(target, getActor())))
					{
						check = false;
						continue;
					}			
					break;
				case TARGET_NOT_SKILL_EFFECT:
					if (target == null || target.getAbnormalList()!=null && target.getAbnormalList().containEffectFromSkills(cond.getValue().getList().stream().mapToInt(i->i).toArray()))
					{
						check = false;
						continue;
					}
				case CANCEL_BUFF_BY_ID:
					break;
					/*case SUMMON_MAX_HP:
					if (getActor().getSummonList().getSummon() == null || getActor().getSummonList().getSummon().isDead() || getActor().getSummonList().getSummon().getCurrentHpPercents() < cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case SUMMON_MAX_MP:
					if (getActor().getSummonList().getSummon() == null || getActor().getSummonList().getSummon().isDead() || getActor().getSummonList().getSummon().getCurrentMpPercents() < cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case SUMMON_MIN_HP:
					if (getActor().getSummonList().getSummon() == null || getActor().getSummonList().getSummon().isDead() || getActor().getSummonList().getSummon().getCurrentHpPercents() > cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;
				case SUMMON_MIN_MP:
					if (getActor().getSummonList().getSummon() == null || getActor().getSummonList().getSummon().isDead() || getActor().getSummonList().getSummon().getCurrentMpPercents() > cond.getValue().IntParameter())
					{
						check = false;
						continue;
					}
					break;*/
				case CANCELING_CAST:
					if (target == null || target.getCastingSkill() == null || !cond.getValue().getList().contains(target.getCastingSkill().getId()))
					{
						check = false;
						continue;
					}
					break;
			}
		}
		if (check && check_skill.getCondition().containsKey(Condition.CANCEL_BUFF_BY_ID))
		{
			getActor().getAbnormalList().stopEffects(check_skill.getCondition().get(Condition.CANCEL_BUFF_BY_ID).IntParameter());
			check = false;
		}
		return check;
	}

	public Location getLocBehind(Player player, GameObject target)
	{
		if (target == null)
			return null;
		Location loc;
		double radian = PositionUtils.convertHeadingToRadian(target.getHeading());
		loc = new Location(target.getX()+(int) (Math.sin(radian)*40), target.getY()-(int) (Math.cos(radian)*40), target.getZ());
		loc.correctGeoZ();
		if (!GeoEngine.canMoveToCoord(player.getX(), player.getY(), player.getZ(), loc.x, loc.y, loc.z, player.getGeoIndex()))
		{
			loc = target.getLoc(); // Если не получается встать рядом с объектом, пробуем встать прямо в него
			if (!GeoEngine.canMoveToCoord(player.getX(), player.getY(), player.getZ(), loc.x, loc.y, loc.z, player.getGeoIndex()))
				return null;
		}
		return loc;
	}

	public boolean castNukeSkill(Creature target)
	{
		if (getActor().isCastingNow() || target == null)
			return false;

		if (target.isPlayer() && target.getAbnormalList()!=null && target.getAbnormalList().containEffectFromSkills(INVINCIBLE))
			return false;

		SkillsGroup nuke = getActor().phantom_params.getClassAI().getNukeSkills();

		if (target.isPlayer() && nuke.getGroupChanceCast() != 100 && !Rnd.chance(nuke.getGroupChanceCast()))
			return false;

		if (nuke.getChanceCastToMonster()!=100 && target.isMonster()&& !Rnd.chance(nuke.getChanceCastToMonster()))
			return false;

		Skill casting_skill = getRandomSkillFromTheList(actor, target, nuke);

		if (casting_skill != null && !actor.isSkillDisabled(casting_skill))
			return CastSkill(actor, casting_skill, target, true)==0 ? true:false;
		return false;
	}

	public boolean castDetectionSkill(Creature target)
	{
		if (getActor().isCastingNow())
			return false;
		/*if (getActor().getClassId().getId() == 96)
		{
			SummonInstance summ = getActor().getSummonList().getSummon();
			if (summ==null || summ.isDead())
				return false;

			Integer a_dist =  (int) target.getDistance(getActor()); // дистанция от цели к нам

			Integer s_dist = (int) target.getDistance(summ); // дистанция от цели к суму

			 int compare = a_dist.compareTo(s_dist);
			 if (compare == 1 || compare == 0)
			 {
				 //каст масухи на пета
			 } else
				 getActor().getTemplate()
		}*/
		SkillsGroup nuke = getActor().phantom_params.getClassAI().getDetectionSkills();
		Skill casting_skill = getRandomSkillFromTheList(actor, target, nuke);
		if (casting_skill != null && !actor.isSkillDisabled(casting_skill) && target != null && actor.isInRange(target, casting_skill.getAffectRange()))
			return CastSkill(actor, casting_skill, target, true)==0 ? true:false;
		return false;
	}

	public void castSelfBuffSkill()
	{
		if (getActor().isCastingNow() || getActor().isInPeaceZoneOld())
			return;

		if (getActor().phantom_params.getPhantomAI().isHealer() && getActor().getCurrentHpPercents() < 50)
			return;
		SkillsGroup buffs = getActor().phantom_params.getClassAI().getSelfBuffs();
		if (buffs == null)
			return;
		Skill casting_skill = getRandomSkillFromTheList(getActor(),getActor(), buffs);
		if (casting_skill == null)
			return;
		if (getActor().phantom_params.getGmLog())
			GmListTable.broadcastToGMs(new SayPacket2(0, ChatType.HERO_VOICE, "SYS:", getActor().getName()+"\n"+"castSelfBuffSkill " +casting_skill));
		CastSkill(getActor(), casting_skill, getActor(), false);
	}

	public boolean castPartyIcon(int byffid, int skillid)
	{
		/*	if (getActor().getLevel() < 83)
			return false;
		Skill skill = getActor().getSkillById(skillid);
		if (getActor().isSkillDisabled(skill)||getActor().getAbnormalList().getEffectBySkillId(skill.getId()) != null )
			return false;

		List <Abnormal> el = getActor().getAbnormalList().getEffectBySkillId(byffid);
		if (el == null)
			return false;
		for(Effect effect : el)
			if (effect != null && effect.getSkill().getLevel() >= 3)
			{
				getActor().getAI().Cast(skill, getActor(), true, true);
				return true;
			}*/
		return false;
	}

	public boolean castBuffSkillOnPartyMember()
	{
		if(actor.getParty() == null)
			return false;
		SkillsGroup buffs = getActor().phantom_params.getClassAI().getBuffSkills();
		for(Player member : actor.getParty().getPartyMembers())
		{
			Skill casting_skill = getRandomSkillFromTheList(getActor(),member, buffs);
			if (casting_skill == null)
				continue;
			if (!actor.isSkillDisabled(casting_skill) && (member.getAbnormalList() == null || member.getAbnormalList().getEffectBySkillId(casting_skill.getId()) == null))
			{
				actor.getAI().Cast(casting_skill, member);
				if (getActor().phantom_params.getGmLog())
					GmListTable.broadcastToGMs(new SayPacket2(0, ChatType.HERO_VOICE, "sys:", getActor().getName()+"BuffSkillOnPartyMember "+member));
				return true;
			}
		}
		return false;
	}

	private boolean checkReviveAnswer(Player target)
	{
		IntObjectPair<OnAnswerListener> ask = target.getPlayer().getAskListener(false);
		ReviveAnswerListener reviveAsk = ask != null && ask.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener) ask.getValue() : null;
		if (reviveAsk != null)// предложение реса поступило
			return false;
		return true;
	}

	public boolean castResurrectSkill(Player target)
	{
		if (target==null || !target.isDead() || !checkReviveAnswer(target) /*|| target.getAbnormalList().getEffectByStackType("ResurrectBlock") != null*/)
			return false;

		SkillsGroup res = getActor().phantom_params.getClassAI().getResurrectSkills();
		PhantomSkill rnd_skill = res.getRandomSkill();
		if (rnd_skill == null)
			return false;

		Skill casting_skill = getSkill(rnd_skill);
		if (casting_skill != null && !actor.isSkillDisabled(casting_skill))
			return CastSkill(actor, casting_skill, target, true)==0 ? true:false;

		return false;
	}

	public boolean castResurrectSkill()
	{
		if (getActor().isCastingNow() || getActor().getParty() == null)
			return false;

		List <Player> need_res = getActor().getParty().getPartyMembers().stream().filter(m->m != null && m.isDead() && checkReviveAnswer(m)  && getActor().getDistance(m) <= 600).collect(Collectors.toList());
		if (need_res.size() == 0)
			return false;

		Player target = Rnd.get(need_res);
		/*if (target.getAbnormalList().getEffectByStackType("ResurrectBlock") != null)
			return false;*/

		SkillsGroup res = getActor().phantom_params.getClassAI().getResurrectSkills();
		PhantomSkill rnd_skill = res.getRandomSkill();
		if (rnd_skill == null)
			return false;

		Skill casting_skill = getSkill(rnd_skill);
		if (casting_skill != null && !actor.isSkillDisabled(casting_skill))
			return CastSkill(actor, casting_skill, target, true)==0 ? true:false;

		return false;
	}

	public boolean castPartyHealSkill()
	{
		if (getActor().isCastingNow())
			return false;

		SkillsGroup heals = getActor().phantom_params.getClassAI().getPartyHealSkills();
		if (!Rnd.chance(heals.getGroupChanceCast()))
			return false;
		Skill casting_skill = getRandomSkillFromTheList(getActor(),getActor(), heals);

		if (casting_skill != null && !actor.isSkillDisabled(casting_skill))
			return CastSkill(actor, casting_skill, actor, true)==0 ? true:false;

		return false;
	}

	public void castSummonActions(Creature target)
	{
		/*	SummonInstance sum = getActor().getSummonList().getSummon();
		if (sum==null|| sum.isDead() || target== null)
			return;

		SkillsGroup p_action = getActor().phantom_params.getClassAI().getSummonActions();
		if (p_action.getGroupChanceCast()!=100 && !Rnd.chance(p_action.getGroupChanceCast()))
			return;
		if (p_action.getChanceCastToMonster()!=100 && target.isMonster()&& !Rnd.chance(p_action.getChanceCastToMonster()))
			return;

		Skill casting_skill = getRandomSkillFromTheList(sum,target, p_action);

		if (casting_skill != null && !sum.isSkillDisabled(casting_skill))
			CastSkill(sum, casting_skill, target, true);*/
	}

	public boolean castHealSkill(Creature target)
	{
		if (getActor().isCastingNow() || target == null || target.isDead())
			return false;

		if (getActor().phantom_params.getGmLog())
			GmListTable.broadcastToGMs(new SayPacket2(0, ChatType.HERO_VOICE, "sys:", getActor().getName()+" castHealSkill "+target));

		SkillsGroup heals = getActor().phantom_params.getClassAI().getHealSkills();
		Skill casting_skill = getRandomHealSkillFromTheList(target, heals);

		if (casting_skill == null || target == null)
			return false;

		return CastSkill(actor, casting_skill, target, true)==0 ? true:false;
	}

	public boolean castControlSkill(Creature target, int... skill_priority)
	{
		if (getActor().isCastingNow())
			return false;

		if (target!=null && target.isPlayer() && target.getAbnormalList()!=null && target.getAbnormalList().containEffectFromSkills(INVINCIBLE))
			return false;

		SkillsGroup debuffs = getActor().phantom_params.getClassAI().getControlSkill();
		Skill casting_skill = getRandomSkillFromTheList(getActor(), target, debuffs, skill_priority);
		if (casting_skill == null)
			return false;

		return CastSkill(actor, casting_skill, target, true)==0 ? true:false;
	}
	//TODO реализовать
	public boolean castSweperSkill()
	{
		if (getActor().isCastingNow())
			return false;
		Creature target = getActor().phantom_params.getLockedTarget();
		SkillsGroup debuffs = getActor().phantom_params.getClassAI().getSweperSkills();

		Skill casting_skill = getRandomSkillFromTheList(getActor(), target, debuffs);

		if (casting_skill == null)
			return false;

		if (actor.isInRange(target, casting_skill.getAffectRange()) &&target.getAbnormalList()!=null&&  !target.getAbnormalList().containEffectFromSkills(new int[]{casting_skill.getId()}))
			return CastSkill(actor, casting_skill, target, true)==0 ? true:false;

		return false;
	}

	//TODO реализовать
	public boolean castSpoilSkill(Creature target)
	{
		if (getActor().isCastingNow())
			return false;

		SkillsGroup debuffs = getActor().phantom_params.getClassAI().getSpoilSkills();

		Skill casting_skill = getRandomSkillFromTheList(getActor(), target, debuffs);

		if (casting_skill == null)
			return false;

		if (actor.isInRange(target, casting_skill.getAffectRange()) &&target.getAbnormalList()!=null&&  !target.getAbnormalList().containEffectFromSkills(new int[]{casting_skill.getId()}))
			return CastSkill(actor, casting_skill, target, true)==0 ? true:false;

		return false;
	}

	// XXX: castDebuffSkill
	public boolean castDebuffSkill(Creature target, int... skill_priority)
	{
		if (getActor().isCastingNow())
			return false;
		if (target!=null && target.getAbnormalList() !=null && target.getAbnormalList().containEffectFromSkills(DEBUFF_RESIST))
			return false;
		SkillsGroup debuffs = getActor().phantom_params.getClassAI().getDebuffs();
		if (!Rnd.chance(debuffs.getGroupChanceCast()))
			return false;

		Skill casting_skill = getRandomSkillFromTheList(getActor(), target, debuffs, skill_priority);

		if (casting_skill == null)
			return false;

		if (actor.isInRange(target, casting_skill.getAffectRange()) &&target.getAbnormalList()!=null&&  !target.getAbnormalList().containEffectFromSkills(new int[]{casting_skill.getId()}))
			return CastSkill(actor, casting_skill, target, true)==0 ? true:false;

		return false;
	}

	// XXX исправить
	public boolean castSummonSkill()
	{
		if (getActor().isCastingNow())
			return false;

		if (actor.getFirstServitor() == null)
		{
			SkillsGroup summon = getActor().phantom_params.getClassAI().getSummonSkills();
			Skill casting_skill = getRandomSkillFromTheList(getActor(),getActor(), summon);
			return CastSkill(actor, casting_skill, actor, true) ==0 ? true:false;
		}
		return false;
	}

	public void Summonbuffs(Summon pet)
	{
		if (getActor().getOlympiadGame() !=null)
			return ;

		/*	for(int[] buff : Config.PHANTOM_TOP_BUFF)
		{
			Skill skill = SkillHolder.getInstance().getSkillEntry(buff[0], buff[1]);
			if (skill == null)
				continue;
			for(EffectTemplate et : skill.getEffectTemplates())
			{
				Env env = new Env(pet, pet, skill);
				Effect effect = et.getEffect(env);
				effect.setPeriod(Config.PHANTOM_BUFF_TIME);
				pet.getAbnormalList().addEffect(effect);
			}
		}*/
	}
	// XXX: SituationSkill
	public boolean castSituationSkill(Creature target)
	{
		if (getActor().isCastingNow())
			return false;
		SkillsGroup situation = getActor().phantom_params.getClassAI().getSituationSkills();
		Skill casting_skill = getRandomSkillFromTheList(getActor(),target, situation);
		if (getActor().phantom_params.getGmLog())
			GmListTable.broadcastToGMs(new SayPacket2(0, ChatType.HERO_VOICE, "SYS:", getActor().getName()+"\n"+"castSituationSkill " +casting_skill));
		return CastSkill(actor, casting_skill, target, true) ==0 ? true:false;
	}

	// XXX: CastSkill
	public int CastSkill(Creature activeChar, Skill skill, GameObject target, boolean forceUse)
	{
		if (activeChar == null)
			return 1;
		if (activeChar.isCastingNow())
			return 2;

		activeChar.getPlayer().setActive();

		if (skill != null)
		{
			if (activeChar.isOutOfControl() || activeChar.isStunned() || activeChar.isSleeping() || activeChar.isParalyzed())
				return 3;
			if (activeChar.isSkillDisabled(skill))
				return 4;
			if(activeChar.isPlayer())
			{
				FlagItemAttachment attachment = activeChar.getPlayer().getActiveWeaponFlagAttachment();
				if (attachment != null && !attachment.canCast(activeChar.getPlayer(), skill))
					return 5;

				// В режиме трансформации доступны только скилы трансформы
				//	if (activeChar.getPlayer().isTransformed() && !activeChar.getAllSkills().contains(skill))
				//return 6;
			}
			if (skill.isToggle())
				if (activeChar.getAbnormalList().containsEffects(skill))
				{
					activeChar.getAbnormalList().stopEffects(skill.getId());
					return 7;
				}

			Creature new_target = getAimingTarget(skill, activeChar, target);
			if (new_target == null)
				return 8;

			if (skill.isNotTargetAoE() && !activeChar.isInRange(new_target, skill.getAffectRange()))
				return 9;

			/*	if (skill.getTargetType() == SkillTargetType.TARGET_ONE && activeChar.getAbnormalList().containsEffects(EffectType.Aggression))// не селф умение 
			{
				List <Effect> tmp = activeChar.getAbnormalList().getEffectsByType(EffectType.Aggression);
				for (Effect ef:tmp)
					if (ef.getEffector() !=new_target)
						return 10;
			}*/
			if (skill.getId()!=246 && !activeChar.isMoving() && !activeChar.isFollowing() && activeChar != new_target && skill.getTargetType() != SkillTargetType.TARGET_SELF && !activeChar.isInRange(new_target, skill.getCastRange()))
			{

				double dist = activeChar.getDistance(new_target); // дистанция
				int cast_dist = skill.getCastRange(); // дистанция каста
				if (cast_dist > 0)
				{
					double diff = dist-cast_dist;
					double λ = 0;
					if (diff > 0)
					{
						if (diff > cast_dist)
						{
							λ = diff/cast_dist;
						}
						else
						{
							λ = cast_dist/diff;
						}

						double posX = (activeChar.getX()+λ*new_target.getX())/(1+λ);
						double posY = (activeChar.getY()+λ*new_target.getY())/(1+λ);

						activeChar.moveToLocation(new Location((int) posX, (int) posY, activeChar.getZ()), 0, true);
					}
				}
				return 11;
			}
			if (activeChar != new_target && new_target.getAbnormalList()!=null&& new_target.getAbnormalList().containEffectFromSkills(DEBUFF_RESIST))
				return 12;

			if (activeChar.isPlayer())
				activeChar.getPlayer().setGroundSkillLoc(null);

			if (skill.getId()!=3318)
				activeChar.setTarget(new_target);

			((Player) activeChar).getAI().Cast(skill, new_target, forceUse, false);

			if (skill.getId()!=3318 && skill.getReuseDelay() < getActor().phantom_params.getRndDelayAi() + Config.PHANTOM_AI_DELAY)//если кд меньше чем тик запланируем повторный каст
				activeChar.getAI().setNextAction(PlayableAI.AINextAction.CAST, skill, target, forceUse, false);

			return 0;
		}
		return 13;
	}

	public void clearPartyAssist()
	{
		// если бот не в пати, то ничего делать не нужно
		if (getActor().phantom_params.getPhantomPartyAI() == null)
			return;

		getActor().phantom_params.getPhantomPartyAI().clearAssists();
	}

	private Creature getAimingTarget(Skill p_skill, Creature activeChar, GameObject obj)
	{
		Creature target = obj == null || !obj.isCreature() ? null : (Creature) obj;
		switch(p_skill.getTargetType())
		{
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_PARTY:
			case TARGET_PARTY_WITHOUT_ME:
			case TARGET_CLAN_ONLY:
			case TARGET_SELF:
			case TARGET_FACTION: {
				return activeChar;
			}
			case TARGET_AURA:
			case TARGET_COMMCHANNEL:
			case TARGET_MULTIFACE_AURA:
			case TARGET_GROUND:
			{
				return activeChar;
			}
			case TARGET_HOLY:
			{
				return target != null && activeChar.isPlayer() && target.isArtefact() ? target : null;
			}
			case TARGET_FLAGPOLE:
			{
				if((obj instanceof StaticObjectInstance) && ((StaticObjectInstance) obj).getType() == 3)
					return activeChar;
				else if(target != null && activeChar.isPlayer() && target.isArtefact())
					return target;
				//else if(target != null && activeChar.isPlayer() && target instanceof ArtifactInstance)
				//return target;
				else if(target != null && activeChar.isPlayer() && target instanceof UpgradingArtifactInstance)
					return target;
				else
					return null;
			}
			case TARGET_UNLOCKABLE:
			{
				return target != null && target.isDoor() || target instanceof ChestInstance ? target : null;
			}
			case TARGET_CHEST:
			{
				return target instanceof ChestInstance ? target : null;
			}
			case TARGET_SERVITORS:
			case TARGET_SUMMONS:
			case TARGET_SELF_AND_SUMMONS:
			{
				return activeChar;
			}
			case TARGET_ONE_SERVITOR:
			case TARGET_SERVITOR_AURA:
			{
				return target != null && target.isServitor() && activeChar.isMyServitor(target.getObjectId()) && target.isDead() == p_skill.getCorpse() ? target : null;
			}
			case TARGET_ONE_SERVITOR_NO_TARGET:
			{
				target = activeChar.getPlayer().getAnyServitor();
				return target != null && target.isDead() == p_skill.getCorpse() ? target : null;
			}
			case TARGET_ONE_SUMMON:
			{
				return target != null && target.isSummon() && activeChar.isMyServitor(target.getObjectId()) && target.isDead() == p_skill.getCorpse() ? target : null;
			}
			case TARGET_ONE_SUMMON_NO_TARGET:
			{
				target = activeChar.getPlayer().getAnySummon();
				return target != null && target.isDead() == p_skill.getCorpse() ? target : null;
			}
			case TARGET_PET:
			{
				target = activeChar.isPlayer() ? activeChar.getPlayer().getPet() : null;
				return target != null && target.isPet() && target.isDead() == p_skill.getCorpse() ? target : null;
			}
			case TARGET_OWNER:
			{
				if(activeChar.isServitor())
				{
					target = activeChar.getPlayer();
					return target != null && target.isDead() == p_skill.getCorpse() ? target : null;
				}
				return null;
			}
			case TARGET_ENEMY_PET:
			{
				if(target == null || activeChar.isMyServitor(target.getObjectId()) || !target.isPet())
					return null;
				return target;
			}
			case TARGET_ENEMY_SUMMON:
			{
				if(target == null || activeChar.isMyServitor(target.getObjectId()) || !target.isSummon())
					return null;
				return target;
			}
			case TARGET_ENEMY_SERVITOR:
			{
				if(target == null || activeChar.isMyServitor(target.getObjectId()) || !target.isServitor())
					return null;
				return target;
			}
			case TARGET_ONE:
			{
				return target != null && target.isDead() == p_skill.getCorpse() && (target != activeChar || !p_skill.isOffensive()) && (!p_skill._isUndeadOnly || target.isUndead()) ? target : null;
			}
			case TARGET_CLAN_ONE:
			{
				if(target == null)
					return null;
				Player cplayer = activeChar.getPlayer();
				Player cptarget = target.getPlayer();
				if(cptarget != null && cptarget == activeChar)
					return target;

				if(!(cplayer == null || !cplayer.isInOlympiadMode() || cptarget == null || cplayer.getOlympiadSide() != cptarget.getOlympiadSide() || cplayer.getOlympiadGame() != cptarget.getOlympiadGame() || target.isDead() != p_skill.getCorpse() || target == activeChar && p_skill.isOffensive() || p_skill._isUndeadOnly && !target.isUndead()))
					return target;

				if(cptarget != null && cplayer != null && cplayer.getClan() != null && cplayer.isInSameClan(cptarget) && target.isDead() == p_skill.getCorpse() && (target != activeChar || !p_skill.isOffensive()) && (!p_skill._isUndeadOnly || target.isUndead()))
					return target;
				return null;
			}
			case TARGET_PARTY_ONE:
			{
				if(target == null)
					return null;
				Player player = activeChar.getPlayer();
				Player ptarget = target.getPlayer();
				if(ptarget != null && ptarget == activeChar)
					return target;

				if(!(player == null || !player.isInOlympiadMode() || ptarget == null || player.getOlympiadSide() != ptarget.getOlympiadSide() || player.getOlympiadGame() != ptarget.getOlympiadGame() || target.isDead() != p_skill.getCorpse() || target == activeChar && p_skill.isOffensive() || p_skill._isUndeadOnly && !target.isUndead()))
					return target;

				if(ptarget != null && player != null && player.getParty() != null && player.getParty().containsMember(ptarget) && target.isDead() == p_skill.getCorpse() && (target != activeChar || !p_skill.isOffensive()) && (!p_skill._isUndeadOnly || target.isUndead()))
					return target;
				return null;
			}
			case TARGET_PARTY_ONE_WITHOUT_ME:
			{
				if(target == null)
					return null;
				Player player = activeChar.getPlayer();
				Player ptarget = target.getPlayer();
				if(ptarget != null && ptarget == activeChar)
					return null;

				if(!(player == null || !player.isInOlympiadMode() || ptarget == null || player.getOlympiadSide() != ptarget.getOlympiadSide() || player.getOlympiadGame() != ptarget.getOlympiadGame() || target.isDead() != p_skill.getCorpse() || target == activeChar && p_skill.isOffensive() || p_skill._isUndeadOnly && !target.isUndead()))
					return target;

				if(ptarget != null && player != null && player.getParty() != null && player.getParty().containsMember(ptarget) && target.isDead() == p_skill.getCorpse() && (target != activeChar || !p_skill.isOffensive()) && (!p_skill._isUndeadOnly || target.isUndead()))
					return target;
				return null;
			}
			case TARGET_AREA:
			case TARGET_MULTIFACE:
			case TARGET_TUNNEL:
			{
				return target != null && target.isDead() == p_skill.getCorpse() && (target != activeChar || !p_skill.isOffensive()) && (!p_skill._isUndeadOnly || target.isUndead()) ? target : null;
			}
			case TARGET_AREA_AIM_CORPSE:
			{
				return target != null && target.isDead() ? target : null;
			}
			case TARGET_CORPSE:
			{
				if(target == null || !target.isDead())
					return null;
				if(target.isSummon() && !activeChar.isMyServitor(target.getObjectId()))
					return target;
				return target.isNpc() ? target : null;
			}
			case TARGET_CORPSE_PLAYER:
			{
				return target != null && target.isPlayable() && target.isDead() ? target : null;
			}
			case TARGET_SIEGE:
			{
				return target != null && !target.isDead() && target.isDoor() ? target : null;
			}
			default:
			{
				activeChar.sendMessage("Target type of skill is not currently handled");
				return null;
			}
		}
	}

	public void teleToClosestTown()
	{
		//GmListTable.broadcastMessageToGMs("teleToClosestTown " + getActor());
		if(!getActor().getAroundPlayers(50, 100).isEmpty()) // если стоим в толпе - запустим такс и отойдем всторонку 
		{
			ThreadPoolManager.getInstance().PhantomOtherSchedule(new MoveToFreePointTask(getActor()), Rnd.get(1, 3) * 1000);
		}
		if (!getActor().phantom_params.getMoveToGkTask())
		{
			getActor().phantom_params.initMoveToGkTask(Rnd.get(4, 5) * 1000);
		}
	}
}

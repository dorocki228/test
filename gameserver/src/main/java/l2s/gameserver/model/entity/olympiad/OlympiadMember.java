package l2s.gameserver.model.entity.olympiad;

import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.model.*;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.ExOlympiadModePacket;
import l2s.gameserver.network.l2.s2c.RevivePacket;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.TimeStamp;
import l2s.gameserver.templates.InstantZone;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.TeleportUtils;
import l2s.gameserver.utils.Util;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OlympiadMember
{
	private static final Logger _log = LoggerFactory.getLogger(OlympiadMember.class);
	private String _name = "";
	private String _clanName = "";
	private int _classId;
	private double _damage;
	private final int _objId;
	private final OlympiadGame _game;
	private final CompType _type;
	private final int _side;
	private Player _player;
	private Location _returnLoc = null;

	public OlympiadMember(int obj_id, OlympiadGame game, int side)
	{
		String player_name = "";
		Player player = GameObjectsStorage.getPlayer(obj_id);
		if(player != null)
			player_name = player.getName();
		else
		{
			String name = Olympiad.getParticipantName(obj_id);
			if(name != null)
				player_name = name;
		}

		_objId = obj_id;
		_name = player_name;
		_game = game;
		_type = game.getType();
		_side = side;
		_player = player;

		if(_player == null)
			return;

		_clanName = player.getClan() == null ? "" : player.getClan().getName();
		_classId = player.getActiveClassId();

		player.setOlympiadSide(side);
		player.setOlympiadGame(game);
	}

	public OlympiadParticipiantData getStat()
	{
		return Olympiad.getParticipantInfo(_objId);
	}

	public void incGameCount()
	{
		OlympiadParticipiantData data = getStat();
		switch(_type)
		{
			case CLASSED:
			{
				data.setClassedGamesCount(data.getClassedGamesCount() + 1);
				break;
			}
			case NON_CLASSED:
			{
				data.setNonClassedGamesCount(data.getNonClassedGamesCount() + 1);
			}
		}
	}

	public void takePointsForCrash()
	{
		if(!checkPlayer())
		{
			OlympiadParticipiantData data = getStat();

			int points = data.getPoints();
			int diff = Math.min(10, points / _type.getLooseMult());

			data.setPoints(points - diff);

			String messagePattern = "Olympiad Result: {} lost {} points for crash";
			ParameterizedMessage message = new ParameterizedMessage(messagePattern, _name, diff);
			LogService.getInstance().log(LoggerType.OLYMPIAD, message);

			Player player = _player;
			if(player == null)
			{
				messagePattern = "Olympiad info: {} crashed coz player == null";
				message = new ParameterizedMessage(messagePattern, _name);
				LogService.getInstance().log(LoggerType.OLYMPIAD, message);
			}
			else
			{
				if(player.isLogoutStarted())
				{
					messagePattern = "Olympiad info:{} crashed coz player.isLogoutStarted()";
					message = new ParameterizedMessage(messagePattern, _name);
					LogService.getInstance().log(LoggerType.OLYMPIAD, message);
				}

				if(!player.isConnected())
				{
					messagePattern = "Olympiad info: {} crashed coz !player.isOnline()";
					message = new ParameterizedMessage(messagePattern, _name);
					LogService.getInstance().log(LoggerType.OLYMPIAD, message);
				}

				if(player.getOlympiadGame() == null)
				{
					messagePattern = "Olympiad info: {} crashed coz player.getOlympiadGame() == null";
					message = new ParameterizedMessage(messagePattern, _name);
					LogService.getInstance().log(LoggerType.OLYMPIAD, message);
				}

				if(player.isInArenaObserverMode())
				{
					messagePattern = "Olympiad info: {} crashed coz player.isInArenaObserverMode()";
					message = new ParameterizedMessage(messagePattern, _name);
					LogService.getInstance().log(LoggerType.OLYMPIAD, message);
				}
			}
		}
	}

	public boolean checkPlayer()
	{
		Player player = _player;
		if(player == null || player.isLogoutStarted() || player.getOlympiadGame() == null || player.isInObserverMode())
			return false;
		return true;
	}

	public void sendInfoAbout(OlympiadMember member) {
		Player memberPlayer = member.getPlayer();
		if(memberPlayer == null)
			return;
		final String className = Util.className(_player, member.getClassId());
		_player.sendMessage(new CustomMessage("olympiad.className").addString(member.getName()).addNumber(memberPlayer.getLevel()).addString(className));
	}

	public void portPlayerToArena()
	{
		Player player = _player;
		if(!checkPlayer() || player.isTeleporting())
		{
			_player = null;
			return;
		}

		DuelEvent duel = player.getEvent(DuelEvent.class);
		if(duel != null)
			duel.abortDuel(player);

		_returnLoc = player.getStablePoint() == null ? player.getReflection().getReturnLoc() == null ? player.getLoc() : player.getReflection().getReturnLoc() : player.getStablePoint();

		if(player.isDead())
			player.setPendingRevive(true);

		if(player.isSitting())
			player.standUp();

        if(player.isMounted())
            player.setMount(null);

		player.setTarget(null);
		player.setIsInOlympiadMode(true);
		player.getInventory().validateItems();
		player.leaveParty();

		Reflection ref = _game.getReflection();
		InstantZone instantZone = ref.getInstancedZone();
		Location tele = Location.findPointToStay(instantZone.getTeleportCoords().get(_side - 1), 50, 50, ref.getGeoIndex());

		player.setStablePoint(_returnLoc);
		player.teleToLocation(tele, ref);

		player.sendPacket(new ExOlympiadModePacket(_side));
	}

	public void portPlayerBack()
	{
		Player player = _player;
		if(player == null)
			return;

		player.setIsInOlympiadMode(false);
		player.setOlympiadSide(-1);
		player.setOlympiadGame(null);

		for(Abnormal e : player.getAbnormalList().getEffects())
		{
			if(player.isSpecialEffect(e.getSkill()) || e.getEffectType() == EffectType.Cubic && player.getSkillLevel(e.getSkill().getId()) > 0)
				continue;
			e.exit();
		}

		List<SummonInstance> summons = player.getSummons();

		if(!summons.isEmpty())
			for(Servitor summon : summons)
				summon.getAbnormalList().stopAllEffects();

		player.setCurrentCp(player.getMaxCp());
		player.setCurrentMp(player.getMaxMp());

		if(player.isDead())
		{
			player.setCurrentHp(player.getMaxHp(), true);
			player.broadcastPacket(new RevivePacket(player));
		}
		else
			player.setCurrentHp(player.getMaxHp(), false);

		if(player.getClan() != null && player.getClan().getReputationScore() >= 0)
			player.getClan().enableSkills(player);

		player.activateHeroSkills(true);

		player.sendSkillList();

		player.sendPacket(new ExOlympiadModePacket(0));

		player.setStablePoint(null);
		player.setReflection(ReflectionManager.MAIN);

		if(_returnLoc == null) {
			final TeleportPoint teleportPoint = TeleportUtils.getRestartPoint(player, RestartType.TO_VILLAGE);
			_returnLoc = teleportPoint.getLoc();
		}

		player.teleToLocation(_returnLoc, ReflectionManager.MAIN);
	}

	public void preparePlayer()
	{
		Player player = _player;
		if(player == null)
			return;

		if(player.isInObserverMode())
			player.leaveObserverMode();

		if(player.getClan() != null)
			player.getClan().disableSkills(player);

		player.activateHeroSkills(false);

		if(player.isCastingNow())
			player.abortCast(true, true);

		if(player.isAttackingNow())
			player.abortAttack(true, true);

		for(Abnormal e : player.getAbnormalList().getEffects())
		{
			if(player.isSpecialEffect(e.getSkill()) || e.getEffectType() == EffectType.Cubic && player.getSkillLevel(e.getSkill().getId()) > 0)
				continue;
			e.exit();
		}

		List<SummonInstance> summons = player.getSummons();
		if(!summons.isEmpty())
		{
			for(Servitor servitor : summons)
			{
				if(servitor.isPet())
				{
					servitor.unSummon(false);
					continue;
				}
				servitor.getAbnormalList().stopAllEffects();
				servitor.transferOwnerBuffs();
			}
		}

		if(player.getAgathionId() > 0)
			player.setAgathion(0);

		for(TimeStamp sts : player.getSkillReuses())
		{
			Skill skill;
			if(sts == null || (skill = SkillHolder.getInstance().getSkill(sts.getId(), sts.getLevel())) == null || (long) skill.getReuseDelay() > 900000)
				continue;
			player.enableSkill(skill);
		}

		player.sendSkillList();
		player.getInventory().validateItems();
		player.removeAutoShots(true);

		heal();
	}

	public void heal()
	{
		Player player = _player;
		if(player == null)
			return;

		player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
		player.setCurrentCp(player.getMaxCp());
		player.broadcastUserInfo(true);
	}

	public void saveParticipantData()
	{
		OlympiadDatabase.saveParticipantData(_objId);
	}

	public void logout()
	{
		_player = null;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public String getName()
	{
		return _name;
	}

	public void addDamage(double d)
	{
		_damage += d;
	}

	public double getDamage()
	{
		return _damage;
	}

	public String getClanName()
	{
		return _clanName;
	}

	public int getClassId()
	{
		return _classId;
	}

	public int getObjectId()
	{
		return _objId;
	}

	@Override
	public String toString()
	{
		return _player.toString();
	}
}

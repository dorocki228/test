package l2s.gameserver.skills.skillclasses;

import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.*;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.instances.*;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.funcs.FuncAdd;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Summon extends Skill
{
	private static final Logger _log = LoggerFactory.getLogger(Summon.class);

	private static final int DEFAULT_LIFE_TIME = 2000000;
	private final SummonType _summonType;
	private final double _expPenalty;
	private final int _itemConsumeIdInTime;
	private final int _itemConsumeCountInTime;
	private final int _itemConsumeDelay;
	private final int _lifeTime;
	private final int _summonPoints;
	private final int _summonsCount;
	private final boolean _isSaveableSummon;
	private final boolean _randomOffset;

	public Summon(StatsSet set)
	{
		super(set);
		_summonType = Enum.valueOf(SummonType.class, set.getString("summonType", "PET").toUpperCase());
		_expPenalty = set.getDouble("expPenalty", 0.0);
		_itemConsumeIdInTime = set.getInteger("itemConsumeIdInTime", 0);
		_itemConsumeCountInTime = set.getInteger("itemConsumeCountInTime", 0);
		_itemConsumeDelay = set.getInteger("itemConsumeDelay", 240) * 1000;
		_lifeTime = set.getInteger("lifeTime", _summonType == SummonType.NPC || _summonType == SummonType.SYMBOL || _summonType == SummonType.GROUND_ZONE ? -1 : DEFAULT_LIFE_TIME) * 1000;
		_summonPoints = Math.max(set.getInteger("summon_points", 0), 0);
		_summonsCount = Math.max(set.getInteger("summon_count", 1), 1);
		_isSaveableSummon = set.getBool("is_saveable_summon", true);
		_randomOffset = set.getBool("random_offset_on_spawn", _summonType == SummonType.CLONE);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
			return false;
		Player player = activeChar.getPlayer();
		if(player == null)
			return false;
		if(player.isProcessingRequest())
		{
			player.sendPacket(SystemMsg.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
			return false;
		}
		switch(_summonType)
		{
			case TRAP:
			case CLONE:
			case GROUND_ZONE:
			{
				if(player.isInPeaceZone() && isOffensive())
				{
					player.sendPacket(SystemMsg.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_A_PEACE_ZONE);
					return false;
				}
				break;
			}
			case PET:
			{
				if(!checkSummonCondition(player))
				{
					player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}

				break;
			}
			case SIEGE_SUMMON:
			{
				if(!checkSummonCondition(player))
				{
					player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}

				if(player.isInZone(Zone.ZoneType.block_siege_summon))
				{
					player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}

				break;
			}
			case AGATHION:
			{
				if(player.getAgathionId() > 0 && getNpcId() != 0)
				{
					player.sendPacket(SystemMsg.AN_AGATHION_HAS_ALREADY_BEEN_SUMMONED);
					return false;
				}
				break;
			}
			case SYMBOL:
			{
				if(player.getSymbol() != null)
				{
					player.sendPacket(new SystemMessagePacket(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addSkillName(this));
					return false;
				}
				break;
			}
		}
		return true;
	}

	@Override
	public void onEndCast(Creature caster, List<Creature> targets)
	{
		super.onEndCast(caster, targets);
		Player activeChar = caster.getPlayer();
		if(activeChar == null)
			return;
		switch(_summonType)
		{
			case AGATHION:
			{
				activeChar.setAgathion(getNpcId());
				break;
			}
			case TRAP:
			{
				SkillEntry trapSkillEntry = getFirstAddedSkill();
				List<TrapInstance> traps = activeChar.getPlayer().getTraps();
				if(!traps.isEmpty())
					traps.get(0).deleteMe();
				TrapInstance trap = new TrapInstance(IdFactory.getInstance().getNextId(), NpcHolder.getInstance().getTemplate(getNpcId()), activeChar, trapSkillEntry.getTemplate());
				activeChar.addTrap(trap);
				trap.spawnMe();
				break;
			}
			case CLONE:
			{
				for(int i = 0; i < _summonsCount; ++i)
				{
					FakePlayer fp = new FakePlayer(IdFactory.getInstance().getNextId(), activeChar.getTemplate(), activeChar);
					fp.setReflection(activeChar.getReflection());
					if(_randomOffset)
						fp.spawnMe(Location.findAroundPosition(activeChar, (int) (50.0 + fp.getColRadius()), (int) (70.0 + fp.getColRadius())));
					else
						fp.spawnMe(activeChar.getLoc());
					fp.setFollowMode(true);
				}
				break;
			}
			case PET:
			case SIEGE_SUMMON:
			{
				summon(activeChar, targets, null);
				break;
			}
			case NPC:
			{
				if(activeChar.hasSummon() || activeChar.isMounted())
					return;
				NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(getNpcId());
				NpcInstance npc = npcTemplate.getNewInstance();
				npc.setCurrentHp(npc.getMaxHp(), false);
				npc.setCurrentMp(npc.getMaxMp());
				npc.setHeading(activeChar.getHeading());
				npc.setReflection(activeChar.getReflection());
				npc.setOwner(activeChar);
				if(_randomOffset)
					npc.spawnMe(Location.findAroundPosition(activeChar, (int) (40.0 + npc.getColRadius()), (int) (40.0 + npc.getColRadius())));
				else
					npc.spawnMe(activeChar.getLoc());
				if(_lifeTime > 0)
				{
					npc.startDeleteTask(_lifeTime);
					break;
				}
				break;
			}
			case SYMBOL:
			{
				SymbolInstance symbol = activeChar.getSymbol();
				if(symbol != null)
				{
					activeChar.setSymbol(null);
					symbol.deleteMe();
				}
				NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(getNpcId());
				NpcInstance npc = npcTemplate.getNewInstance();
				npc.setCurrentHp(npc.getMaxHp(), false);
				npc.setCurrentMp(npc.getMaxMp());
				npc.setHeading(activeChar.getHeading());
				if(npc instanceof SymbolInstance)
				{
					symbol = (SymbolInstance) npc;
					activeChar.setSymbol(symbol);
					symbol.setOwner(activeChar);
				}
				Location loc = Location.findPointToStay(activeChar.getLoc(), 50, 100, activeChar.getReflection().getGeoIndex());
				if(activeChar.getGroundSkillLoc() != null)
				{
					loc = activeChar.getGroundSkillLoc();
					activeChar.setGroundSkillLoc(null);
				}
				npc.setReflection(activeChar.getReflection());
				npc.spawnMe(loc);
				if(_lifeTime > 0)
				{
					npc.startDeleteTask(_lifeTime);
					break;
				}
				break;
			}
			case GROUND_ZONE:
			{
				if(activeChar.isMounted())
					return;
				NpcTemplate npcTemplate = NpcHolder.getInstance().getTemplate(getNpcId());
				NpcInstance npc = npcTemplate.getNewInstance();
				npc.setCurrentHp(npc.getMaxHp(), false);
				npc.setCurrentMp(npc.getMaxMp());
				npc.setHeading(activeChar.getHeading());
				if(npc instanceof SymbolInstance)
					npc.setOwner(activeChar);
				Location loc2 = activeChar.getLoc();
				if(activeChar.getGroundSkillLoc() != null)
				{
					loc2 = activeChar.getGroundSkillLoc();
					activeChar.setGroundSkillLoc(null);
				}
				npc.setReflection(activeChar.getReflection());
				npc.spawnMe(loc2);
				if(_lifeTime > 0)
				{
					npc.startDeleteTask(_lifeTime);
					break;
				}
				break;
			}
		}
	}

	@Override
	public boolean isOffensive()
	{
		return getTargetType() == SkillTargetType.TARGET_CORPSE;
	}

	private boolean checkSummonCondition(Player player)
	{
		//		final int summonsCount = player.getSummonsCount();
		//		if(summonsCount >= 4)
		//			return false;
		//		if(_summonPoints == 0 && summonsCount > 0)
		//			return false;
		//		final int availPoints = player.getAvailableSummonPoints();
		//		return _summonPoints <= availPoints && (_summonPoints <= 0 || availPoints != player.getMaxSummonPoints() || summonsCount <= 0);
		return true;
	}

	public void summon(Player player, List<Creature> targets, SummonInstance.RestoredSummon restored)
	{
		Location loc = null;
		if(restored == null)
		{
			if(getTargetType() == SkillTargetType.TARGET_CORPSE)
				for(Creature target : targets)
					if(target != null && target.isDead())
					{
						player.getAI().setAttackTarget(null);
						loc = target.getLoc();
						if(target.isNpc())
							((NpcInstance) target).endDecayTask();
						else
						{
							if(!target.isSummon())
								return;
							((SummonInstance) target).endDecayTask();
						}
					}
			for(SummonInstance summon : player.getSummons())
				summon.unSummon(false);

			//				if(summon.getNpcId() == getNpcId() && summon.isUnSummonExisting())
			//				{
			//					summon.unSummon(false);
			//					break;
			//				}
		}
		else if(player.getSkillLevel(restored.skillId, 0) < restored.skillLvl)
			return;
		if(!checkSummonCondition(player))
			return;
		NpcTemplate summonTemplate = NpcHolder.getInstance().getTemplate(getNpcId());
		if(summonTemplate == null)
		{
			_log.warn("Summon: Template ID " + getNpcId() + " is NULL FIX IT!");
			return;
		}
		SummonInstance summon;

		//FIXME перенести в склклассы
		if(getNpcId() != 14768)
			summon = new SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, player, _lifeTime, _itemConsumeIdInTime, _itemConsumeCountInTime, _itemConsumeDelay, _summonPoints, this, _isSaveableSummon);
		else
			summon = new WildHohCannonInstance(IdFactory.getInstance().getNextId(), summonTemplate, player, _lifeTime, _itemConsumeIdInTime, _itemConsumeCountInTime, _itemConsumeDelay, _summonPoints, this, _isSaveableSummon);

		player.addSummon(summon);
		summon.setTitle("%OWNER_NAME%");
		summon.setExpPenalty(_expPenalty);
		summon.setExp(Experience.LEVEL[Math.min(summon.getLevel(), Experience.LEVEL.length - 1)]);
		summon.setHeading(player.getHeading());
		summon.setReflection(player.getReflection());
		summon.spawnMe(loc == null ? Location.findAroundPosition(player, 50, 70) : loc);
		summon.setRunning();
		summon.setFollowMode(true);
		if(summon.getSkillLevel(4140) > 0)
			summon.altUseSkill(SkillHolder.getInstance().getSkill(4140, summon.getSkillLevel(4140)), player);
		if("Shadow".equalsIgnoreCase(summon.getName()))
			summon.addStatFunc(new FuncAdd(Stats.ABSORB_DAMAGE_PERCENT, 64, this, 15.0));
		if(restored == null)
			summon.setCurrentHpMp(summon.getMaxHp(), summon.getMaxMp(), false);
		else
		{
			summon.setCurrentHpMp(restored.curHp, restored.curMp, false);
			summon.setConsumeCountdown(restored.time);
		}
		if(_summonType == SummonType.SIEGE_SUMMON)
		{
			summon.setSiegeSummon(true);
			for(SiegeEvent<?, ?> siegeEvent : player.getEvents(SiegeEvent.class))
				siegeEvent.addSiegeSummon(player, summon);
		}
		player.getListeners().onSummonServitor(summon);
	}

	private enum SummonType
	{
		PET,
		SIEGE_SUMMON,
		AGATHION,
		TRAP,
		NPC,
		SYMBOL,
		CLONE,
		GROUND_ZONE
    }
}

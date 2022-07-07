package l2s.gameserver.model;

import l2s.commons.lang.reference.HardReference;
import l2s.commons.util.Rnd;
import l2s.commons.util.concurrent.atomic.AtomicState;
import l2s.gameserver.Config;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.actor.basestats.PlayableBaseStats;
import l2s.gameserver.model.entity.boat.Boat;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.StaticObjectInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo.MagicAttackType;
import l2s.gameserver.permission.ActionPermissionComponent;
import l2s.gameserver.permission.EActionPermissionLevel;
import l2s.gameserver.permission.interfaces.IAttackPermission;
import l2s.gameserver.permission.interfaces.IIncomingAttackPermission;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.TimeStamp;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.CreatureTemplate;
import l2s.gameserver.templates.item.EtcItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.utils.Location;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class Playable extends Creature {
    private final AtomicState _isSilentMoving;
    private boolean _isPendingRevive;
    protected final ReadWriteLock questLock;
    protected final Lock questRead;
    protected final Lock questWrite;
    protected final Map<Integer, TimeStamp> _sharedGroupReuses;
    private Boat _boat;
    private Location _inBoatPosition;
    private long _nonAggroTime;
    private long _nonPvpTime;
	private final int[] EPIC_BOSSES = { 29228, 29001, 29014, 29006, 29046, 29020, 29019, 29066, 29028 };

	public Playable(int objectId, CreatureTemplate template)
	{
		super(objectId, template);
		_isSilentMoving = new AtomicState();
		questLock = new ReentrantReadWriteLock();
		questRead = questLock.readLock();
		questWrite = questLock.writeLock();
		_sharedGroupReuses = new ConcurrentHashMap<>();
	}

	@Override
	public HardReference<? extends Playable> getRef()
	{
		return (HardReference<? extends Playable>) super.getRef();
	}

	public abstract Inventory getInventory();

	public abstract long getWearedMask();

	@Override
	public boolean checkPvP(Creature target, Skill skill)
	{
		Player player = getPlayer();
		if(isDead() || target == null || player == null || target == this || target == player || player.isMyServitor(target.getObjectId()) || player.isPK())
			return false;
		if(skill != null)
		{
			if(skill.altUse())
				return false;
			if(skill.getTargetType() == Skill.SkillTargetType.TARGET_UNLOCKABLE)
				return false;
			if(skill.getTargetType() == Skill.SkillTargetType.TARGET_CHEST)
				return false;
		}
		List<SingleMatchEvent> events = getEvents(SingleMatchEvent.class);
		for(SingleMatchEvent event : events)
			if(!event.checkPvPFlag(target))
				return false;
		if(isInPeaceZone() && target.isInPeaceZone())
			return false;
		if(isInZoneBattle() && target.isInZoneBattle())
			return false;
		if(isInSiegeZone() && target.isInSiegeZone())
			return false;
//		if(skill == null || skill.isOffensive())
//		{
//			if(target.isRaid() && !ArrayUtils.contains(EPIC_BOSSES, target.getNpcId()))
//				return true;
//		}
		
		if(target.getPvpFlag() > 0 || target.isPK())
			return true;
		return false;
	}

	public boolean checkTarget(Creature target)
	{
		Player player = getPlayer();
		if(player == null)
			return false;
		if(target == null || target.isDead())
		{
			player.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}
		if(!isInRange(target, 2000L))
		{
			player.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}
		if(!target.isAttackable(this))
		{
			player.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}
		if(target.paralizeOnAttack(this))
		{
			if(Config.PARALIZE_ON_RAID_DIFF)
				paralizeMe(target);
			return false;
		}
		if(target.isInvisible(this) || getReflection() != target.getReflection() || !GeoEngine.canSeeTarget(this, target, false))
		{
			player.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
			return false;
		}
		if(player.isInZone(Zone.ZoneType.epic) != target.isInZone(Zone.ZoneType.epic))
		{
			player.sendPacket(SystemMsg.INVALID_TARGET);
			return false;
		}
		if(target.isPlayable())
		{
			if(isInZoneBattle() != target.isInZoneBattle())
			{
				player.sendPacket(SystemMsg.INVALID_TARGET);
				return false;
			}
			if(isInPeaceZone() || target.isInPeaceZone())
			{
				player.sendPacket(SystemMsg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE);
				return false;
			}
			if(player.isInOlympiadMode() && !player.isOlympiadCompStart())
				return false;
		}
		return true;
	}

	@Override
	public void doAttack(Creature target)
	{
		Player player = getPlayer();
		if(player == null)
			return;
		if(isAMuted() || isAttackingNow())
		{
			player.sendActionFailed();
			return;
		}
		if(player.isInObserverMode())
		{
			player.sendActionFailed();
			return;
		}
		if(!checkTarget(target))
		{
			if(!isServitor())
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
			player.sendActionFailed();
			return;
		}
		WeaponTemplate weaponItem = getActiveWeaponTemplate();
		if(weaponItem != null)
		{
			int weaponMpConsume = weaponItem.getMpConsume();
			int[] reducedMPConsume = weaponItem.getReducedMPConsume();
			if(reducedMPConsume[0] > 0 && Rnd.chance(reducedMPConsume[0]))
				weaponMpConsume = reducedMPConsume[1];
			boolean isBow = weaponItem.getItemType() == WeaponTemplate.WeaponType.BOW;
			if(isBow)
			{
				double cheapShot = calcStat(Stats.CHEAP_SHOT, 0.0, target, null);
				if(Rnd.chance(cheapShot))
					weaponMpConsume = 0;
			}
			if(weaponMpConsume > 0)
			{
				if(_currentMp < weaponMpConsume)
				{
					getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
					player.sendPacket(SystemMsg.NOT_ENOUGH_MP);
					player.sendActionFailed();
					return;
				}
				reduceCurrentMp(weaponMpConsume, null);
			}
			if(isBow && !player.checkAndEquipArrows())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				player.sendPacket(SystemMsg.YOU_HAVE_RUN_OUT_OF_ARROWS);
				player.sendActionFailed();
				return;
			}
		}
		super.doAttack(target);
	}

	@Override
	public void doCast(SkillEntry skillEntry, Creature target, boolean forceUse)
	{
		if(skillEntry == null)
			return;
		Skill skill = skillEntry.getTemplate();
		if(skill.isAoE() && skill.isOffensive() && isInPeaceZone())
		{
			getPlayer().sendPacket(SystemMsg.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_A_PEACE_ZONE);
			return;
		}
		if(skill.getSkillType() == Skill.SkillType.DEBUFF && target.isNpc() && target.isInvul() && !target.isMonster())
		{
			getPlayer().sendPacket(SystemMsg.INVALID_TARGET);
			return;
		}
		super.doCast(skillEntry, target, forceUse);
	}

	@Override
	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp,
								boolean directHp, boolean canReflectAndAbsorb, boolean transferDamage, boolean isDot,
								boolean sendReceiveMessage, boolean sendGiveMessage, boolean crit, boolean miss,
								boolean shld, boolean magic)
	{
		if(attacker == null || isDead() || attacker.isDead() && !isDot)
			return;
		boolean damageBlocked = isDamageBlocked(attacker, skill);

		if(damageBlocked && transferDamage)
			return;

		if(damageBlocked && attacker != this)
		{
			if(attacker.isDead() && isDot) {
				damageBlocked = false;
			}
			else {
				if(attacker.isPlayer() && sendGiveMessage) {
					ExMagicAttackInfo.packet(attacker, this, MagicAttackType.IMMUNE);
					attacker.sendPacket(SystemMsg.THE_ATTACK_HAS_BEEN_BLOCKED);
				}
				return;
			}
		}

		if(attacker != this && attacker.isPlayable())
		{
			Player player = getPlayer();
			Player pcAttacker = attacker.getPlayer();

			if(pcAttacker != player && player.isInOlympiadMode() && !player.isOlympiadCompStart())
			{
				if(sendGiveMessage)
					pcAttacker.sendPacket(SystemMsg.INVALID_TARGET);
				return;
			}
			if(isInZoneBattle() != attacker.isInZoneBattle())
			{
				if(sendGiveMessage)
					attacker.getPlayer().sendPacket(SystemMsg.INVALID_TARGET);
				return;
			}

			DuelEvent duelEvent = getEvent(DuelEvent.class);
			if(duelEvent != null && attacker.getEvent(DuelEvent.class) != duelEvent)
				duelEvent.abortDuel(player);

		}
		super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflectAndAbsorb, transferDamage, isDot, sendReceiveMessage, sendGiveMessage, crit, miss, shld, magic);
	}

	@Override
	public boolean isAttackable(Creature attacker)
	{
		return isCtrlAttackable(attacker, true, false);
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return isCtrlAttackable(attacker, false, false);
	}

	public boolean isCtrlAttackable(Creature attacker, boolean force, boolean nextAttackCheck)
	{
		Player player = getPlayer();
		if(attacker == null || player == null || attacker == this || attacker == player && !force || isDead() || attacker.isAlikeDead())
			return false;

		if(isInvisible(attacker) || getReflection() != attacker.getReflection())
			return false;

		if(force && attacker.isMyServitor(getObjectId()))
			return true;

		Boat boat = player.getBoat();
		if(boat != null)
			return false;

		if(isTransformed() && !getTransform().isNormalAttackable())
			return false;

		Player pcAttacker = attacker.getPlayer();
		if(isPlayer() && pcAttacker == this)
			return false;

		if(pcAttacker == null || pcAttacker == player)
			return getFraction().canAttack(attacker.getFraction());

		boat = pcAttacker.getBoat();

		if(boat != null)
			return false;

		if((player.isInOlympiadMode() || pcAttacker.isInOlympiadMode()) && player.getOlympiadGame() != pcAttacker.getOlympiadGame())
			return false;
		if(player.isInOlympiadMode() && !player.isOlympiadCompStart())
			return false;
		if(player.isInOlympiadMode() && player.isOlympiadCompStart() && player.getOlympiadSide() == pcAttacker.getOlympiadSide() && !force)
			return false;
		if(pcAttacker.isInOlympiadMode() && player.isInOlympiadMode() && player.getOlympiadGame() == pcAttacker.getOlympiadGame())
			return true;

		if(player.isInNonPvpTime())
			return false;

		if(!force && player.getParty() != null && player.getParty() == pcAttacker.getParty())
			return false;

		if(force && player.getParty() != null && player.getParty() == pcAttacker.getParty()) {
			return true;
		}

		if(!force && player.isInParty() && player.getParty().getCommandChannel() != null && pcAttacker.isInParty() && pcAttacker.getParty().getCommandChannel() != null && player.getParty().getCommandChannel() == pcAttacker.getParty().getCommandChannel())
			return false;

		for(Event e : attacker.getEvents())
			if(e.checkForAttack(this, attacker, null, force) != null)
				return false;

		for(Event e : attacker.getEvents())
			if(e.canAttack(this, attacker, null, force, nextAttackCheck))
				return true;

		ActionPermissionComponent actionPermissionComponent = attacker.getActionPermissionComponent();
		if(actionPermissionComponent.anyFailure(EActionPermissionLevel.None, IAttackPermission.class, attacker, this)) {
			return false;
		}
		if(actionPermissionComponent.anySuccess(EActionPermissionLevel.None, IAttackPermission.class, attacker, this)) {
			ActionPermissionComponent selfActionPermission = getActionPermissionComponent();
			if(selfActionPermission.anyFailure(EActionPermissionLevel.None, IIncomingAttackPermission.class, attacker, this)) {
				return false;
			}
			if(selfActionPermission.anySuccess(EActionPermissionLevel.None, IIncomingAttackPermission.class, attacker, this)) {
				return true;
			}
			return true;
		}
		if(player.getPvpFlag() != 0) {
			if(pcAttacker.isInSameClan(player)) {
				return false;
			}
			if(pcAttacker.isInSameAlly(player)) {
				return false;
			}
			return !nextAttackCheck;
		}

		return getFraction().canAttack(attacker.getFraction());
	}

	@Override
	public int getKarma()
	{
		Player player = getPlayer();
		return player == null ? 0 : player.getKarma();
	}

	@Override
	public void callSkill(Skill skill, List<Creature> targets, boolean useActionSkills, boolean trigger)
	{
		Player player = getPlayer();
		if(player == null)
			return;
		for(Creature target : targets)
		{
			if(target.isNpc())
			{
				if(!trigger && skill.isOffensive() && target.paralizeOnAttack(player))
				{
					if(Config.PARALIZE_ON_RAID_DIFF)
						paralizeMe(target);
					return;
				}
				target.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skill, this, target);
			}
			else if(target.isPlayable() && player != target && !player.isMyServitor(target.getObjectId()))
			{
				int aggro = skill.getEffectPoint() != 0 ? skill.getEffectPoint() : Math.max(1, (int) skill.getPower());
				if(skill.isOffensive())
					aggro = -aggro;
				List<NpcInstance> npcs = World.getAroundNpc(target);
				for(NpcInstance npc : npcs)
				{
					npc.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, skill, this, target);
					if(!trigger && useActionSkills && !skill.altUse() && !npc.isDead() && npc.isInRangeZ(this, 2000L))
					{
						if(npc.getAggroList().getHate(target) > 0 && !skill.isHandler() && npc.paralizeOnAttack(player))
						{
							if(Config.PARALIZE_ON_RAID_DIFF)
							{
								Skill revengeSkill = SkillHolder.getInstance().getSkill(Skill.SKILL_RAID_CURSE_2, 1);
								if(revengeSkill != null)
									revengeSkill.getEffects(npc, this);
							}
							return;
						}
						if(aggro <= 0)
							continue;
						AggroList.AggroInfo ai = npc.getAggroList().get(target);
						if(ai == null)
							continue;
						if(ai.hate < 100)
							continue;
						if(!GeoEngine.canSeeTarget(npc, target, false))
							continue;
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, ai.damage == 0 ? aggro / 2 : aggro);
					}
				}
			}
			if(!trigger && checkPvP(target, skill))
				startPvPFlag(target);
		}
		super.callSkill(skill, targets, useActionSkills, trigger);
	}

	public void broadcastPickUpMsg(ItemInstance item)
	{
		Player player = getPlayer();
		if(item == null || player == null)
			return;
		if(item.isEquipable() && !(item.getTemplate() instanceof EtcItemTemplate))
		{
			SystemMessagePacket msg;
			String player_name = player.getName();
			if(item.getEnchantLevel() > 0)
			{
				SystemMsg systemMsg = isPlayer() ? SystemMsg.ATTENTION_C1_HAS_PICKED_UP_S2S3 : SystemMsg.ATTENTION_C1S_PET_HAS_PICKED_UP_S2S3;
				msg = new SystemMessagePacket(systemMsg).addString(player_name).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
			}
			else
			{
				SystemMsg systemMsg = isPlayer() ? SystemMsg.ATTENTION_C1_HAS_PICKED_UP_S2 : SystemMsg.ATTENTION_C1S_PET_HAS_PICKED_UP_S2S3;
				msg = new SystemMessagePacket(systemMsg).addString(player_name).addItemName(item.getItemId());
			}
			for(Player target : World.getAroundObservers(this))
				if(!isInvisible(target))
					target.sendPacket(msg);
		}
	}

	public void paralizeMe(Creature effector)
	{
		Skill revengeSkill = SkillHolder.getInstance().getSkill(Skill.SKILL_RAID_CURSE, 1);
		revengeSkill.getEffects(effector, this);
	}

	public final void setPendingRevive(boolean value)
	{
		_isPendingRevive = value;
	}

	public boolean isPendingRevive()
	{
		return _isPendingRevive;
	}

	public void doRevive()
	{
		if(!isTeleporting())
		{
			setPendingRevive(false);
			setNonAggroTime(System.currentTimeMillis() + Config.NONAGGRO_TIME_ONTELEPORT);
			setNonPvpTime(System.currentTimeMillis() + Config.NONPVP_TIME_ONTELEPORT);
			if(isSalvation())
			{
				getAbnormalList().stopEffects(EffectType.Salvation);
                setCurrentHp(getMaxHp(), true);
                setCurrentMp(getMaxMp());
                setCurrentCp(getMaxCp());
			}
			else
			{
				setCurrentHp(Math.max(1.0, getMaxHp() * Config.RESPAWN_RESTORE_HP), true);
				if(Config.RESPAWN_RESTORE_MP >= 0.0)
					setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);
				if(isPlayer() && Config.RESPAWN_RESTORE_CP >= 0.0)
					setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
			}
            broadcastPacket(new RevivePacket(this));

			getListeners().onRevive();
		}
		else
			setPendingRevive(true);
	}

	public abstract void doPickupItem(GameObject p0);

	public void sitDown(StaticObjectInstance throne)
	{}

	public void standUp()
	{}

	public boolean isInNonAggroTime()
	{
		return _nonAggroTime > System.currentTimeMillis();
	}

	public void setNonAggroTime(long time)
	{
		_nonAggroTime = time;
	}

	public boolean isInNonPvpTime()
	{
		return _nonPvpTime > System.currentTimeMillis();
	}

	public void setNonPvpTime(long time)
	{
		_nonPvpTime = time;
	}

	public boolean startSilentMoving()
	{
		return _isSilentMoving.getAndSet(true);
	}

	public boolean stopSilentMoving()
	{
		return _isSilentMoving.setAndGet(false);
	}

	public boolean isSilentMoving()
	{
		return _isSilentMoving.get();
	}

	public int getMaxLoad()
	{
		return 0;
	}

	public int getInventoryLimit()
	{
		return 0;
	}

	@Override
	public boolean isPlayable()
	{
		return true;
	}

	public boolean isSharedGroupDisabled(int groupId)
	{
		TimeStamp sts = _sharedGroupReuses.get(groupId);
		if(sts == null)
			return false;
		if(sts.hasNotPassed())
			return true;
		_sharedGroupReuses.remove(groupId);
		return false;
	}

	public TimeStamp getSharedGroupReuse(int groupId)
	{
		return _sharedGroupReuses.get(groupId);
	}

	public void addSharedGroupReuse(int group, TimeStamp stamp)
	{
		_sharedGroupReuses.put(group, stamp);
	}

	public Map<Integer, TimeStamp> getSharedGroupReuses()
	{
		return _sharedGroupReuses;
	}

	public boolean useItem(ItemInstance item, boolean ctrlPressed, boolean force)
	{
		return false;
	}

	public int getCurrentLoad()
	{
		return 0;
	}

	public int getWeightPenalty()
	{
		return 0;
	}

	@Override
	public boolean isInBoat()
	{
		return _boat != null;
	}

	@Override
	public boolean isInShuttle()
	{
		return _boat != null && _boat.isShuttle();
	}

	public Boat getBoat()
	{
		return _boat;
	}

	public void setBoat(Boat boat)
	{
		_boat = boat;
	}

	public Location getInBoatPosition()
	{
		return _inBoatPosition;
	}

	public void setInBoatPosition(Location loc)
	{
		_inBoatPosition = loc;
	}

	public int getNameColor()
	{
		return 0;
	}

	@Override
	public PlayableBaseStats getBaseStats()
	{
		if(_baseStats == null)
			_baseStats = new PlayableBaseStats(this);
		return (PlayableBaseStats) _baseStats;
	}

	public int getRelation(Player target)
	{
		int result = 0;
		if(getClan() != null)
		{
			result |= RelationChangedPacket.RELATION_CLAN_MEMBER;
			if(getClan() == target.getClan())
				result |= RelationChangedPacket.RELATION_CLAN_MATE;
			if(getClan().getAllyId() != 0)
				result |= RelationChangedPacket.RELATION_ALLY_MEMBER;
		}
		if(getPlayer().isClanLeader())
			result |= RelationChangedPacket.RELATION_LEADER;
		Party party = getPlayer().getParty();
		if(party != null && party == target.getParty())
		{
			result |= RelationChangedPacket.RELATION_HAS_PARTY;
			switch(party.getPartyMembers().indexOf(this))
			{
				case 0:
					result |= RelationChangedPacket.RELATION_PARTYLEADER; // 0x10
					break;
				case 1:
					result |= RelationChangedPacket.RELATION_PARTY4; // 0x8
					break;
				case 2:
					result |= RelationChangedPacket.RELATION_PARTY3 + RelationChangedPacket.RELATION_PARTY2 + RelationChangedPacket.RELATION_PARTY1; // 0x7
					break;
				case 3:
					result |= RelationChangedPacket.RELATION_PARTY3 + RelationChangedPacket.RELATION_PARTY2; // 0x6
					break;
				case 4:
					result |= RelationChangedPacket.RELATION_PARTY3 + RelationChangedPacket.RELATION_PARTY1; // 0x5
					break;
				case 5:
					result |= RelationChangedPacket.RELATION_PARTY3; // 0x4
					break;
				case 6:
					result |= RelationChangedPacket.RELATION_PARTY2 + RelationChangedPacket.RELATION_PARTY1; // 0x3
					break;
				case 7:
					result |= RelationChangedPacket.RELATION_PARTY2; // 0x2
					break;
				case 8:
					result |= RelationChangedPacket.RELATION_PARTY1; // 0x1
					break;
			}
		}
		Clan clan1 = getClan();
		Clan clan2 = target.getClan();

		if(clan1 != null && clan2 != null)
		{
			if(target.getPledgeType() != Clan.SUBUNIT_ACADEMY && getPlayer().getPledgeType() != Clan.SUBUNIT_ACADEMY)
			{
				if(clan2.isAtWarWith(clan1))
				{
					result |= RelationChangedPacket.RELATION_1SIDED_WAR;
					if(clan1.isAtWarWith(clan2))
						result |= RelationChangedPacket.RELATION_MUTUAL_WAR;
				}
			}
		}
		for(Event e : getEvents())
			result = e.getRelation(getPlayer(), target, result);
		return result;
	}

	public void broadcastRelation()
	{
		if(!isVisible())
			return;
		for(Player target : World.getAroundObservers(this))
		{
			if(isInvisible(target))
				continue;
			RelationChangedPacket relationChanged = new RelationChangedPacket(this, target);
			for(Servitor servitor : getServitors())
				relationChanged.add(servitor, target);
			target.sendPacket(relationChanged);
		}
	}

    @Override
    public void setXYZ(int x, int y, int z, boolean MoveTask) {
        super.setXYZ(x, y, z, MoveTask);
        int dbgMove = getPlayer().getVarInt("debugMove", 0);

        if (MoveTask && isPlayable() && dbgMove > 0) {
            Location loc = getLoc();
            ExServerPrimitivePacket tracePkt = new ExServerPrimitivePacket(loc.toXYZString(), loc.getX(), loc.getY(), (int) ((double) loc.getZ() + getColHeight() + 16.0));
            if (getDestination() != null) {
                Color[] ccs = {Color.CYAN, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.RED, Color.YELLOW, Color.RED};
                Color c = ccs[System.identityHashCode(getDestination()) % ccs.length];
                tracePkt.addPoint(String.format("%s|%08x", loc.toXYZString(), getDestination().hashCode()), c, true, loc.getX(), loc.getY(), loc.getZ());
            } else {
                tracePkt.addPoint(loc.toXYZString(), 16777215, true, loc.getX(), loc.getY(), loc.getZ());
            }

            sendPacket(tracePkt);
            if (dbgMove > 1)
                broadcastPacketToOthers(tracePkt);
        }
	}

	public Playable getPlayable()
	{
		return this;
	}
}

package l2s.gameserver.model.instances;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.QuestHolder;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.RaidBossSpawnManager;
import l2s.gameserver.listener.actor.OnCurrentHpDamageListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.bbs.CommunityBoardEntry;
import l2s.gameserver.model.bbs.RaidBossTeleportationCommunityBoardEntry;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.model.reward.RewardItem;
import l2s.gameserver.model.reward.RewardList;
import l2s.gameserver.model.reward.RewardType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.service.MoraleBoostService;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;

public class RaidBossInstance extends MonsterInstance
{
	private static final int MINION_UNSPAWN_INTERVAL = 5000;
	private static final int[] RESTRICTED_FOR_SPECIAL_REWARD_ID = new int[] {
		40522,	// Aragus (Kamaloka)
		40523,	// Ederius (Kamaloka)
		25283,	// Lilith
		25286	// Anakim
	};
	private static final int[] DISABLED_HP_ANNOUNCE = new int[] {
		40522,	// Aragus (Kamaloka)
		40523,	// Ederius (Kamaloka)
	};

	private final RaidBossTeleportationCommunityBoardEntry bbsEntry;
	private final Map<Integer, ScheduledFuture<?>> attackerTimers = new ConcurrentHashMap<>();
	private final ReduceHpListener reduceHpListener = new ReduceHpListener();
	private ScheduledFuture<?> minionMaintainTask;
	private ScheduledFuture<?> specialRewardTask;

	public RaidBossInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
		bbsEntry = new RaidBossTeleportationCommunityBoardEntry(this);
	}

	@Override
	public boolean isRaid()
	{
		return true;
	}

	protected int getMinionUnspawnInterval()
	{
		return MINION_UNSPAWN_INTERVAL;
	}

	protected int getKilledInterval(NpcInstance minion)
	{
		return 120000;
	}

	@Override
	public void notifyMinionDied(NpcInstance minion)
	{
		minionMaintainTask = ThreadPoolManager.getInstance().schedule(new MaintainKilledMinion(minion), getKilledInterval(minion) + minion.getCorpseTime());
		super.notifyMinionDied(minion);
	}

	@Override
	public double getRewardRate(Player player)
	{
		return Config.RATE_DROP_RAIDBOSS;
	}

	@Override
	protected void onDeath(Creature killer)
	{
		if(minionMaintainTask != null)
		{
			minionMaintainTask.cancel(false);
			minionMaintainTask = null;
		}
		if(this instanceof ReflectionBossInstance)
		{
			super.onDeath(killer);
			return;
		}
		if(killer != null && killer.isPlayable())
		{
			MoraleBoostService.getInstance().bossKill(this, killer.getPlayable());
			Player player = killer.getPlayer();
			if(player.isInParty())
				player.getParty().broadCast(SystemMsg.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL);
			else
				player.sendPacket(SystemMsg.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL);
			Quest q = QuestHolder.getInstance().getQuest(508);
			if(q != null && player.getClan() != null && player.getClan().getLeader().isOnline())
			{
				QuestState st = player.getClan().getLeader().getPlayer().getQuestState(q);
				if(st != null)
					st.getQuest().onKill(this, st);
			}
		}

		if(getMinionList().hasAliveMinions() && doUnspawnMinions())
			ThreadPoolManager.getInstance().schedule(() -> {
				if(isDead())
					getMinionList().unspawnMinions();
				return;
			}, getMinionUnspawnInterval());

		int boxId = 0;
		switch(getNpcId())
		{
			case 25035:
			{
				boxId = 31027;
				break;
			}
			case 25054:
			{
				boxId = 31028;
				break;
			}
			case 25126:
			{
				boxId = 31029;
				break;
			}
			case 25220:
			{
				boxId = 31030;
				break;
			}
		}
		if(boxId != 0)
		{
			NpcTemplate boxTemplate = NpcHolder.getInstance().getTemplate(boxId);
			if(boxTemplate != null)
			{
				NpcInstance box = new NpcInstance(IdFactory.getInstance().getNextId(), boxTemplate, StatsSet.EMPTY);
				box.spawnMe(getLoc());
				box.setSpawnedLoc(getLoc());
				box.startDeleteTask(60000L);
			}
		}
		if(killer != null && killer.getPlayer() != null && Config.RAID_DROP_GLOBAL_ITEMS)
		{
			if(Config.MIN_RAID_LEVEL_TO_DROP > 0 && getLevel() < Config.MIN_RAID_LEVEL_TO_DROP)
			{
				super.onDeath(killer);
				return;
			}
			for(Config.RaidGlobalDrop drop_inf : Config.RAID_GLOBAL_DROP)
			{
				int id = drop_inf.getId();
				long count = drop_inf.getCount();
				double chance = drop_inf.getChance();
				if(Rnd.chance(chance))
					ItemFunctions.addItem(killer.getPlayer(), id, count, true);
			}
		}
		if (specialRewardAvailable()) {
			stopSpecialRewardTask();
			Announcements.announceToAllFromStringHolder("l2s.gameserver.instancemanager.RaidBossInstance.onDeath", getName());
		}
		super.onDeath(killer);
	}

	@Override
	protected void onDecay()
	{
		super.onDecay();
		if (hpAnnounceEnabled()) {
			removeListener(reduceHpListener);
			reduceHpListener.reset();
		}
		if (specialRewardAvailable()) {
			bbsEntry.unregister();
		}
		RaidBossSpawnManager.getInstance().onBossDespawned(this, true);
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		if (hpAnnounceEnabled()) {
			addListener(reduceHpListener);
		}
		addSkill(SkillHolder.getInstance().getSkillEntry(4045, 1));
		if (specialRewardAvailable()) {
			bbsEntry.register();
			startSpecialRewardTask();
		}
		RaidBossSpawnManager.getInstance().onBossSpawned(this);
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}

	@Override
	public boolean hasRandomWalk()
	{
		return false;
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}

	@Override
	public boolean dropOnTheGround()
	{
		return true;
	}

    @Override
    public void rollRewards(RewardList list, Creature lastAttacker, Creature topDamager)
    {
/*
        // FIXME вынести куда-нибудь
        if (getNpcId() == 29014 || getNpcId() == 29006 || getNpcId() == 29001*/
/* || getNpcId() == 29020*//*
) {
            super.rollRewards(list, lastAttacker, topDamager);
            return;
        }
*/

        RewardType type = list.getType();
        if(type == RewardType.SWEEP && !isSpoiled())
            return;
        Creature activeChar = type == RewardType.SWEEP ? lastAttacker : lockDropTo(topDamager);
        Player activePlayer = activeChar.getPlayer();
        if(activePlayer == null)
            return;
        double penaltyMod = Experience.penaltyModifier(calculateLevelDiffForDrop(topDamager.getLevel()));

        int minDamageForReward = getMaxHp() * Config.RAID_BOSSES_DAMAGE_PERCENT_FOR_REWARD_AMOUNT / 100;

        Set<Player> dropPlayers = getAggroList().getPlayableMap().entrySet().stream()
                .filter(entry -> entry.getValue().damage >= minDamageForReward)
                .map(Map.Entry::getKey)
                .map(GameObject::getPlayer)
                .collect(Collectors.toSet());

        List<RewardItem> rewardItems = list.roll(activePlayer, penaltyMod, this);
        for (RewardItem drop : rewardItems) {
            if (!Config.DROP_ONLY_THIS.isEmpty() && !Config.DROP_ONLY_THIS.contains(drop.itemId)
                    && (!Config.INCLUDE_RAID_DROP || !isRaid())) {
                return;
            }

            dropItemToTheGround(dropPlayers, drop.itemId, drop.count);
        }

        if (getChampion() > 0 && Config.SPECIAL_ITEM_ID > 0 && Math.abs(getLevel() - activePlayer.getLevel()) < 9 && Rnd.chance(Config.SPECIAL_ITEM_DROP_CHANCE)) {
            ItemFunctions.addItem(activePlayer, Config.SPECIAL_ITEM_ID, Config.SPECIAL_ITEM_COUNT);
        }
    }

	public CommunityBoardEntry getBBSEntry() {
		return bbsEntry;
	}

	private void startSpecialRewardTask() {
		specialRewardTask = ThreadPoolManager.getInstance().scheduleAtFixedDelay(
			new RefreshSpecialReward(),
			0,
			Config.RAID_BOSSES_SPECIAL_REWARD_REFRESH_TIMER_DELAY * 1000L
		);
	}

	private void stopSpecialRewardTask() {
		if (specialRewardTask != null) {
			specialRewardTask.cancel(true);
			specialRewardTask = null;
		}

		final Map<Fraction, Long> factionDamage = new HashMap<>();

		long totalDamage = getAggroList().getPlayableMap().values().stream().mapToLong(h -> h.damage).sum();
		if (totalDamage == 0) {
			return;
		}

		getAggroList().getPlayableMap()
			.forEach((p, info) -> factionDamage.merge(p.getFraction(), (long) info.damage, Long::sum));

		long fireDamagePercent = calcFactionRewardPercent(Fraction.FIRE, factionDamage, totalDamage);
		long waterDamagePercent = calcFactionRewardPercent(Fraction.WATER, factionDamage, totalDamage);

		attackerTimers.forEach((objId, timer) -> {
			final Player player = GameObjectsStorage.getPlayer(objId);
			if (player != null) {
				long count = 0L;
				switch (player.getFraction()) {
					case FIRE:
						count = fireDamagePercent;
						break;
					case WATER:
						count = waterDamagePercent;
						break;
				}
				player.sendMessage(new CustomMessage("l2s.gameserver.instancemanager.RaidBossInstance.specialReward"));
				ItemFunctions.addItem(player, Config.RAID_BOSSES_SPECIAL_REWARD_ID, count, true);
			}
		});
	}

	private long calcFactionRewardPercent(Fraction f, Map<Fraction, Long> fractionDamage, long totalDamage) {
		int max = Config.RAID_BOSSES_SPECIAL_REWARD_MAX_FACTION_DAMAGE_PERCENT;
		int min = 100 - max;
		long damage = fractionDamage.getOrDefault(f, 0L) * 100 / totalDamage;
		return damage < min ? min : damage > max ? max : damage;
	}

	private boolean specialRewardAvailable() {
		return !isBoss() && Arrays.stream(RESTRICTED_FOR_SPECIAL_REWARD_ID).noneMatch(id -> id == getNpcId());
	}

	private boolean hpAnnounceEnabled() {
		return Arrays.stream(DISABLED_HP_ANNOUNCE).noneMatch(id -> id == getNpcId());
	}

	private class MaintainKilledMinion implements Runnable
	{
		private final NpcInstance minion;

		public MaintainKilledMinion(NpcInstance minion)
		{
			this.minion = minion;
		}

		@Override
		public void run()
		{
			if(!isDead())
			{
				minion.refreshID();
				spawnMinion(minion);
			}
		}
	}

	private class RefreshSpecialReward implements Runnable {

		@Override
		public void run() {
			getAroundCharacters(2000, 2000).stream()
				.filter(c -> !c.isDead())
				.forEach(c -> {
					ScheduledFuture<?> removeTask = ThreadPoolManager.getInstance().schedule(
						new RemoveFromAttackers(c.getObjectId()),
						Config.RAID_BOSSES_SPECIAL_REWARD_TIME * 1000L
					);
					if (removeTask != null) {
						ScheduledFuture<?> oldTask = attackerTimers.put(c.getObjectId(), removeTask);
						if (oldTask != null) {
							oldTask.cancel(true);
						}
					}
				});
		}
	}

	private class RemoveFromAttackers implements Runnable {

		private final int attackerId;

		public RemoveFromAttackers(int attackerId) {
			this.attackerId = attackerId;
		}

		@Override
		public void run() {
			attackerTimers.remove(attackerId);
		}
	}

	private static class ReduceHpListener implements OnCurrentHpDamageListener {
		private final int[] threshold = { 70, 30 };
		private int step = 0;

		@Override
		public void onCurrentHpDamage(Creature actor, double dmg, Creature attacker, Skill skill, boolean shared) {
			double hpPercent = actor.getCurrentHpPercents();
			for (int i = step; i < threshold.length; i++) {
				if (hpPercent <= threshold[i]) {
					CustomMessage message = new CustomMessage("l2s.gameserver.instancemanager.RaidBossInstance.reduceHp")
						.addString(actor.getName())
						.addNumber(threshold[i]);

					for (Player player : GameObjectsStorage.getPlayers()) {
						String text = message.toString(player);
						player.sendPacket(new ExShowScreenMessage(text, 4000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, text.length() < 64));
					}
					step++;
				}
			}
		}

		public void reset() {
			step = 0;
		}
	}
}

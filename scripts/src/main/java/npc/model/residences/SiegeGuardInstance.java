package npc.model.residences;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.Config;
import l2s.gameserver.component.fraction.FractionBoostBalancer;
import l2s.gameserver.data.xml.holder.FortressUpgradeHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.entity.residence.fortress.UpgradeData;
import l2s.gameserver.model.entity.residence.fortress.UpgradeType;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.reward.RewardItem;
import l2s.gameserver.model.reward.RewardList;
import l2s.gameserver.templates.npc.NpcTemplate;

import java.util.List;

public class SiegeGuardInstance extends NpcInstance {
    private static final long serialVersionUID = 1L;

    public SiegeGuardInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
        setHasChatWindow(false);
    }

    @Override
    public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp,
                                boolean directHp, boolean canReflectAndAbsorb, boolean transferDamage, boolean isDot,
                                boolean sendReceiveMessage, boolean sendGiveMessage, boolean crit, boolean miss,
                                boolean shld, boolean magic)
    {
        // Если одна из фракций, владеет сразу обеими замками, сделать следующее:
        // Атакующей фракции увеличить урон по гвардам замка на 50%
        if (FractionBoostBalancer.getInstance().getCount(attacker.getFraction().revert()) == 2)
            damage *= 1.50;

        super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflectAndAbsorb, transferDamage,
                isDot, sendReceiveMessage, sendGiveMessage, crit, miss, shld, magic);
    }

    @Override
    public int getMaxHp() {
        final int max_hp = super.getMaxHp();
        FortressSiegeEvent siege = getEvent(FortressSiegeEvent.class);
        if (siege != null) {
            final int level = siege.getResidence().getUpgrade(UpgradeType.GUARD);
            final UpgradeData data = FortressUpgradeHolder.getInstance().get(siege.getResidence().getId()).getData(UpgradeType.GUARD, level);
            return (int) ((max_hp * Double.parseDouble(data.getParam())) - max_hp);
        } else
            return max_hp;
    }

    @Override
    public boolean isSiegeGuard() {
        return true;
    }

    @Override
    public int getAggroRange() {
        return 1200;
    }

    @Override
    public double getRewardRate(Player player) {
        return Config.RATE_DROP_SIEGE_GUARD; // ПА не действует на эполеты
    }

    @Override
    public boolean isAttackable(Creature attacker) {
        return isAutoAttackable(attacker);
    }

    @Override
    public boolean isAutoAttackable(Creature attacker) {
        Player player = attacker.getPlayer();
        if (player == null)
            return false;
        SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
        if (siegeEvent == null)
            return false;
        Residence residence = siegeEvent.getResidence();

        Fraction owner = residence.getFraction();

        return owner.canAttack(attacker.getFraction());
    }

    @Override
    public boolean hasRandomAnimation() {
        return false;
    }

    @Override
    public boolean isPeaceNpc() {
        return false;
    }

    @Override
    protected void onDeath(Creature killer) {
        SiegeEvent<?, ?> siegeEvent = getEvent(SiegeEvent.class);
        if (killer != null) {
            Player player = killer.getPlayer();
            if (siegeEvent != null && player != null) {
                Clan clan = player.getClan();
                SiegeEvent<?, ?> siegeEvent2 = killer.getEvent(SiegeEvent.class);
                if (clan != null && siegeEvent == siegeEvent2 && siegeEvent.getSiegeClan(SiegeEvent.DEFENDERS, clan) == null) {
                    Creature topdam = getAggroList().getTopDamager(killer);
                    for (RewardList list : getTemplate().getRewards())
                        rollRewards(list, killer, topdam);
                }
            }
        }
        super.onDeath(killer);
    }

    @Override
    public void rollRewards(RewardList list, Creature lastAttacker, Creature topDamager) {
        Player activePlayer = topDamager.getPlayer();
        if (activePlayer == null)
            return;

        double penaltyMod = Experience.penaltyModifier(calculateLevelDiffForDrop(topDamager.getLevel()));

        List<RewardItem> rewardItems = list.roll(activePlayer, penaltyMod, this);

        for (RewardItem drop : rewardItems)
            dropItem(activePlayer, drop.itemId, drop.count);
    }

    @Override
    public boolean isFearImmune() {
        return true;
    }

    @Override
    public boolean isParalyzeImmune() {
        return true;
    }

    @Override
    public Clan getClan() {
        return null;
    }

    @Override
    public Fraction getFraction() {
        SiegeEvent siegeEvent = getEvent(SiegeEvent.class);
        if (siegeEvent == null)
            return Fraction.NONE;

        Residence residence = siegeEvent.getResidence();
        return residence.getFraction();
    }
}
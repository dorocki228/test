package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.reward.RewardItem;
import l2s.gameserver.model.reward.RewardList;
import l2s.gameserver.templates.npc.NpcTemplate;

import java.util.List;

public class OutpostGuardInstance extends NpcInstance {

    public OutpostGuardInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
        setHasChatWindow(false);
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
    public boolean isAutoAttackable(Creature attacker) {
        return attacker.isPlayable() && (getFraction().canAttack(attacker.getFraction()) || attacker.isPK());
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
    public boolean isGuard() {
        return true;
    }

    @Override
    public boolean isFearImmune() {
        return true;
    }

    @Override
    public boolean isParalyzeImmune() {
        return true;
    }
}
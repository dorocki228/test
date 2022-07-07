package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.data.xml.holder.FortressUpgradeHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2s.gameserver.model.entity.residence.fortress.UpgradeData;
import l2s.gameserver.model.entity.residence.fortress.UpgradeType;
import l2s.gameserver.templates.npc.NpcTemplate;

public class GuardInstance extends NpcInstance {
    public GuardInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
        setHasChatWindow(false);
    }

    @Override
    public boolean isAttackable(Creature attacker) {
        return isAutoAttackable(attacker);
    }

    @Override
    public boolean isAutoAttackable(Creature attacker) {
        return attacker.isPlayable() && (getFraction().canAttack(attacker.getFraction()) || attacker.isPK());
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
    public boolean isGuard() {
        return true;
    }

    @Override
    public boolean isPeaceNpc() {
        return false;
    }

    @Override
    public boolean isFearImmune() {
        return true;
    }

    @Override
    public boolean isParalyzeImmune() {
        return true;
    }

/*    @Override
    public boolean noShiftClick() {
        return true;
    }*/
}

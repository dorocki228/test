package l2s.gameserver.model.entity.events.impl.upgrading.listener;

import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.listener.zone.OnZoneTickListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.events.impl.UpgradingEvent;
import l2s.gameserver.skills.SkillEntry;

/**
 * @author KRonst
 */
public class UpgradingEventOnTickListener implements OnZoneTickListener, OnZoneEnterLeaveListener {

    private final UpgradingEvent upgradingEvent;
    private final SkillEntry winnerSkill;
    private final SkillEntry loserSkill;

    public UpgradingEventOnTickListener(UpgradingEvent upgradingEvent, SkillEntry winnerSkill, SkillEntry loserSkill) {
        this.upgradingEvent = upgradingEvent;
        this.winnerSkill = winnerSkill;
        this.loserSkill = loserSkill;
    }

    @Override
    public void onTick(Zone zone) {
        if (!upgradingEvent.isInProgress()) {
            return;
        }
        if (winnerSkill == null || loserSkill == null) {
            return;
        }
        if (upgradingEvent.getArtifactOwner() == Fraction.NONE) {
            return;
        }
        zone.getInsidePlayers().forEach(this::applyEffect);
    }

    @Override
    public void onZoneEnter(Zone zone, Creature creature) {
        Player player = creature.getPlayer();
        if(player == null) {
            return;
        }
        if(!upgradingEvent.isInProgress()) {
            return;
        }
        if (!player.containsEvent(upgradingEvent)) {
            player.addEvent(upgradingEvent);
        }
        if (upgradingEvent.getArtifactOwner() != Fraction.NONE) {
            applyEffect(player);
        }
    }

    @Override
    public void onZoneLeave(Zone zone, Creature creature) {
        Player player = creature.getPlayer();
        if (player == null) {
            return;
        }
        if (player.containsEvent(upgradingEvent)) {
            player.removeEvent(upgradingEvent);
        }
    }

    private void applyEffect(Player player) {
        if (!player.isDead()) {
            if (player.getFraction() == upgradingEvent.getArtifactOwner()) {
                winnerSkill.getTemplate().getEffects(player, player);
            } else {
                loserSkill.getTemplate().getEffects(player, player);
            }
        }
    }
}

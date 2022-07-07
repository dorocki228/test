package l2s.gameserver.model.entity.events.impl.service;

import l2s.commons.util.Rnd;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.base.SpecialEffectState;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.snapshot.SnapshotPlayer;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.ChangeWaitTypePacket;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.RevivePacket;
import l2s.gameserver.service.PlayerService;
import l2s.gameserver.utils.Location;

import java.util.List;
import java.util.Objects;

public class EventService {
    private static final EventService INSTANCE = new EventService();

    private EventService() {
    }

    public static EventService getInstance() {
        return INSTANCE;
    }

    public void restorePlayerAndTeleportToBack(Player player, SnapshotPlayer snapshotPlayer) {
        if (player == null) {
            return;
        }
        if (player.isLogoutStarted()) {
            return;
        }
        removeInvisible(player, false);
        unsetPlayerStatusFromEvent(player, false);
        if (!player.isUndyingFlag() && player.isDead()) {
            player.setCurrentHp(player.getMaxHp(), true);
            player.broadcastPacket(new RevivePacket(player));
        }
        if (snapshotPlayer != null) {
            PlayerService.getInstance().recoverFromSnapshotEffect(player, snapshotPlayer);
            PlayerService.getInstance().recoverFromSnapshotCpHpMp(player, snapshotPlayer);
        }
        player.setReflection(ReflectionManager.MAIN);
        player.teleToClosestTown();
    }

    public void cpHpMpHeal(Player player) {
        player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
        player.setCurrentCp(player.getMaxCp());
        player.broadcastUserInfo(true);
    }

    public void sendScreenCustomMessage(String customMessageName, String[] texts, Player player, int time) {
        if (customMessageName == null || player == null) {
            return;
        }
        CustomMessage customMessage = new CustomMessage(customMessageName);
        if (texts != null) {
            for (String text : texts) {
                customMessage.addString(text);
            }
        }
        ExShowScreenMessage sm = new ExShowScreenMessage(customMessage.toString(player), time, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true);
        player.sendPacket(sm);
    }

    public void sendScreenCustomMessage(String customMessageName, String[] texts, Player player) {
        sendScreenCustomMessage(customMessageName, texts, player, 2000);
    }

    public boolean teleportPlayerToRandomPoint(Player player, List<Location> points, Reflection reflection) {
        if (player == null || points == null || reflection == null) {
            return false;
        }
        Location point = Rnd.get(points);
        Objects.requireNonNull(point);
        return player.teleToLocation(point, reflection);
    }

    public void setPlayerStatusFromEvent(Player player, boolean moveBlock, boolean invisible) {
        if (player == null) {
            return;
        }
        player.leaveParty();
        if (player.isDead()) {
            player.setPendingRevive(true);
        }
        if (player.isSitting()) {
            player.standUp();
        }
        player.setTransform(null);
        player.setTarget(null);
        player.abortAttack(true, false);
        if (player.isCastingNow()) {
            player.abortCast(true, true);
        }
        player.stopMove();
        if (player.isInObserverMode()) {
            player.leaveObserverMode();
        }
        if (moveBlock && !player.isMoveBlocked()) {
            player.startMoveBlock();
        }
        if (invisible && !player.isGMInvisible() && !player.isInvisible(null)) {
            player.setInvisible(true);
        }
        player.setUndying(SpecialEffectState.TRUE);
        cpHpMpHeal(player);
        for (Servitor servitor : player.getServitors()) {
            servitor.unSummon(false);
        }
    }

    public void unsetPlayerStatusFromEvent(Player player, boolean broadcast) {
        if (player == null) {
            return;
        }
        if (player.isSitting()) {
            player.standUp();
        }
        player.setTransform(null);
        player.setTarget(null);
        player.abortAttack(true, false);
        if (player.isCastingNow()) {
            player.abortCast(true, true);
        }
        if (player.isFrozen()) {
            player.stopFrozen();
        }
        if (player.isPartyBlocked()) {
            player.stopPartyBlock();
        }
        player.stopMove();
        if (player.isMoveBlocked()) {
            player.stopMoveBlock();
        }
        if (player.isInvisible() && !player.isGMInvisible()) {
            player.setInvisible(false);
        }
        if (player.isUndyingFlag()) {
            removeFakeDeath(player);
        }
        player.setUndying(SpecialEffectState.FALSE);
        for (Servitor servitor : player.getServitors()) {
            servitor.unSummon(false);
        }
        if (broadcast) {
            player.broadcastCharInfo();
        }
    }

    public void removeInvisible(Player player, boolean broadcast) {
        if (player == null) {
            return;
        }
        if (player.isInvisible() && !player.isGMInvisible()) {
            player.setInvisible(false);
            if (broadcast) {
                player.broadcastCharInfo();
            }
        }
    }

    public void setFakeDeath(Player player) {
        if (player.isLogoutStarted()) {
            return;
        }
        if (!player.isFrozen()) {
            player.startFrozen();
        }
        player.broadcastPacket(new ChangeWaitTypePacket(player, ChangeWaitTypePacket.WT_START_FAKEDEATH));
        player.broadcastCharInfo();
    }

    public void removeFakeDeath(Player player) {
        if (player.isLogoutStarted()) {
            return;
        }
        if (player.isFrozen()) {
            player.stopFrozen();
        }
        player.broadcastPacket(new ChangeWaitTypePacket(player, ChangeWaitTypePacket.WT_STOP_FAKEDEATH));
        player.broadcastCharInfo();
    }
}

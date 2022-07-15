package l2s.gameserver.utils;

import com.google.common.flogger.FluentLogger;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.features.huntingzones.HuntingZone;
import l2s.gameserver.features.huntingzones.HuntingZonesService;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.instancemanager.MapRegionManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.TeleportPoint;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.mapregion.RestartArea;
import l2s.gameserver.templates.mapregion.RestartPoint;

public class TeleportUtils {
    private static final FluentLogger _log = FluentLogger.forEnclosingClass();

    public final static Location DEFAULT_RESTART = new Location(17817, 170079, -3530);

    public static boolean canExTeleport(Player activeChar, int teleportId, boolean getFee) {
        HuntingZone zone = HuntingZonesService.INSTANCE.getZone(teleportId);
        if (zone == null) {
            activeChar.sendActionFailed();
            return false;
        }

        if (!zone.getEnabled()) {
            activeChar.sendMessage("Can't use this teleport.");
            return false;
        }

        if (!checkTeleportCond(activeChar)) {
            activeChar.sendMessage("Can't use right now.");
            return false;
        }

        if (getFee && activeChar.getLevel() >= Config.TELEPORT_FREE_UNTIL_LEVEL) {
            if (ItemFunctions.getItemCount(activeChar, ItemTemplate.ITEM_ID_ADENA) < zone.getPrice()) {
                activeChar.sendMessage(activeChar.isLangRus()
                        ? "У вас не хватает нужных вещей для выполнение опрации."
                        : "You have not enough item to proceed the operation.");
                return false;
            }
            return ItemFunctions.deleteItem(activeChar, ItemTemplate.ITEM_ID_ADENA, zone.getPrice());
        }

        return true;
    }

    public static boolean checkTeleportCond(Player player) {
        if (player.isInCombat() || player.getPvpFlag() != 0 || player.isPK())
            return false;

        if (player.isDead())
            return false;

        if (player.getTeam() != TeamType.NONE)
            return false;

        if (player.isFlying() || player.isInFlyingTransform())
            return false;

        if (player.isInBoat())
            return false;

        if (player.isInStoreMode() || player.isInTrade() || player.isInOfflineMode())
            return false;

        if (player.isInDuel())
            return false;

        if(player.isActionsDisabled(false))
            return false;

        if (!player.getReflection().isMain() || player.isInSiegeZone() || player.isInZone(Zone.ZoneType.RESIDENCE)
                || player.isInZone(Zone.ZoneType.HEADQUARTER) || player.isInZone(Zone.ZoneType.battle_zone)
                || player.isInZone(Zone.ZoneType.ssq_zone) || player.isInZone(Zone.ZoneType.no_restart)
                || player.isInZone(Zone.ZoneType.offshore) || player.isInZone(Zone.ZoneType.epic)
                || player.isInOlympiadMode() || player.isInSiegeZone()) {
            player.sendMessage(player.isLangRus() ? "Вы не можете совершить телепорт с локации в которой находитесь в данный момент." : "You can not make a teleport to the location in which are at the moment.");
            return false;
        }

        return true;
    }


    public static TeleportPoint getRestartPoint(Player player, RestartType restartType) {
        return getRestartPoint(player, player.getLoc(), restartType);
    }

    public static TeleportPoint getRestartPoint(Player player, Location from, RestartType restartType) {
        final TeleportPoint teleportPoint = new TeleportPoint();

        Reflection r = player.getReflection();
        if (!r.isMain()) {
            if (r.getCoreLoc() != null)
                return teleportPoint.setLoc(r.getCoreLoc());
            else if (r.getReturnLoc() != null)
                return teleportPoint.setLoc(r.getReturnLoc());
        }

        Clan clan = player.getClan();
        if (clan != null) {
            int residenceId = 0;
            if (restartType == RestartType.AGIT) // If teleport to clan hall
                residenceId = clan.getHasHideout();
            else if (restartType == RestartType.CASTLE) // If teleport to castle
                residenceId = clan.getCastle();

            if (residenceId != 0) {
                Residence residence = ResidenceHolder.getInstance().getResidence(residenceId);
                if (residence != null) {
                    Reflection reflection = residence.getReflection(clan.getClanId());
                    if (reflection != null) {
                        teleportPoint.setLoc(residence.getOwnerRestartPoint());
                        teleportPoint.setReflection(reflection);
                        return teleportPoint;
                    }
                }
            }
        }

        if (player.isPK()) {
            if (player.getPKRestartPoint() != null)
                return teleportPoint.setLoc(player.getPKRestartPoint());
        } else {
            if (player.getRestartPoint() != null)
                return teleportPoint.setLoc(player.getRestartPoint());
        }

        RestartArea ra = MapRegionManager.getInstance().getRegionData(RestartArea.class, from);
        if (ra != null) {
            RestartPoint rp = ra.getRestartPoint().get(player.getRace());

            Location restartPoint = Rnd.get(rp.getRestartPoints());
            Location PKrestartPoint = Rnd.get(rp.getPKrestartPoints());

            return teleportPoint.setLoc(player.isPK() ? PKrestartPoint : restartPoint);
        }

        _log.atWarning().log("Cannot find restart location from coordinates: %s!", from);

        return teleportPoint.setLoc(DEFAULT_RESTART);
    }
}
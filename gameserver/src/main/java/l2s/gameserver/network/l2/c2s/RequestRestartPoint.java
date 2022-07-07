package l2s.gameserver.network.l2.c2s;

import gve.zones.GveZoneManager;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import l2s.commons.lang.ArrayUtils;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.instancemanager.GveRewardManager;
import l2s.gameserver.instancemanager.gve.GvePortalManager;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.TeleportPoint;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.base.ResidenceFunctionType;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventRestartLoc;
import l2s.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.entity.residence.ResidenceFunction;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.PortalInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.DiePacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.TeleportUtils;
import org.napile.pair.primitive.IntObjectPair;

public class RequestRestartPoint extends L2GameClientPacket
{
    private RestartType _restartType;

    @Override
    protected void readImpl()
    {
        _restartType = (RestartType) ArrayUtils.valid((Object[]) RestartType.VALUES, readD());
    }

    @Override
    protected void runImpl()
    {
        Player activeChar = getClient().getActiveChar();
        if(_restartType == null || activeChar == null)
            return;
        if(activeChar.isFakeDeath())
        {
            activeChar.breakFakeDeath();
            return;
        }
        if(!activeChar.isDead() && !activeChar.isGM())
        {
            activeChar.sendActionFailed();
            return;
        }
        switch(_restartType)
        {
            case ADVENTURES_SONG:
            {
                if(activeChar.getAbnormalList().containsEffects(22410) || activeChar.getAbnormalList().containsEffects(22411))
                {
                    activeChar.getAbnormalList().stopEffects(22410);
                    activeChar.getAbnormalList().stopEffects(22411);
                    activeChar.doRevive(100.0);
                    break;
                }
                activeChar.sendPacket(ActionFailPacket.STATIC, new DiePacket(activeChar));
                break;
            }
            case AGATHION:
            {
                if(activeChar.isAgathionResAvailable())
                {
                    activeChar.doRevive(100.0);
                    break;
                }
                activeChar.sendPacket(ActionFailPacket.STATIC, new DiePacket(activeChar));
                break;
            }
            case FIXED:
            {
                if(activeChar.getPlayerAccess().ResurectFixed)
                {
                    activeChar.doRevive(100.0);
                    break;
                }

                if(checkFeatherOfBlessingAvailable(activeChar))
                {
                    if(ItemFunctions.deleteItem(activeChar, 13300, 1L, true) || ItemFunctions.deleteItem(activeChar, 10649, 1L, true))
                    {
                        SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(7008, 1);
                        if(skillEntry != null)
                        {
                            activeChar.sendPacket(SystemMsg.YOU_HAVE_USED_THE_FEATHER_OF_BLESSING_TO_RESURRECT);
                            activeChar.doRevive(100.0);
                            skillEntry.getEffects(activeChar, activeChar);
                        }
                        break;
                    }
                    activeChar.sendPacket(ActionFailPacket.STATIC, new DiePacket(activeChar));
                    break;
                }

                if(!activeChar.getEvents().isEmpty())
                {
                    TeleportPoint teleportPoint = null;
                    Reflection ref = activeChar.getReflection();
                    for(Event e : activeChar.getEvents())
                    {
                        if (e.handleRevive(activeChar)) {
                            return;
                        }
                        EventRestartLoc eventRestartLoc = e.getRestartLoc(activeChar, _restartType);
                        if(eventRestartLoc != null && eventRestartLoc.getLoc() != null) {
                            Reflection reflection = eventRestartLoc.getReflection() != null ? eventRestartLoc.getReflection() : ref;
                            teleportPoint = new TeleportPoint(eventRestartLoc.getLoc(), reflection);
                        }
                    }

                    if(teleportPoint != null)
                    {
                        IntObjectPair<OnAnswerListener> ask = activeChar.getAskListener(false);
                        if(ask != null && ask.getValue() instanceof ReviveAnswerListener
                                && !((ReviveAnswerListener) ask.getValue()).isForPet())
                            activeChar.getAskListener(true);
                        GveRewardManager.getInstance().manageRevivePenalty(activeChar, false);
                        activeChar.setPendingRevive(true);

                        activeChar.teleToLocation(teleportPoint.getLoc(), teleportPoint.getReflection());

                        activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxHp());
                        activeChar.setCurrentCp(activeChar.getMaxCp());

                        break;
                    }
                }

                Location respawnLoc = GveZoneManager.getInstance().getClosestRespawnLoc(activeChar);
                if(respawnLoc != null)
                {
                    activeChar.sendMessage("You will be teleported in 5 seconds.");

                    ThreadPoolManager.getInstance().schedule(() ->
                    {
                        if(!activeChar.isDead())
                            return;

                        IntObjectPair<OnAnswerListener> ask = activeChar.getAskListener(false);
                        if(ask != null && ask.getValue() instanceof ReviveAnswerListener && !((ReviveAnswerListener) ask.getValue()).isForPet())
                            activeChar.getAskListener(true);
                        GveRewardManager.getInstance().manageRevivePenalty(activeChar, false);
                        activeChar.dispelDebuffs();
                        activeChar.setPendingRevive(true);
                        var loc = Location.findAroundPosition(respawnLoc, 250, activeChar.getGeoIndex());
                        activeChar.teleToLocation(loc);
                    }, 5, TimeUnit.SECONDS);

                    return;
                }

                activeChar.sendPacket(ActionFailPacket.STATIC, new DiePacket(activeChar));
                break;
            }
            default:
            {
                TeleportPoint teleportPoint = null;
                Reflection ref = activeChar.getReflection();
                if (ref.isMain()) {
                    for (Event e : activeChar.getEvents()) {
                        EventRestartLoc eventRestartLoc = e.getRestartLoc(activeChar, _restartType);
                        if (eventRestartLoc != null && eventRestartLoc.getLoc() != null) {
                            Reflection reflection = eventRestartLoc.getReflection() != null ? eventRestartLoc.getReflection() : ref;
                            teleportPoint = new TeleportPoint(eventRestartLoc.getLoc(), reflection);
                        }
                    }
                }
                if(teleportPoint == null)
                    teleportPoint = defaultPoint(_restartType, activeChar);
                if(teleportPoint != null)
                {
                    long delay = 0;
                    if(_restartType == RestartType.TO_CASTLE)
                    {
                        Fraction f = activeChar.getFraction();
                        for(Castle c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
                        {
                            if(c.getSiegeEvent() != null && c.getFraction() != f && c.getSiegeEvent().isInProgress() && c.getSiegeEvent().getPlayersInZone().contains(activeChar))
                            {
                                delay = TimeUnit.SECONDS.toMillis(8);
                                break;
                            }
                        }
                    }
                    else if(_restartType == RestartType.TO_PORTAL)
                    {
                        delay = TimeUnit.SECONDS.toMillis(8);
                    }

                    if(delay > 0)
                        activeChar.sendMessage("You will be teleported at 8 seconds.");

                    AtomicReference<TeleportPoint> tp = new AtomicReference<>(teleportPoint);
                    ThreadPoolManager.getInstance().schedule(() ->
                    {
                        if(!activeChar.isDead())
                            return;

                        if(_restartType == RestartType.TO_PORTAL)
                        {
                            Optional<NpcInstance> closestPortal = GvePortalManager.getInstance().getClosestPortalOrFlag(activeChar);
                            if(closestPortal.isPresent() && closestPortal.get() instanceof PortalInstance)
                            {
                                Location loc = Location.findAroundPosition(closestPortal.get(), 30);
                                tp.set(new TeleportPoint(loc));
                            }
                            else
                            {
                                tp.set(TeleportUtils.getRestartPoint(activeChar, RestartType.TO_VILLAGE));
                            }
                        }

                        TeleportPoint point = tp.get();

                        IntObjectPair<OnAnswerListener> ask = activeChar.getAskListener(false);
                        if(ask != null && ask.getValue() instanceof ReviveAnswerListener && !((ReviveAnswerListener) ask.getValue()).isForPet())
                            activeChar.getAskListener(true);
                        GveRewardManager.getInstance().manageRevivePenalty(activeChar, false);
                        activeChar.setPendingRevive(true);

                        activeChar.dispelDebuffs();

                        activeChar.teleToLocation(point.getLoc(), point.getReflection());

                        activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxHp());
                        activeChar.setCurrentCp(activeChar.getMaxCp());
                    }, delay);
                    break;
                }
                activeChar.sendPacket(ActionFailPacket.STATIC, new DiePacket(activeChar));
                break;
            }
        }
    }

    private static boolean checkFeatherOfBlessingAvailable(Player player)
    {
        return !player.getAbnormalList().containsEffects(7008) && (ItemFunctions.haveItem(player, 13300, 1L) || ItemFunctions.haveItem(player, 10649, 1L));
    }

    public static TeleportPoint defaultPoint(RestartType restartType, Player activeChar)
    {
        TeleportPoint teleportPoint = null;
        Clan clan = activeChar.getClan();
        switch(restartType)
        {
            case TO_FORTRESS:
                FortressSiegeEvent event = (FortressSiegeEvent) activeChar.getZoneEvents()
                    .stream()
                    .filter(e -> e instanceof FortressSiegeEvent)
                    .findFirst()
                    .orElse(null);
                if (event != null && (event.getOwnerFraction() == activeChar.getFraction())) {
                    Location restartPoint = event.getResidence().getOwnerRestartPoint();
                    teleportPoint = new TeleportPoint(restartPoint);
                }
                break;
            case TO_CLANHALL:
            {
                if(clan != null && clan.getHasHideout() != 0)
                {
                    ClanHall clanHall = activeChar.getClanHall();
                    teleportPoint = TeleportUtils.getRestartPoint(activeChar, RestartType.TO_CLANHALL);
                    ResidenceFunction function = clanHall.getActiveFunction(ResidenceFunctionType.RESTORE_EXP);
                    if(function != null)
                        activeChar.restoreExp(function.getTemplate().getExpRestore() * 100.0);
                    break;
                }
                break;
            }
            case TO_CASTLE:
            {
                Fraction f = activeChar.getFraction();

                Location loc = null;

                for(Castle c : ResidenceHolder.getInstance().getResidenceList(Castle.class))
                {
                    if(c.getSiegeEvent() != null && c.getSiegeEvent().isInProgress()
                            && (c.getSiegeEvent().getPlayersInZone().contains(activeChar)
                            || loc == null && c.getFraction() == f))
                    {
                        loc = c.getRestartPoint(activeChar);

                        ResidenceFunction function = c.getActiveFunction(ResidenceFunctionType.RESTORE_EXP);
                        if(function != null)
                            activeChar.restoreExp(function.getTemplate().getExpRestore() * 100.0);

                        break;
                    }
                }

                if(loc != null)
                    teleportPoint = new TeleportPoint(loc);

                break;
            }
            case TO_PORTAL:
            {
                Optional<NpcInstance> closestPortal = GvePortalManager.getInstance().getClosestPortalOrFlag(activeChar);
                if(closestPortal.isPresent() && closestPortal.get() instanceof PortalInstance)
                {
                    ((PortalInstance) closestPortal.get()).decreaseTeleportsLeft();
                    Location loc = Location.findAroundPosition(closestPortal.get(), 30);
                    teleportPoint = new TeleportPoint(loc);
                }
                else
                    teleportPoint = TeleportUtils.getRestartPoint(activeChar, RestartType.TO_VILLAGE);
                break;
            }
            default:
            {
                teleportPoint = TeleportUtils.getRestartPoint(activeChar, RestartType.TO_VILLAGE);
                break;
            }
        }

        return teleportPoint;
    }
}

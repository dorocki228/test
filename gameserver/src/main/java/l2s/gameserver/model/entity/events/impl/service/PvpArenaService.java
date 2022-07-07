package l2s.gameserver.model.entity.events.impl.service;

import l2s.commons.util.Rnd;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.base.SpecialEffectState;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.impl.PvpArenaEvent;
import l2s.gameserver.model.entity.events.impl.pvparena.PvpArenaFactionTeam;
import l2s.gameserver.model.entity.events.impl.pvparena.PvpArenaPlayer;
import l2s.gameserver.model.entity.events.impl.pvparena.PvpArenaZone;
import l2s.gameserver.model.entity.events.objects.ZoneObject;
import l2s.gameserver.model.snapshot.SnapshotPlayer;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.ChangeWaitTypePacket;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.RevivePacket;
import l2s.gameserver.service.PlayerService;
import l2s.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class PvpArenaService {
    private static final PvpArenaService INSTANCE = new PvpArenaService();

    private PvpArenaService() {
    }

    public static PvpArenaService getInstance() {
        return INSTANCE;
    }

    public List<PvpArenaZone> getZonesFromConfiguration(PvpArenaEvent pvpArenaEvent, String[] activeZones) {
        Objects.requireNonNull(pvpArenaEvent);
        Objects.requireNonNull(activeZones);
        List<PvpArenaZone> zones = new ArrayList<>();
        for (String activeZone : activeZones) {
            List<Object> objects = pvpArenaEvent.getObjects(activeZone);
            List<Location> points = new ArrayList<>();
            ZoneObject zoneObject = null;
            for (Object object : objects) {
                if (object instanceof Location) {
                    points.add(((Location) object));
                } else if (object instanceof ZoneObject) {
                    zoneObject = ((ZoneObject) object);
                }
            }
            PvpArenaZone pvpArenaZone = new PvpArenaZone(zoneObject, points);
            zones.add(pvpArenaZone);
        }
        return zones;
    }

    public boolean teleportPlayerToArena(Player player, List<Location> points, Reflection reflection) {
        if (player == null || points == null || reflection == null) {
            return false;
        }
        Location point = Rnd.get(points);
        Objects.requireNonNull(point);
        return player.teleToLocation(point, reflection);
    }

    public Map<Fraction, PvpArenaFactionTeam> createTeams(Map<Fraction, List<Player>> fractionMap) {
        return fractionMap
                .entrySet()
                .stream()
                .map(i -> {
                    Map<Integer, PvpArenaPlayer> map = i.getValue().stream()
                            .collect(Collectors.toMap(GameObject::getObjectId, p -> {
                                SnapshotPlayer snapshot = PlayerService.getInstance().createSnapshot(p);
                                return new PvpArenaPlayer(p, snapshot);
                            }));
                    return new PvpArenaFactionTeam(i.getKey(), map);
                })
                .collect(Collectors.toMap(PvpArenaFactionTeam::getFraction, p -> p));
    }
}

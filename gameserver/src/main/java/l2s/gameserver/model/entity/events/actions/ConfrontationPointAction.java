package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.EventAction;
import l2s.gameserver.security.HwidUtils;
import l2s.gameserver.service.ConfrontationService;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ConfrontationPointAction implements EventAction {
    private final Supplier<Integer> supplierPoints;

    public ConfrontationPointAction(Supplier<Integer> supplierPoints) {
        this.supplierPoints = supplierPoints;
    }

    @Override
    public void call(Event event) {
        int points = supplierPoints.get();
        Collection<Player> collection = HwidUtils.INSTANCE.filterSameHwids(event.itemObtainPlayers()).stream()
                .filter(p -> !p.isDead())
                .filter(p -> p.getLevel() >= Config.FACTION_WAR_MIN_LEVEL)
                .collect(Collectors.toUnmodifiableList());

        ConfrontationService.getInstance().siege(collection, points);
    }
}

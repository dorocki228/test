package l2s.gameserver.instancemanager.gve;

import com.google.common.collect.ImmutableSortedSet;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.OutpostInstance;
import l2s.gameserver.model.instances.PortalInstance;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GvePortalManager
{
    private static final GvePortalManager _instance = new GvePortalManager();

    private final int MAX_FRACTION_PORTALS = 3;

    private final Map<Integer, PortalInstance> personal = new ConcurrentHashMap<>();
    private final Map<Integer, PortalInstance> fractionPortals = new ConcurrentHashMap<>();

    public static GvePortalManager getInstance()
    {
        return _instance;
    }

    public List<PortalInstance> getFractionPortals(Player p)
    {
        return getFractionPortals(p.getFraction());
    }

    public List<PortalInstance> getFractionPortals(Fraction fraction)
    {
        return fractionPortals.values().stream()
                .filter(portal -> !portal.getFraction().canAttack(fraction))
                .collect(Collectors.toList());
    }

    public PortalInstance getPersonalPortals(Player p)
    {
        return personal.get(p.getObjectId());
    }

    public void addPortal(PortalInstance p)
    {
        Player owner = p.getPlayer();

        switch(p.getNpcId())
        {
            case 40045:
            case 40047:
            {
                //personal
                if(owner != null)
                {
                    PortalInstance old = personal.get(owner.getObjectId());
                    if(old != null)
                        old.deleteMe();

                    personal.put(owner.getObjectId(), p);
                }
                break;
            }
            case 40046:
            case 40048:
            {
                //fraction
                if(owner != null)
                {
                    List<PortalInstance> oldPorts = fractionPortals.values().stream()
                            .filter(op -> op.getPlayer() != null && op.getPlayer().getObjectId() == owner.getObjectId())
                            .collect(Collectors.toList());

                    if(oldPorts.size() >= MAX_FRACTION_PORTALS)
                    {
                        PortalInstance op = oldPorts.remove(MAX_FRACTION_PORTALS - 1);
                        fractionPortals.remove(op.getObjectId());
                        op.deleteMe();
                    }

                    fractionPortals.put(p.getObjectId(), p);
                }
                break;
            }
        }
    }

    public void removePortal(PortalInstance p)
    {
        switch(p.getNpcId())
        {
            case 40045:
            case 40047:
            {
                //personal
                Player owner = p.getPlayer();
                if(owner != null)
                    personal.remove(owner.getObjectId());
                break;
            }
            case 40046:
            case 40048:
            {
                //fractionPortals
                fractionPortals.remove(p.getObjectId());
                break;
            }
        }
    }

    public Optional<NpcInstance> getClosestPortalOrFlag(Player player)
    {
        List<PortalInstance> fractionPortals = getFractionPortals(player);
        PortalInstance personalPortals = getPersonalPortals(player);
        int npcId = player.getFraction() == Fraction.FIRE ? OutpostInstance.FIRE_FLAG : OutpostInstance.WATER_FLAG;
        List<NpcInstance> flags = GameObjectsStorage.getAllByNpcId(npcId, true);

        ImmutableSortedSet.Builder<NpcInstance> builder =
                new ImmutableSortedSet.Builder<NpcInstance>(Comparator.comparingDouble(player::getDistance))
                        .addAll(fractionPortals)
                        .addAll(flags);
        if(personalPortals != null)
        {
            builder.add(personalPortals);
        }

        ImmutableSortedSet<NpcInstance> portals = builder.build();
        return portals.isEmpty() ? Optional.empty() : Optional.of(portals.first());
    }

    public boolean showToFlagOnDie(Player player)
    {
        Optional<NpcInstance> closestPortalOrFlag = getClosestPortalOrFlag(player);
        return closestPortalOrFlag.isPresent() && closestPortalOrFlag.get() instanceof PortalInstance;
    }
}

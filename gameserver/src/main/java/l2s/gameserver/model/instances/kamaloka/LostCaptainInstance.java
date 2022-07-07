package l2s.gameserver.model.instances.kamaloka;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.KamalokaInstance;
import l2s.gameserver.model.instances.ReflectionBossInstance;
import l2s.gameserver.templates.InstantZone;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.data.ChancedItemData;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;

import java.util.List;

public class LostCaptainInstance extends ReflectionBossInstance {
    public LostCaptainInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
    }

    @Override
    protected void onDeath(Creature killer) {
        Location location = getLoc();
        Reflection r = getReflection();
        r.setReenterTime(System.currentTimeMillis());
        super.onDeath(killer);
        InstantZone iz = r.getInstancedZone();
        if(iz != null) {
            final int returnNpc = iz.getAddParams().getInteger("returnNpc", -1);
            if(returnNpc == -1)
                return;
            String returnNpcLoc = iz.getAddParams().getString("returnNpcLoc", null);
            if(returnNpcLoc != null) {
                final NpcTemplate template = NpcHolder.getInstance().getTemplate(returnNpc);
                if(template == null)
                    return;
                KamalokaInstance npc = new KamalokaInstance(IdFactory.getInstance().getNextId(), template, StatsSet.EMPTY);
                npc.setSpawnedLoc(Location.parseLoc(returnNpcLoc));
                npc.setReflection(r);
                npc.spawnMe(npc.getSpawnedLoc());
            }
            List<ChancedItemData> rewards = iz.getRewards();
            if(!rewards.isEmpty())
                getReflection().getPlayers().stream().
                        filter(p-> location.distance(p.getLoc()) <= 1000).
                        forEach(p -> rollReward(rewards, p));
        }
    }

    private static void rollReward(List<ChancedItemData> rewards, Player player) {
        rewards.stream().filter(p -> Rnd.chance(p.getChance())).forEach(p -> ItemFunctions.addItem(player, p.getId(), p.getCount(), true));
    }
}

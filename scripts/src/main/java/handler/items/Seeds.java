package handler.items;

import l2s.gameserver.component.farm.GatheringTemplate;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.data.xml.holder.SteadDataHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.farm.Stead;
import l2s.gameserver.model.farm.SteadBarnManager;
import l2s.gameserver.model.farm.zone.SteadZone;
import l2s.gameserver.model.instances.residences.farm.SeedInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Seeds extends SimpleItemHandler {
    @Override
    protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl) {
        final int itemId = item.getItemId();
        if (player.getInventory().getCountOf(77000) < 1 && player.getInventory().getCountOf(77001) < 1) {
            player.sendPacket(new CustomMessage("farmer.string.s5"));
            return false;
        }

        if (player.getLevel() < 80) {
            player.sendPacket(new CustomMessage("farmer.conditions.level").addNumber(80));
            return false;
        }

        if (player.getPvpKills() < 50) {
            player.sendPacket(new CustomMessage("farmer.conditions.pvpkills").addNumber(50));
            return false;
        }

        GatheringTemplate gathering = SteadDataHolder.getInstance().getGathering(itemId);
        if (gathering == null) {
            player.sendPacket(new CustomMessage("farmer.string.s4"));
            return false;
        }

        SteadZone zone = SteadZone.class.cast(player.getZone(Zone.ZoneType.STEAD));
        if (zone.getStead().getPossession().getFraction() != player.getFraction()) {
            player.sendPacket(new CustomMessage("farmer.string.s3"));
            return false;
        }

        if (!SteadBarnManager.getInstance().checkHwid(player)) {
            player.sendPacket(new CustomMessage("farmer.string.s8"));
            return false;
        }

        Stead stead = zone.getStead();
        if (stead.checkSeeds(player)) {
            List<Location> list = new ArrayList<>(zone.getPoints());
            stead.getSeeds(player).forEach(loc -> list.remove(loc.getLoc()));
            Location point = getClosestLocation(player, list);
            if (point == null) {
                player.sendPacket(new CustomMessage("farmer.string.s2"));
                return false;
            }

            SteadBarnManager.getInstance().addHwid(player);

            SimpleSpawner spawn = new SimpleSpawner(NpcHolder.getInstance().getTemplate(gathering.getModel(0)));
            spawn.setLoc(point);
            spawn.setAmount(1);
            spawn.setRespawnDelay(0);
            spawn.setReflection(player.getReflection());
            final SeedInstance seed = SeedInstance.class.cast(spawn.spawnOne());
            spawn.stopRespawn();
            seed.init(player, gathering);
            seed.setStead(stead);
            stead.getSeeds(player).add(seed);
            // При спавне пакет не отправляется на клиент из за отсутствия овнера в инстансе, потому отправляем повторно
            player.sendPacket(player.addVisibleObject(seed, null));
            ItemFunctions.deleteItem(player, item, 1);
            return true;
        }

        player.sendPacket(new CustomMessage("farmer.string.s1"));
        return false;
    }

    private Location getClosestLocation(Player player, List<Location> list) {
        return list.stream().min(Comparator.comparing(distance -> distance.distance3D(player.getLoc()))).orElse(null);
    }
}

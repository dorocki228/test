package npc.model.events;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

/**
 * @author Java-man
 * @since 08.09.2018
 */
public class BossSpawnEventTeleporterInstance extends NpcInstance
{
    public static final Location LOCATION = new Location(50856, -12232, -9384);

    public BossSpawnEventTeleporterInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
    {
        super(objectId, template, set);
    }

    @Override
    public void onBypassFeedback(Player player, String command)
    {
        if("teleport_to_event".equals(command))
        {
           player.teleToLocation(LOCATION);
           var message = player.isLangRus()
                         ? "Вы будете телепортированы к Эпическому Боссу в течении 2х минут " +
                                 "или в течении 4х минут если вражеская фракция владеет всеми замками."
                         : "You will be teleported to the Epic Boss within 2 minutes " +
                                 "or within 4 minutes if the enemy faction owns all the castles.";
           player.sendMessage(message);
        }
        else
            super.onBypassFeedback(player, command);
    }
}

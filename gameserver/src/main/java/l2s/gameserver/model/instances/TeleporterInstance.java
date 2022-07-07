package l2s.gameserver.model.instances;

import gnu.trove.map.hash.TIntObjectHashMap;
import gve.zones.GveZoneManager;
import gve.zones.model.GveOutpost;
import gve.zones.model.GveZone;
import gve.zones.model.impl.PvpZone;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.instancemanager.ReflectionManager;
import l2s.gameserver.instancemanager.gve.GvePortalManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.TeleportPoint;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.bbs.ArtifactTeleportationCommunityBoardEntry;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.FortressSiegeEvent;
import l2s.gameserver.model.entity.residence.Fortress;
import l2s.gameserver.model.instances.residences.castle.BlockpostTeleportInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.service.CommunityBoardService;
import l2s.gameserver.service.FractionService;
import l2s.gameserver.service.LocationBalancerService;
import l2s.gameserver.service.PaidActionsStatsService;
import l2s.gameserver.service.PaidActionsStatsService.PaidActionType;
import l2s.gameserver.skills.skillclasses.SummonPortal.TeleportType;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.TeleportUtils;
import org.apache.commons.lang3.StringUtils;

public class TeleporterInstance extends NpcInstance
{
    private final TIntObjectHashMap<Location[]> fortressTeleports = new TIntObjectHashMap<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");

    public TeleporterInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
    {
        super(objectId, template, set);

        fortressTeleports.put(400, new Location[]{
                new Location(78296, 90552, -2880),
                new Location(79960, 89224, -2880),
                new Location(79688, 89896, -2440),
                new Location(80408, 91288, -2880),
                new Location(79688, 92952, -2440),
                new Location(77512, 92872, -2880)});

        fortressTeleports.put(401, new Location[]{
                new Location(63186, 69655, -3024),
                new Location(63128, 67704, -3024),
                new Location(60136, 69496, -3024),
                new Location(62088, 68360, -2576),
                new Location(63384, 69800, -3024),
                new Location(61176, 69624, -3024)});

        fortressTeleports.put(402, new Location[]{
                new Location(47832, 91736, -2976),
                new Location(48552, 91704, -2680),
                new Location(47096, 90039, -2976),
                new Location(46888, 90776, -2976),
                new Location(45224, 92648, -2976),
                new Location(47032, 92760, -2976)});
    }

    @Override
    public void onBypassFeedback(Player player, String command)
    {
        if(command.startsWith("portal"))
        {
            String[] arg = command.split("_");
            TeleportType type = TeleportType.values()[Integer.parseInt(arg[1])];
            int objId = Integer.parseInt(arg[2]);
            Optional<PortalInstance> portal = Optional.empty();
            switch(type)
            {
                case PERSONAL:
                {
                    PortalInstance p = GvePortalManager.getInstance().getPersonalPortals(player);
                    portal = Optional.ofNullable(p);
                    break;
                }
                case FRACTION:
                {
                    List<PortalInstance> list = GvePortalManager.getInstance().getFractionPortals(player);
                    portal = list.stream()
                            .filter(p -> p.getObjectId() == objId)
                            .findFirst();
                    break;
                }
            }

            portal.ifPresent(portalInstance ->
            {
                Location loc = Location.coordsRandomize(portalInstance.getLoc(), 50, 150);
                if(LocationBalancerService.getInstance().canTeleport(player, loc))
                {
                    if (!player.isPhantom() && player.tScheme_record.isLogging())
                        player.tScheme_record.setTeleport(loc);

                    player.teleToLocation(loc);
                    portalInstance.decreaseTeleportsLeft();
                }
            });
        }
        else if(command.startsWith("outpost"))
        {
            String[] args = command.split(" ");

            GveOutpost outpost = GveOutpost.valueOf(args[1]);
            int id = Integer.parseInt(args[2]);

            if(!StringUtils.containsIgnoreCase(outpost.name(), player.getFraction().name()))
                return;

            int status = outpost.getStatus();

            Location loc = null;

            switch(status)
            {
                case GveOutpost.ALIVE:
                {
                    loc = outpost.getMain();

                    break;
                }
                case GveOutpost.ATTACKED:
                {
                    List<Location> locations = outpost.getLocations();
                    if(id >= locations.size())
                        return;

                    loc = locations.get(id);

                    break;
                }
                case GveOutpost.DEAD:
                {
                    return;
                }
            }

            if(GveZoneManager.getInstance().getActiveZones().stream()
                    .filter(z -> z.getOutposts(player.getFraction()).contains(outpost))
                    .anyMatch(z -> !z.canEnterZone(player)))
            {
                return;
            }

            if(loc != null) {
                Location coordsRandomize = Location.coordsRandomize(loc, 50, 150);
                if(LocationBalancerService.getInstance().canTeleport(player, coordsRandomize)) {
                    if (payForTeleport(player)) {
                        if (!player.isPhantom() && player.tScheme_record.isLogging())
                            player.tScheme_record.setTeleport(coordsRandomize);
                        player.teleToLocation(coordsRandomize);
                    }
                }
            }
        }
        else if(Objects.equals(command, "goHome"))
        {
            TeleportPoint loc = TeleportUtils.getRestartPoint(player, RestartType.TO_VILLAGE);

            if (!player.isPhantom() && player.tScheme_record.isLogging())
                player.tScheme_record.setTeleport(loc.getLoc());

            player.teleToLocation(loc.getLoc(), ReflectionManager.MAIN);
        }
        else if(command.startsWith("pvp"))
        {
            String zoneName = command.substring(4);
            List<GveZone> zones = GveZoneManager.getInstance().getActiveZones();
            zones.stream()
                    .filter(z -> z.getType() == Zone.ZoneType.gve_pvp)
                    .filter(z -> z.getName().equals(zoneName))
                    .findAny()
                    .ifPresent(z -> {
                        if (payForTeleport(player)) {
                            if (!player.isPhantom() && player.tScheme_record.isLogging())
                                player.tScheme_record.setTeleport(z.getRandomRespawnLoc(player));

                            player.teleToLocation(z.getRandomRespawnLoc(player));
                        }
                    });
        } else if (command.startsWith("artifact")) {
            int artifactObjId = Integer.parseInt(command.substring(9));
            final ArtifactInstance artifact = (ArtifactInstance) GameObjectsStorage.getNpc(artifactObjId);
            if (artifact != null) {
                final Location randomLoc = Rnd.get(artifact.entity.getTemplate().getTeleportLocations());
                if (randomLoc != null) {
                    final Location location = Location.findAroundPosition(randomLoc, 30, artifact.getGeoIndex());

                    if (!player.isPhantom() && player.tScheme_record.isLogging())
                        player.tScheme_record.setTeleport(location);

                    player.teleToLocation(location);
                }
            }
        } else if (command.startsWith("fort")) {
            int id = Integer.parseInt(command.substring(5));
            Fortress fort = ResidenceHolder.getInstance().getResidence(Fortress.class, id);
            if (fort == null) {
                return;
            }

            if (fort.getFraction() == player.getFraction()) {
                if (payForTeleport(player)) {
                    player.teleToLocation(fort.getOwnerRestartPoint());
                }
            } else {
                player.sendMessage("You cannot move to enemy fortress.");
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    @Override
    public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace)
    {
        String filename = getHtmlPath(getHtmlFilename(val, player), player);
        String html = HtmCache.getInstance().getHtml(filename, player);
        HtmlMessage packet = new HtmlMessage(this).setPlayVoice(firstTalk);
        packet.setHtml(html);

        int firePer = FractionService.getInstance().getFractionPlayersCountPercentage(Fraction.FIRE);
        int waterPer = FractionService.getInstance().getFractionPlayersCountPercentage(Fraction.WATER);

        packet.replace("%fire%", String.valueOf(firePer));
        packet.replace("%water%", String.valueOf(waterPer));

        Fraction f = player.getFraction();

        StringBuilder sb = new StringBuilder();

        //блок возврата в город для аванпостов
        if(this instanceof OutpostInstance || this instanceof BlockpostTeleportInstance)
        {
            sb.append("<table>");
            sb.append("<tr>");
            sb.append("<td width=13 height=20></td>");
            sb.append("<td width=16 ></td>");
            sb.append("<td width=133 ><font color=ffa500>Town</font></td>");
            sb.append("<td width=70 align=center></td>");
            sb.append("<td width=30 align=center></td>");
            sb.append("</tr>");
            sb.append("</table>");
            sb.append("<table>");
            sb.append("<tr>");
            // sb.append("<td width=15 height=20></td>");
            sb.append("<td width=150 ><Button ALIGN=LEFT ICON=\"TELEPORT\"  color=\"red-text\" action=\"bypass -h npc_%objectId%_goHome\">").append(Fraction.getTown(f,player.getLanguage())).append("</Button></td>");
            sb.append("<td width=70 align=center></td>");
            sb.append("<td width=30 align=center></td>");
            sb.append("</tr>");
            sb.append("</table>");
        }
        packet.replace("%goHome%", sb.toString());
        sb.setLength(0);
        //блок возврата в город для аванпостов конец

        //блок зашиты форта если на него атака скрывается если нет зон под атакой
        for(int id : fortressTeleports.keys())
        {
            FortressSiegeEvent siegeEvent = EventHolder.getInstance().getEvent(EventType.SIEGE_EVENT, id);

            if(siegeEvent != null && siegeEvent.isInProgress())
            {
                Fortress r = siegeEvent.getResidence();
                if(!f.canAttack(r.getFraction()))
                {
                    if(sb.length() == 0)
                    {
                        sb.append("<table>");
                        sb.append("<tr>");
                        sb.append("<td width=13 height=20></td>");
                        sb.append("<td width=16 ><img src=L2UI_CH3.blueshield width=16 height=16></td>");
                        sb.append("<td width=243 ><font color=ffa500>Need protection fortress</font></td>");
                        sb.append("</tr>");
                        sb.append("</table>");
                    }

                    sb.append("<table>");
                    sb.append("<tr>");
                    sb.append("<td width=15 height=20></td>");

                    Location[] locs = fortressTeleports.get(id);

                    sb.append("<td width=110 ><button action=\"bypass -h npc_%objectId%_teleport_").append(locs[0].x).append("_").append(locs[0].y).append("_").append(locs[0].z).append("\" value=\"").append(r.getName()).append("\" width=110 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"></td>");

                    for(int i = 1; i < locs.length; i++)
                        sb.append("<td width=15 ><button action=\"bypass -h npc_%objectId%_teleport_").append(locs[i].x).append("_").append(locs[i].y).append("_").append(locs[i].z).append("\" value=\"T").append(i).append("\" width=18 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"></td>");

                    sb.append("<td width=55 ><font color=ff2a0e>Attack</font></td>");
                    sb.append("</tr>");
                    sb.append("</table>");
                }
            }
        }

        packet.replace("%fortressProtectionBlock%", sb.toString());
        sb.setLength(0);
        //блок зашиты форта конец

        //Блок фракционных порталов скрывается если их нет

        PortalInstance personal = GvePortalManager.getInstance().getPersonalPortals(player);

        if(personal != null && val == 0)
        {
            if(sb.length() == 0)
            {
                sb.append("<table>");
                sb.append("<tr>");
                sb.append("<td width=13 height=20></td>");
                sb.append("<td width=16 ><img src=L2UI_CH3.chatting_msn1_down width=16 height=16></td>");
                sb.append("<td width=133 ><font color=ffa500>Personal/Faction teleports</font></td>");
                sb.append("<td width=70 align=center><font color=a4a4a4>Time</font></td>");
                sb.append("<td width=30 align=center><font color=a4a4a4>TP</font></td>");
                sb.append("</tr>");
                sb.append("</table>");
            }

            sb.append("<table>");
            // допускается 1 портал 6ыстым, персональным если его нет то так же выводится 5 порталов глобальным
            sb.append("<tr>");
            //sb.append("<td width=15 height=20></td>");
            sb.append("<td width=150><button action=\"bypass -h npc_%objectId%_portal_0_").append(personal.getObjectId()).append("\" value=\"").append(personal.getName()).append("\" width=150 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"></td>");
            sb.append("<td width=70 align=center>").append(sdf.format(new Date(TimeUnit.MINUTES.toMillis(5) - (System.currentTimeMillis() - personal.getTimeToDelete())))).append("</td>");
            sb.append("<td width=30 align=center>").append(personal.getTeleportsLeft()).append("</td>");
            sb.append("</tr>");
            // персональный портал
        }

        List<PortalInstance> fraction = GvePortalManager.getInstance().getFractionPortals(player);

        int portsOnPage = 5;

        int start = val * portsOnPage;

        if(val != 0 && personal != null)
            start -= 1;

        int end = start + portsOnPage - (personal != null ? 1 : 0);

        for(int i = start; i < end; i++)
        {
            if(fraction.size() <= i)
                continue;

            PortalInstance p = fraction.get(i);
            if(p != null)
            {
                if(sb.length() == 0)
                {
                    sb.append("<table>");
                    sb.append("<tr>");
                    sb.append("<td width=13 height=20></td>");
                    sb.append("<td width=16 ><img src=L2UI_CH3.chatting_msn1_down width=16 height=16></td>");
                    sb.append("<td width=133 ><font color=ffa500>Personal/Faction teleports</font></td>");
                    sb.append("<td width=70 align=center><font color=a4a4a4>Time</font></td>");
                    sb.append("<td width=30 align=center><font color=a4a4a4>TP</font></td>");
                    sb.append("</tr>");
                    sb.append("</table>");
                    sb.append("<table>");
                }

                sb.append("<tr>");
                sb.append("<td width=15 height=20></td>");
                sb.append("<td width=150><button action=\"bypass -h npc_%objectId%_portal_1_").append(p.getObjectId()).append("\" value=\"").append(p.getName()).append("\" width=150 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"></td>");
                sb.append("<td width=70 align=center>").append(sdf.format(new Date(TimeUnit.MINUTES.toMillis(5) - (System.currentTimeMillis() - p.getTimeToDelete())))).append("</td>");
                sb.append("<td width=30 align=center>").append(p.getTeleportsLeft()).append("</td>");
                sb.append("</tr>");
            }
        }

        if(personal != null || !fraction.isEmpty())
            sb.append("</table>");

        int size = fraction.size() + (personal != null ? 1 : 0);

        if(size > portsOnPage)
        {
            sb.append("<table>");
            sb.append("<tr>");
            sb.append("<td width=15></td>");
            sb.append("<td>");
            sb.append("<table width=260 >");
            sb.append("<tr>");
            int pages = size / portsOnPage;
            for(int i = 0; i <= pages; i++)
            {
                sb.append("<td height=16 width=16>");
                sb.append("<center><a action=\"bypass -h npc_%objectId%_Chat ").append(i).append("\">").append(i + 1).append("</a></center>");
                sb.append("</td>");
            }
            sb.append("</tr>");
            sb.append("</table>");
            sb.append("</td>");
            sb.append("</tr>");
            sb.append("</table>");
            sb.append("<br>");
        }

        packet.replace("%portalBlock%", sb.toString());
        sb.setLength(0);
        //	конец порталам

        List<GveZone> zones = GveZoneManager.getInstance().getActiveZones();

        // зоны блок 1
        sb.append("<table>");
        sb.append("<tr>");
        sb.append("<td width=13 height=20></td>");
        sb.append("<td width=260 ><font color=ffa500>Start Location</font><font color=a4a4a4> [Max. Lv. 70, C-Grade, Enchant +5]</font></td>");
        sb.append("</tr>");
        sb.append("</table>");

        zones.stream()
                .filter(temp -> temp.getType() == Zone.ZoneType.gve_low)
                .map(z -> z.getOutposts(f))
                .forEach(outposts -> makeOutpostInfo(player, outposts, sb));
        // зоны блок 1

        // зоны блок 2

        sb.append("<table>");
        sb.append("<tr>");
        sb.append("<td width=13 height=20></td>");
        sb.append("<td width=260 ><font color=ffa500>Mid Location</font><font color=a4a4a4> [All allowed]</font></td>");
        sb.append("</tr>");
        sb.append("</table>");

        zones.stream()
                .filter(temp -> temp.getType() == Zone.ZoneType.gve_mid || temp.getType() == Zone.ZoneType.gve_static_mid)
                .map(z -> z.getOutposts(f))
                .forEach(outposts -> makeOutpostInfo(player, outposts, sb));

        sb.append("<table>");
        sb.append("<tr>");
        sb.append("<td width=13 height=20></td>");
        sb.append("<td width=260 ><font color=ffa500>High Location</font><font color=a4a4a4> [All allowed]</font></td>");
        sb.append("</tr>");
        sb.append("</table>");

        zones.stream()
                .filter(temp -> temp.getType() == Zone.ZoneType.gve_high || temp.getType() == Zone.ZoneType.gve_static_high)
                .map(z -> z.getOutposts(f))
                .forEach(outposts -> makeOutpostInfo(player, outposts, sb));

        // зоны блок 2

        // зоны блок 3
        sb.append("<table>");
        sb.append("<tr>");
        sb.append("<td width=13 height=20></td>");
        sb.append("<td width=260 ><font color=ffa500>PvP Location</font></td>");
        sb.append("</tr>");
        sb.append("</table>");

        zones.stream()
                .filter(temp -> temp.getType() == Zone.ZoneType.gve_pvp)
                .map(temp -> (PvpZone) temp)
                .forEach(z ->
                {
                    sb.append("<table>");
                    sb.append("<tr>");
                    // sb.append("<td width=15 height=20></td>");
                    sb.append("<td width=150><Button ALIGN=LEFT ICON=\"TELEPORT\"  color=\"red-text\" action=\"bypass -h npc_%objectId%_pvp " + z.getName() + " \"><font color=\"7D2EE1\">").append(z.getInGameName()).append("</font></Button></td>");
                    sb.append("<td width=15 ></td>");
                    sb.append("<td width=15 ></td>");
                    sb.append("<td width=15 ></td>");
                    sb.append("<td width=70 ></td>");
                    sb.append("</tr>");
                    sb.append("</table>");
                });
        // зоны блок 3

        packet.replace("%zonesBlock%", sb.toString());
        sb.setLength(0);

        final List<ArtifactTeleportationCommunityBoardEntry> artifactsUnderAttack = CommunityBoardService.getInstance()
                .getEntries(ArtifactTeleportationCommunityBoardEntry.class).stream()
                .filter(a -> a.getFaction() == player.getFraction())
                .collect(Collectors.toList());

        if (!artifactsUnderAttack.isEmpty()) {
            // Артефакты под атакой
            sb.append("<table>");
            sb.append("<tr>");
            sb.append("<td width=13 height=20></td>");
            sb.append("<td width=260 ><font color=ffa500>Artifacts under attack</font></td>");
            sb.append("</tr>");
            sb.append("</table>");

            artifactsUnderAttack.stream()
                    .filter(a -> a.getObjectId() != 0)
                    .forEach(a -> {
                        sb.append("<table>");
                        sb.append("<tr>");
                        // sb.append("<td width=15 height=20></td>");
                        sb.append("<td width=180><button action=\"bypass -h npc_%objectId%_artifact " + a.getObjectId() + "\" value=\"").append(a.getName()).append("\" width=180 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"></td>");
                        sb.append("<td width=15 ></td>");
                        sb.append("<td width=15 ></td>");
                        sb.append("<td width=15 ></td>");
                        sb.append("<td width=70 ></td>");
                        sb.append("</tr>");
                        sb.append("</table>");
                    });
        }

        packet.replace("%artifactsUnderAttack%", sb.toString());
        sb.setLength(0);

        // Блок фортпостов
        sb.append("<table>");
        sb.append("<tr>");
        sb.append("<td width=13 height=20></td>");
        sb.append("<td width=113 ><font color=ffa500>Fortress</font></td>");
        sb.append("<td width=3 ></td>");
        sb.append("<td width=75 ><font color=a4a4a4>Owner</font></td>");
        sb.append("<td width=3 ></td>");
        sb.append("<td width=51 ><font color=a4a4a4>Reward</font></td>");
        sb.append("<td width=10 ></td>");
        sb.append("</tr>");
        sb.append("</table>");

        sb.append("<table background=\"l2ui_ct1.ComboBox_DF_Dropmenu_Bg\" >");
        ResidenceHolder.getInstance().getResidenceList(Fortress.class).forEach(fort ->
        {
            sb.append("<tr>");
            sb.append("<td width=15 height=20></td>");
            sb.append("<td width=110 ><button action=\"bypass -h npc_%objectId%_fort ").append(fort.getId()).append("\" value=\"").append(fort.getName()).append("\" width=110 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"></td>");
            sb.append("<td width=3 ></td>");
            String color = fort.getFraction() == Fraction.FIRE ? "ff2a0e" : fort.getFraction() == Fraction.WATER ? "3d59ff" : "ffffff";
            String owner = fort.getOwner() != null ? fort.getOwner().getName() : fort.getFraction() == Fraction.FIRE ? "Fire" : fort.getFraction() == Fraction.WATER ? "Water" : "NPC";
            sb.append("<td width=85 ><font color=").append(color).append(">[").append(owner).append("]</font></td>");
            sb.append("<td width=3 ></td>");
            sb.append("<td width=41 ><font color=ffa500>").append(fort.getSiegeEvent().getReward()).append(" a.</font></td>");
            sb.append("</tr>");
        });
        sb.append("</table>");
        //		<!--	Блок фортпостов конец
        packet.replace("%fortBlock%", sb.toString());
        sb.setLength(0);

        //		<!--	зоны блок 4
        //		<table bgcolor=000000 >
        //			<tr>
        //				<td width=13 height=20></td>
        //				<td width=260 ><font color=ffa500>Other Zone</font></td>
        //			</tr>
        //		</table>
        //		<table>
        //			<tr>
        //				<td width=15 height=20></td>
        //				<td width=235 ><button action="bypass -h menu_select?ask=1&reply=3000009" value="Talking island Vilage" width=225 height=20 back="l2ui_ch3.trade_slotlock" fore="l2ui_ch3.trade_slotlock"></td>
        //				<td width=25 ></td>
        //			</tr>
        //			<tr>
        //				<td width=15 height=20></td>
        //				<td width=235 ><button action="bypass -h menu_select?ask=1&reply=3000018" value="Giran Harbor" width=225 height=20 back="l2ui_ch3.trade_slotlock" fore="l2ui_ch3.trade_slotlock"></td>
        //				<td width=25 ></td>
        //			</tr>
        //		</table>
        //		<!--	зоны блок 4 -->
        if(replace.length % 2 == 0)
            for(int i = 0; i < replace.length; i += 2)
                packet.replace(String.valueOf(replace[i]), String.valueOf(replace[i + 1]));
        player.sendPacket(packet);
    }

    private void makeOutpostInfo(Player player, List<GveOutpost> outposts, StringBuilder sb)
    {
        sb.append("<table>");
        sb.append("<tr>");

        outposts.forEach(outpost ->
        {
            switch(outpost.getStatus())
            {
                case GveOutpost.ALIVE:
                {
                    //sb.append("<td width=15 height=20></td>");
                    sb.append("<td width=150><Button ALIGN=LEFT ICON=\"TELEPORT\"  color=\"red-text\" action=\"bypass -h npc_%objectId%_outpost ").append(outpost).append(" 0\" >").append("<font color=\""+outpost.getColor()+"\">"+outpost.getName(player.getLanguage())).append("</font></Button>");
                    sb.append("<td width=15 ></td>");
                    sb.append("<td width=15 ></td>");
                    sb.append("<td width=15 ></td>");
                    sb.append("<td width=70 ></td>");
                    break;
                }
                case GveOutpost.ATTACKED:
                {
                    sb.append("<td width=15 height=20></td>");
                    List<Location> locations = outpost.getLocations();
                    for(int i = 0; i < locations.size(); i++)
                    {
                        if(i == 0)
                            sb.append("<td width=150><button action=\"bypass -h npc_%objectId%_outpost ")
                                    .append(outpost).append(" 0\" value=\"").append(outpost.getName(player.getLanguage()))
                                    .append("\" width=150 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"></td>");
                        else
                            sb.append("<td width=15 ><button action=\"bypass -h npc_%objectId%_outpost ")
                                    .append(outpost).append(' ').append(i).append("\" value=\"T").append(i)
                                    .append("\" width=18 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"></td>");
                    }
                    sb.append("<td width=45 align=center><font color=ff2a0e>Attack</font></td>");
                    break;
                }
                case GveOutpost.DEAD:
                {
                    sb.append("<td width=15 height=20></td>");
                    sb.append("<td width=150><button action=\"bypass -h\" value=\"").append(outpost.getName(player.getLanguage())).append("\" width=150 height=20 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\"></td>");
                    sb.append("<td width=120 align=center>").append(sdf.format(new Date(TimeUnit.SECONDS.toMillis(outpost.getRespawnTime()) - System.currentTimeMillis()))).append("</td>");
                    break;
                }
            }
        });

        sb.append("</tr>");
        sb.append("</table>");
    }

    @Override
    public String getHtmlDir(String filename, Player player)
    {
        return "gve/teleporter/";
    }

    @Override
    public String getHtmlFilename(int val, Player player)
    {
        return "index.htm";
    }

    private boolean payForTeleport(Player player) {
        if (player.getLevel() < Config.GVE_TELEPORT_PAYED_LEVEL) {
            return true;
        }
        if (player.reduceAdena(Config.GVE_TELEPORT_PRICE, true)) {
            PaidActionsStatsService.getInstance()
                    .updateStats(PaidActionType.GATEKEEPER_TELEPORT, Config.GVE_TELEPORT_PRICE);
            return true;
        } else {
            CustomMessage message = new CustomMessage("bypass.teleport.error.item").addString("Adena");
            player.sendMessage(message);
            return false;
        }
    }
}

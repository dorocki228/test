package l2s.gameserver.handler.admincommands.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import l2s.commons.geometry.Polygon;
import l2s.commons.geometry.Polygon.PolygonBuilder;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.instancemanager.MapRegionManager;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.Territory;
import l2s.gameserver.model.World;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExShowTrace;
import l2s.gameserver.tables.GmListTable;
import l2s.gameserver.templates.mapregion.DomainArea;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminZone implements IAdminCommandHandler {
    protected final Logger _log = LoggerFactory.getLogger(AdminZone.class);
    private static final List<Location> polygons = new ArrayList<>();

    @Override
    public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar) {
        Commands command = (Commands) comm;
        if (activeChar == null || !activeChar.getPlayerAccess().CanTeleport)
            return false;
        switch (command) {
            case admin_zone_check: {
                activeChar.sendMessage("Current region: " + activeChar.getCurrentRegion());
                activeChar.sendMessage("Zone list:");
                List<Zone> zones = new ArrayList<>();
                World.getZones(zones, activeChar.getLoc(), activeChar.getReflection());
                for (Zone zone : zones)
                    activeChar.sendMessage(zone.getType() + ", name: " + zone.getName() + ", state: " + (zone.isActive() ? "active" : "not active") + ", inside: " + zone.checkIfInZone(activeChar) + "/" + zone.checkIfInZone(activeChar.getX(), activeChar.getY(), activeChar.getZ()));
                break;
            }
            case admin_region: {
                activeChar.sendMessage("Current region: " + activeChar.getCurrentRegion());
                activeChar.sendMessage("Objects list:");
                for (GameObject o : activeChar.getCurrentRegion())
                    if (o != null)
                        activeChar.sendMessage(o.toString());
                break;
            }
            case admin_vis_count: {
                activeChar.sendMessage("Current region: " + activeChar.getCurrentRegion());
                activeChar.sendMessage("Players count: " + World.getAroundPlayers(activeChar).size());
                break;
            }
            case admin_pos: {
                String pos = activeChar.getX() + ", " + activeChar.getY() + ", " + activeChar.getZ() + ", " + activeChar.getHeading() + " Geo [" + (activeChar.getX() - World.MAP_MIN_X >> 4) + ", " + (activeChar.getY() - World.MAP_MIN_Y >> 4) + "] Ref " + activeChar.getReflectionId();
                activeChar.sendMessage("Pos: " + pos);
                break;
            }
            case admin_domain: {
                DomainArea domain = MapRegionManager.getInstance().getRegionData(DomainArea.class, activeChar);
                Castle castle = domain != null ? ResidenceHolder.getInstance().getResidence(Castle.class, domain.getId()) : null;
                if (castle != null) {
                    activeChar.sendMessage("Domain: " + castle.getName());
                    break;
                }
                activeChar.sendMessage("Domain: Unknown");
                break;
            }
            case admin_loc: {
                System.out.println(activeChar.getX() + " " + activeChar.getY() + " " + activeChar.getZ() + " " + activeChar.getHeading());
                activeChar.sendMessage("Point saved.");
                ItemInstance temp = ItemFunctions.createItem(1060);
                temp.dropMe(activeChar, activeChar.getLoc());
                break;
            }
            case admin_locdump: {
                System.out.println("x=\"" + activeChar.getX() + "\" y=\"" + activeChar.getY() + "\" z=\"" + activeChar.getZ());
                activeChar.sendMessage("Point saved and dumped.");
                ItemInstance temp = ItemFunctions.createItem(1060);
                temp.dropMe(activeChar, activeChar.getLoc());
                try {
                    new File("dumps").mkdir();
                    File f = new File("dumps/locdump.txt");
                    if (!f.exists())
                        f.createNewFile();
                    FileWriter writer = new FileWriter(f, true);
                    writer.write("Loc: " + activeChar.getLoc().x + ", " + activeChar.getLoc().y + ", " + activeChar.getLoc().z + "\n");
                    writer.close();
                } catch (Exception ex) {
                }
                break;
            }
            case admin_polygon: {
                if (polygons.size() > 0) {
                    final ExShowTrace trace = new ExShowTrace();
                    trace.addLine(polygons.get(polygons.size() - 1), activeChar.getLoc(), 10, 7000000);
                    showPath(trace);
                }
                final String pos = activeChar.getX() + " " + activeChar.getY() + " " + (activeChar.getZ() - 100) + "" + (activeChar.getZ() + 100);
                activeChar.sendMessage("Cords: " + pos + " is save!");
                polygons.add(activeChar.getLoc());
                break;
            }
            case admin_polygon_clear: {
                activeChar.sendMessage("All cords is removed: " + polygons.size() + " cords!");
                polygons.clear();
                break;
            }
            case admin_polygon_save: {
                parseTerritory();

                final ExShowTrace trace = new ExShowTrace();
                Location last = null;
                Location first = null;
                for (final Location location : polygons) {
                    if (last != null) {
                        trace.addLine(last, location, 10, 7000000);
                    } else
                        first = location;
                    last = location;
                }

                trace.addLine(last, first, 10, 7000000);
                showPath(trace);

                buildZone(activeChar);
                break;
            }
            case admin_generate_seed: { // Хахахааха я был бухой когда писал это, но вроде работает xD
                if (!activeChar.isInZone(Zone.ZoneType.STEAD)) {
                    activeChar.sendMessage("You must be in STEAD zone type!");
                    return false;
                }

                int radius = 100;
                StringTokenizer st = new StringTokenizer(fullString, " ");
                st.nextToken();
                if (st.hasMoreTokens())
                    radius = Integer.parseInt(st.nextToken());

                Zone zone = activeChar.getZone(Zone.ZoneType.STEAD);
                zone.getInsideNpcs().forEach(GameObject::deleteMe);
                int count = 0;
                while (count++ < 1000) {
                    Location location = zone.getTerritory().getRandomLoc(activeChar.getGeoIndex());
                    List<NpcInstance> inside = zone.getInsideNpcs();
                    int correct = 0;
                    for (NpcInstance npc : inside) {
                        if (location.distance3D(npc.getLoc()) > radius)
                            correct++;
                    }
                    if (correct == inside.size()) {
                        SimpleSpawner spawn = new SimpleSpawner(NpcHolder.getInstance().getTemplate(40814));
                        spawn.setLoc(location);
                        spawn.setAmount(1);
                        spawn.setRespawnDelay(0);
                        spawn.setReflection(activeChar.getReflection());
                        spawn.init();
                        spawn.stopRespawn();
                        System.out.println("<seed_loc x=\"" + location.x + "\" y=\"" + location.y + "\" z=\"" + location.z + "\"/>");
                    }
                }

                activeChar.sendMessage("Generate " + zone.getInsideNpcs().size());
            }
        }
        return true;
    }


    private void parseTerritory() {
        final Territory t = new Territory();
        t.add(parsePolygon0());
    }

    private Polygon parsePolygon0() {
        final PolygonBuilder temp = new PolygonBuilder();
        polygons.forEach(loc -> temp.add(loc.x, loc.y).setZmin(loc.z - 100).setZmax(loc.z + 100));

        if (!temp.validate()) {
            _log.error("Invalid polygon{" + temp.toString() + "}.");
            GmListTable.broadcastMessageToGMs("Invalid polygon{" + temp.toString() + "}.");
        }

        return temp.createPolygon();
    }

    private void showPath(final ExShowTrace trace) {
        trace.getTraces().forEach(position ->
                drop(new Location(position._x, position._y, position._z))
        );
    }

    private void drop(final Location location) {
        final ItemInstance item = ItemFunctions.createItem(57);
        item.setCount(1);
        item.dropMe(null, location);
    }

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH-mm");

    private void buildZone(final Player player) {
        final Document document = DocumentHelper.createDocument();
        document.addDocType("list", null, "zone.dtd");
        final Date date = new Date();
        final String name = "[" + DATE_FORMAT.format(date) + " " + TIME_FORMAT.format(date) + "]";

        final Element element = document.addElement("list");
        final Element zone = element.addElement("zone");
        zone.addAttribute("name", name);
        zone.addAttribute("type", "STEAD");
        final Element set = zone.addElement("set");
        set.addAttribute("name", "enabled");
        set.addAttribute("val", "false");

        final Element territory = zone.addElement("polygon");
        polygons.forEach(location -> {
            final Element coords = territory.addElement("coords");
            coords.addAttribute("loc", String.valueOf(location.x) + " " + String.valueOf(location.y));
        });

        try {
            final String path = Config.DATAPACK_ROOT + "/data/zone/" + name + ".xml";

            final OutputFormat prettyPrint = OutputFormat.createPrettyPrint();
            prettyPrint.setIndent("\t");
            final XMLWriter xmlWriter = new XMLWriter(new FileOutputStream(path), prettyPrint);
            xmlWriter.write(document);
            xmlWriter.close();
        } catch (final Exception e1) {
            e1.printStackTrace();
        }

        player.sendMessage("Zone is save: " + polygons.size() + " cords!");
        polygons.clear();
    }

    @Override
    public Enum<?>[] getAdminCommandEnum() {
        return Commands.values();
    }

    private enum Commands {
        admin_zone_check,
        admin_region,
        admin_pos,
        admin_vis_count,
        admin_domain,
        admin_loc,
        admin_locdump,
        admin_polygon,
        admin_polygon_clear,
        admin_polygon_save,
        admin_generate_seed
    }
}

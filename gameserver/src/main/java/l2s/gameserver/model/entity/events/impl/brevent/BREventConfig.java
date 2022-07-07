package l2s.gameserver.model.entity.events.impl.brevent;

import l2s.commons.geometry.Circle;
import l2s.commons.util.Rnd;
import l2s.gameserver.model.Territory;
import l2s.gameserver.model.entity.events.impl.brevent.model.BRItemUpgradeableSet;
import l2s.gameserver.model.entity.events.impl.brevent.model.BRStage;
import l2s.gameserver.model.entity.events.impl.brevent.util.XMLUtil;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.data.CapsuledItemData;
import l2s.gameserver.utils.ReflectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * @author : Nami
 * @author Java-man
 * @date : 19.06.2018
 * @time : 20:14
 * <p/>
 */
public class BREventConfig {
    private static final Logger LOGGER = getLogger(BREventConfig.class);

    private static final BREventConfig instance = new BREventConfig();

    public static int MIN_PLAYERS;
    public static int MAX_PLAYERS;
    public static int MOB_KILL_PC_POINTS_REWARD;
    public static int PLAYER_KILL_PC_POINTS_REWARD;
    public static int INSTANT_ZONE_ID;
    public static String START_GRADE;

    public static List<Pair<Integer, Integer>> EVENT_EQUIP_ITEMS;  // список предоставляемых расходников
    public static Map<Integer, List<BRItemUpgradeableSet>> WEAPON_UPGRADE_LIST;  // списки апгрейдов снаряжения
    public static Map<Integer, List<BRItemUpgradeableSet>> ARMOR_UPGRADE_LIST;
    public static List<BRItemUpgradeableSet> JEWEL_UPGRADE_LIST;

    public static List<Territory> AREA_LIST;
    public static List<Territory> PLAYER_SPAWNS;

    public static List<BRStage> STAGE_LIST;

    private BREventConfig() {
        reload();
    }

    public static BREventConfig getInstance()
    {
        return instance;
    }

    public void reload() {
        File file = new File("config/events/BattleRoyalProperties.xml");
        DocumentBuilderFactory factory1 = DocumentBuilderFactory.newInstance();
        factory1.setValidating(false);
        factory1.setIgnoringComments(true);
        AREA_LIST = new ArrayList<>();
        PLAYER_SPAWNS = new ArrayList<>();
        EVENT_EQUIP_ITEMS = new ArrayList<>();
        WEAPON_UPGRADE_LIST = new HashMap<>();
        ARMOR_UPGRADE_LIST = new HashMap<>();
        JEWEL_UPGRADE_LIST = new ArrayList<>();
        STAGE_LIST = new ArrayList<>();
        try {
            Document doc1 = factory1.newDocumentBuilder().parse(file);
            for (Node list = doc1.getFirstChild(); list != null; list = list.getNextSibling()) {
                if (XMLUtil.isNodeName(list, "list")) {
                    for (Node n1 = list.getFirstChild(); n1 != null; n1 = n1.getNextSibling()) {
                        if (XMLUtil.isNodeName(n1, "instantZoneId")) {
                            INSTANT_ZONE_ID = XMLUtil.get(n1, "val", 70);
                        }
                        if (XMLUtil.isNodeName(n1, "minPlayers")) {
                            MIN_PLAYERS = XMLUtil.get(n1, "val", 70);
                        }
                        if (XMLUtil.isNodeName(n1, "maxPlayers")) {
                            MAX_PLAYERS = XMLUtil.get(n1, "val", 100);
                        }
                        if (XMLUtil.isNodeName(n1, "mobPcReward")) {
                            MOB_KILL_PC_POINTS_REWARD = XMLUtil.get(n1, "val", 1);
                        }
                        if (XMLUtil.isNodeName(n1, "playerPcReward")) {
                            PLAYER_KILL_PC_POINTS_REWARD = XMLUtil.get(n1, "val", 15);
                        }
                        if (XMLUtil.isNodeName(n1, "startGrade")) {
                            START_GRADE = XMLUtil.get(n1, "val");
                        }

                        if (XMLUtil.isNodeName(n1, "stageList")) {
                            for (Node stage_node = n1.getFirstChild(); stage_node != null; stage_node = stage_node.getNextSibling()) {
                                if (XMLUtil.isNodeName(stage_node, "stage")) {
                                    int stageNumber, safeTime, runTime, damageInitial, damageFinal, radius;
                                    stageNumber = XMLUtil.get(stage_node, "stageNumber", 0);
                                    safeTime = XMLUtil.get(stage_node, "safeTime", 0);
                                    runTime = XMLUtil.get(stage_node, "runTime", 0);
                                    damageInitial = XMLUtil.get(stage_node, "damageInitial", 0);
                                    damageFinal = XMLUtil.get(stage_node, "damageFinal", 0);
                                    radius = XMLUtil.get(stage_node, "radius", 0);
                                    STAGE_LIST.add(new BRStage(stageNumber, safeTime, runTime, damageInitial, damageFinal, radius));
                                }
                            }
                            STAGE_LIST.sort((o1, o2) -> o1.getStageNumber() > o2.getStageNumber() ? 1 : 0);
                        }
                        if (XMLUtil.isNodeName(n1, "areaList")) {
                            for (Node territory_node = n1.getFirstChild(); territory_node != null; territory_node = territory_node.getNextSibling()) {
                                if (XMLUtil.isNodeName(territory_node, "circleCenter")) {
                                    int x, y, z_min, z_max, radius;
                                    x = XMLUtil.get(territory_node, "x", -1);
                                    y = XMLUtil.get(territory_node, "y", -1);
                                    z_min = XMLUtil.get(territory_node, "z_min", -1);
                                    z_max = XMLUtil.get(territory_node, "z_max", -1);
                                    radius = XMLUtil.get(territory_node, "radius", -1);

                                    Circle circle = new Circle(x, y, radius);
                                    circle.setZmax(z_max);
                                    circle.setZmin(z_min);

                                    Territory territory = new Territory().add(circle);
                                    AREA_LIST.add(territory);
                                }
                                else if (XMLUtil.isNodeName(territory_node, "areaZone")) {
                                    var zoneName = XMLUtil.get(territory_node, "name");
                                    var zone = ReflectionUtils.getZone(zoneName);

                                    AREA_LIST.add(zone.getTerritory());
                                }
                            }
                        }
                        if (XMLUtil.isNodeName(n1, "playerSpawns")) {
                            for (Node spawn_node = n1.getFirstChild(); spawn_node != null; spawn_node = spawn_node.getNextSibling()) {
                                if (XMLUtil.isNodeName(spawn_node, "spawn_point")) {
                                    int x = XMLUtil.get(spawn_node, "x", 0);
                                    int y = XMLUtil.get(spawn_node, "y", 0);
                                    int z = XMLUtil.get(spawn_node, "z", 0);
                                    int radius = XMLUtil.get(spawn_node, "radius", 0);

                                    Circle circle = new Circle(x, y, radius);
                                    circle.setZmin(z - 200);
                                    circle.setZmax(z + 200);
                                    var territory = new Territory().add(circle);
                                    PLAYER_SPAWNS.add(territory);
                                }
                            }
                            LOGGER.info("Loaded items: {}", EVENT_EQUIP_ITEMS.size());
                        }
                        if (XMLUtil.isNodeName(n1, "itemList")) {
                            for (Node item_node = n1.getFirstChild(); item_node != null; item_node = item_node.getNextSibling()) {
                                if (XMLUtil.isNodeName(item_node, "item")) {
                                    int id, count;
                                    id = XMLUtil.get(item_node, "id", 0);
                                    count = XMLUtil.get(item_node, "count", 0);
                                    EVENT_EQUIP_ITEMS.add(new ImmutablePair<>(id, count));
                                }
                            }
                            LOGGER.info("Loaded items: {}", EVENT_EQUIP_ITEMS.size());
                        }
                        if (XMLUtil.isNodeName(n1, "weaponUpgradeableList")) {
                            for (Node classNode = n1.getFirstChild(); classNode != null; classNode = classNode.getNextSibling()) {
                                if (XMLUtil.isNodeName(classNode, "class")) {
                                    int id = XMLUtil.get(classNode, "id", 0);
                                    List<BRItemUpgradeableSet> weaponList = getItemSet(classNode);
                                    WEAPON_UPGRADE_LIST.put(id, weaponList);
                                }
                            }
                            LOGGER.info("Loaded weapons: {}", WEAPON_UPGRADE_LIST.size());
                        } else if (XMLUtil.isNodeName(n1, "armorUpgradeableList")) {
                            for (Node classNode = n1.getFirstChild(); classNode != null; classNode = classNode.getNextSibling()) {
                                if (XMLUtil.isNodeName(classNode, "class")) {
                                    ARMOR_UPGRADE_LIST.put(XMLUtil.get(classNode, "id", 0), getItemSet(classNode));
                                }
                            }
                            LOGGER.info("Loaded armors: {}", ARMOR_UPGRADE_LIST.size());
                        } else if (XMLUtil.isNodeName(n1, "jewelUpgradeableList")) {
                            JEWEL_UPGRADE_LIST.addAll(getItemSet(n1));
                            LOGGER.info("Loaded jewels: {}", JEWEL_UPGRADE_LIST.size());
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException pce) {
            LOGGER.error("", pce);
        }
    }

    private List<BRItemUpgradeableSet> getItemSet(Node n1) {
        List<BRItemUpgradeableSet> setList = new ArrayList<>();
        for (Node set_node = n1.getFirstChild(); set_node != null; set_node = set_node.getNextSibling()) {
            if (XMLUtil.isNodeName(set_node, "upgradeableSet")) {
                ItemGrade itemGrade = XMLUtil.get(set_node, "grade", ItemGrade.class);
                int price = XMLUtil.get(set_node, "price", 0);
                List<CapsuledItemData> itemList = new ArrayList<>();
                for (Node item_node = set_node.getFirstChild(); item_node != null; item_node = item_node.getNextSibling()) {
                    if (XMLUtil.isNodeName(item_node, "item")) {
                        int id, enchant;
                        long count;
                        id = XMLUtil.get(item_node, "id", 0);
                        count = XMLUtil.get(item_node, "count", 1);
                        enchant = XMLUtil.get(item_node, "enchant", 0);
                        itemList.add(new CapsuledItemData(id, count, count, 100, enchant));
                    }
                }
                setList.add(new BRItemUpgradeableSet(itemGrade, price, itemList));
            }
        }
        return setList;
    }

    public Territory getRandomArea()
    {
        return AREA_LIST.get(Rnd.get(AREA_LIST.size()));
    }

    public Optional<BRItemUpgradeableSet> getNextItemByGrade(ItemGrade current, int type, int classId) {
        switch(type)
        {
            case 0:
                if(WEAPON_UPGRADE_LIST.containsKey(classId))
                    return WEAPON_UPGRADE_LIST.get(classId).stream()
                            .filter(e -> e.getGrade().extOrdinal() - 1 == current.extOrdinal())
                            .findFirst();

                return Optional.empty();
            case 1:
                if(ARMOR_UPGRADE_LIST.containsKey(classId))
                    return ARMOR_UPGRADE_LIST.get(classId).stream()
                            .filter(e -> e.getGrade().extOrdinal() - 1 == current.extOrdinal())
                            .findFirst();

                return Optional.empty();
            case 2:
                return JEWEL_UPGRADE_LIST.stream()
                        .filter(e -> e.getGrade().extOrdinal() - 1 == current.extOrdinal())
                        .findFirst();
        }

        return Optional.empty();
    }

    public Optional<BRItemUpgradeableSet> getItemByGrade(ItemGrade current, int type, int classId) {
        switch (type) {
            case 0:
                if(WEAPON_UPGRADE_LIST.containsKey(classId))
                    return WEAPON_UPGRADE_LIST.get(classId).stream()
                            .filter(e -> e.getGrade().extOrdinal() == current.extOrdinal())
                            .findFirst();

                return Optional.empty();
            case 1:
                if(WEAPON_UPGRADE_LIST.containsKey(classId))
                    return ARMOR_UPGRADE_LIST.get(classId).stream()
                            .filter(e -> e.getGrade().extOrdinal() == current.extOrdinal())
                            .findFirst();

                return Optional.empty();
            case 2:
                return JEWEL_UPGRADE_LIST.stream()
                        .filter(e -> e.getGrade().extOrdinal() == current.extOrdinal())
                        .findFirst();
        }

        return Optional.empty();
    }
}

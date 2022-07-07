package l2s.gameserver.data.xml.parser;


import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ArtifactHolder;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.data.xml.holder.SpawnHolder;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.artifact.ArtifactTemplate;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.spawn.SpawnTemplate;
import l2s.gameserver.utils.Location;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class ArtifactParser extends AbstractParser<ArtifactHolder> {
    private static final ArtifactParser INSTANCE = new ArtifactParser();
    private static final Logger logger = LoggerFactory.getLogger(ArtifactParser.class);

    private ArtifactParser() {
        super(ArtifactHolder.getInstance());
    }

    public static ArtifactParser getInstance() {
        return INSTANCE;
    }

    @Override
    public File getXMLPath() {
        return new File(Config.DATAPACK_ROOT, "data/xml/artifact.xml");
    }

    @Override
    public String getDTDFileName() {
        return "artifact.dtd";
    }

    @Override
    protected void readData(Element p0) throws Exception {
        p0.elements().forEach(e -> {
            int id = Integer.parseInt(e.attributeValue("id"));
            int npcId = Integer.parseInt(e.attributeValue("npcId"));
            Location loc = Location.parseLoc(e.attributeValue("loc"));
            int protectTime = Integer.parseInt(e.attributeValue("protectTime"));
            String stringName = e.attributeValue("stringName");
            NpcTemplate template = NpcHolder.getInstance().getTemplate(npcId);
            if(template == null)
                throw new NullPointerException(String.format("Artifact id=%s npcId=%s isNull", id, npcId));
            ArtifactTemplate artifactTemplate = new ArtifactTemplate(id, template, loc, protectTime, stringName);
            e.elements().forEach(e1 -> {
                String elementName = e1.getName();
                if(elementName.equalsIgnoreCase("spawn_groups")) {
                    e1.elements().forEach(e2 -> {
                        Fraction faction = Fraction.valueOf(e2.attributeValue("type"));
                        e2.elements().forEach(e3 -> {
                            String groupName = e3.attributeValue("name");
                            List<SpawnTemplate> spawn = SpawnHolder.getInstance().getSpawn(groupName);
                            if(spawn.isEmpty())
                                logger.warn("Artifact Spawn Group {} isNull", groupName);
                            else
                                artifactTemplate.addSpawnGroup(faction, groupName);
                        });
                    });
                } else if(elementName.equalsIgnoreCase("teleport_locations")) {
                    e1.elements().forEach(e2 -> {
                        var x = Integer.parseInt(e2.attributeValue("x"));
                        var y = Integer.parseInt(e2.attributeValue("y"));
                        var z = Integer.parseInt(e2.attributeValue("z"));
                        artifactTemplate.addTeleportLocation(new Location(x, y, z));
                    });
                } else if(elementName.equalsIgnoreCase("rewardSkills")) {
                    e1.elements().forEach(e2 -> {
                        int skillId = Integer.parseInt(e2.attributeValue("id"));
                        int skillLevel = Integer.parseInt(e2.attributeValue("level"));
                        SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(skillId, skillLevel);
                        if(skillEntry == null)
                            logger.warn("Artifact skillId={} skillLevel={} isNull", skillId, skillLevel);
                        else
                            artifactTemplate.addSkill(skillEntry);
                    });
                } else if(elementName.equalsIgnoreCase("rewardItems")) {
                    e1.elements().forEach(e2 -> {
                        int itemId = Integer.parseInt(e2.attributeValue("itemId"));
                        int count = Integer.parseInt(e2.attributeValue("count"));
                        ItemData itemData = new ItemData(itemId, count);
                        artifactTemplate.addRewardItem(itemData);
                    });
                } else if(elementName.equalsIgnoreCase("params")) {
                    e1.elements().forEach(e2 -> {
                        String name = e2.attributeValue("name");
                        String value = e2.attributeValue("value");
                        artifactTemplate.addParam(name, value);
                    });
                }
            });
            getHolder().add(artifactTemplate);
        });
    }
}

package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.FactionWarSkillHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.FactionWarSkill;
import org.dom4j.Element;

import java.io.File;

public class FactionWarSkillParser extends AbstractParser<FactionWarSkillHolder> {
    private static final FactionWarSkillParser INSTANCE = new FactionWarSkillParser();

    private FactionWarSkillParser() {
        super(FactionWarSkillHolder.getInstance());
    }

    public static FactionWarSkillParser getInstance() {
        return INSTANCE;
    }

    @Override
    public File getXMLPath() {
        return new File(Config.DATAPACK_ROOT, "data/xml/faction_war_skills.xml");
    }

    @Override
    public String getDTDFileName() {
        return "faction_war_skills.dtd";
    }

    @Override
    protected void readData(Element element) throws Exception {
        element.elements().forEach(e -> {
            final int skillId = Integer.parseInt(e.attributeValue("id"));
            final int level = Integer.parseInt(e.attributeValue("level"));
            final int minTrust = Integer.parseInt(e.attributeValue("minTrust"));
            final int cost = Integer.parseInt(e.attributeValue("cost"));
            final SkillEntry skillEntry = SkillHolder.getInstance().getSkillEntry(skillId, level);
            if(skillEntry == null)
                warn(String.format("Faction War Skill id=%s level=%s is null!", skillId, level));
            else
                getHolder().add(new FactionWarSkill(skillId, level, minTrust, cost));
        });
    }
}

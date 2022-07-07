package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.FactionLeaderCommandHolder;
import l2s.gameserver.utils.Language;
import org.dom4j.Element;

import java.io.File;

public class FactionLeaderCommandParser extends AbstractParser<FactionLeaderCommandHolder> {
    private static final FactionLeaderCommandParser INSTANCE = new FactionLeaderCommandParser();

    private FactionLeaderCommandParser() {
        super(FactionLeaderCommandHolder.getInstance());
    }

    public static FactionLeaderCommandParser getInstance() {
        return INSTANCE;
    }

    @Override
    public File getXMLPath() {
        return new File(Config.DATAPACK_ROOT, "data/xml/leader_commands.xml");
    }

    @Override
    public String getDTDFileName() {
        return "leader_commands.dtd";
    }

    @Override
    protected void readData(Element e) throws Exception {
        e.elements().forEach(element -> {
            String code = element.attributeValue("code");
            element.elements().forEach(element1 -> {
                Language language = Language.getLanguage(element1.attributeValue("lang"));
                String message = element1.attributeValue("text");
                getHolder().put(code, language, message);
            });
        });
    }
}

package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.FortressUpgradeHolder;
import l2s.gameserver.model.entity.residence.fortress.FortressUpgrade;
import l2s.gameserver.model.entity.residence.fortress.UpgradeData;
import l2s.gameserver.model.entity.residence.fortress.UpgradeType;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class FortressUpgradeParser extends AbstractParser<FortressUpgradeHolder> {
    private static FortressUpgradeParser instance = new FortressUpgradeParser();
    private static final Logger logger = LoggerFactory.getLogger(FortressUpgradeParser.class);

    private FortressUpgradeParser() {
        super(FortressUpgradeHolder.getInstance());
    }

    public static FortressUpgradeParser getInstance() {
        return instance;
    }

    @Override
    public File getXMLPath() {
        return new File(Config.DATAPACK_ROOT, "data/xml/fortress_upgrade.xml");
    }

    @Override
    public String getDTDFileName() {
        return "fortress_upgrade.dtd";
    }

    @Override
    protected void readData(Element p0) throws Exception {
        p0.elements().forEach(e -> {
            final int id = Integer.parseInt(e.attributeValue("id"));
            final FortressUpgrade upgrade = new FortressUpgrade();
            e.elements().forEach(e1 -> {
                final UpgradeType type = UpgradeType.valueOf(e1.attributeValue("type"));
                e1.elements().forEach(e2 -> {
                    final int level = Integer.parseInt(e2.attributeValue("level"));
                    final String param = e2.attributeValue("param");
                    final long price = Long.parseLong(e2.attributeValue("price"));
                    final UpgradeData data = new UpgradeData(param, price);
                    upgrade.put(type, level, data);
                });
            });
            getHolder().add(id, upgrade);
        });
    }
}

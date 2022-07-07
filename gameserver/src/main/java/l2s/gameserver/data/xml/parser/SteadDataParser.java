package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.component.farm.GatheringData;
import l2s.gameserver.component.farm.GatheringTemplate;
import l2s.gameserver.data.xml.holder.SteadDataHolder;
import l2s.gameserver.model.farm.Stead;
import l2s.gameserver.model.farm.zone.SteadZone;
import l2s.gameserver.utils.Location;
import org.dom4j.Element;

import java.io.File;
import java.util.Arrays;

public class SteadDataParser extends AbstractParser<SteadDataHolder> {
    private static SteadDataParser instance = new SteadDataParser();

    private SteadDataParser() {
        super(SteadDataHolder.getInstance());
    }

    public static SteadDataParser getInstance() {
        return instance;
    }

    @Override
    public File getXMLPath() {
        return new File(Config.DATAPACK_ROOT, "data/stead_data.xml");
    }

    @Override
    public String getDTDFileName() {
        return "stead_data.dtd";
    }

    @Override
    protected void readData(Element p0) throws Exception {
        p0.elements().forEach(e -> {
            if ("configuration".equalsIgnoreCase(e.getName())) {
                final String key = e.attributeValue("key");
                final String param = e.attributeValue("param");
                getHolder().addConfiguration(key, param);
            } else if ("gathering".equalsIgnoreCase(e.getName())) {
                final int id = Integer.parseInt(e.attributeValue("id"));
                final String model = e.attributeValue("model");
                final int[] array = Arrays.stream(model.split(":"))
                        .map(String::trim).mapToInt(Integer::parseInt).toArray();
                final int maturation = Integer.parseInt(e.attributeValue("maturation"));
                final GatheringTemplate gathering = new GatheringTemplate(id, maturation);
                gathering.setModel(array);
                getHolder().addGathering(gathering);
                e.elements().forEach(e1 -> {
                    final int gathering_id = Integer.parseInt(e1.attributeValue("id"));
                    final int min = Integer.parseInt(e1.attributeValue("min"));
                    final int max = Integer.parseInt(e1.attributeValue("max"));
                    final double chance = Double.parseDouble(e1.attributeValue("chance"));
                    final GatheringData data = new GatheringData(gathering_id, min, max, chance);
                    if ("crop".equalsIgnoreCase(e1.getName()))
                        gathering.addCrop(data);
                    else
                        gathering.addSeed(data);
                });
            } else if ("stead".equalsIgnoreCase(e.getName())) {
                final int size = Integer.parseInt(e.attributeValue("size"));
                final int possession = Integer.parseInt(e.attributeValue("possession"));
                final Stead stead = new Stead(possession, size);
                getHolder().addStead(possession, stead);
                e.elements().forEach(e1 -> {
                    final String name = e1.attributeValue("name");
                    SteadZone zone = stead.addZone(name);
                    e1.elements().forEach(e2 -> {
                        final int x = Integer.parseInt(e2.attributeValue("x"));
                        final int y = Integer.parseInt(e2.attributeValue("y"));
                        final int z = Integer.parseInt(e2.attributeValue("z"));
                        final Location location = new Location(x, y, z);
                        zone.addPoint(location);
                    });
                });
            }
        });
    }
}

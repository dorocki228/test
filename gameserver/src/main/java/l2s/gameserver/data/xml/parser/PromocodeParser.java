package l2s.gameserver.data.xml.parser;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.data.xml.holder.PromocodeHolder;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.templates.promocode.PromocodeTemplate;
import org.dom4j.Element;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Mangol
 */
public class PromocodeParser extends AbstractParser<PromocodeHolder>
{
    private static final PromocodeParser ourInstance = new PromocodeParser();

    public static PromocodeParser getInstance()
    {
        return ourInstance;
    }

    public PromocodeParser()
    {
        super(PromocodeHolder.getInstance());
    }

    @Override
    public File getXMLPath()
    {
        return new File(Config.DATAPACK_ROOT, "data/promocodes/");
    }

    @Override
    public String getDTDFileName()
    {
        return "promocode.dtd";
    }

    @Override
    protected void readData(Element rootElement) throws Exception
    {
        rootElement.elements().forEach(e ->
        {
            String id = e.attributeValue("id");
            boolean enable = Boolean.parseBoolean(e.attributeValue("enabled"));
            int reuse = e.attributeValue("reuse") != null ? Integer.parseInt(e.attributeValue("reuse")) : 1;
            List<ItemData> list = e.element("rewards").elements().stream()
                    .map(r ->
                    {
                        int itemId = Integer.parseInt(r.attributeValue("id"));
                        long count = Long.parseLong(r.attributeValue("count"));
                        ItemTemplate item = ItemHolder.getInstance().getTemplate(itemId);
                        if(item == null)
                        {
                            warn(String.format("code id=%s, itemId=%s is null!", id, itemId));
                            return null;
                        }
                        return new ItemData(itemId, count);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            PromocodeTemplate template = new PromocodeTemplate(id, enable, reuse);
            template.addRewards(list);
            _holder.add(template);
        });
    }
}

package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.promocode.PromocodeTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mangol
 */
public class PromocodeHolder extends AbstractHolder
{
    private static final PromocodeHolder ourInstance = new PromocodeHolder();

    public static PromocodeHolder getInstance()
    {
        return ourInstance;
    }

    private final Map<String, PromocodeTemplate> promocodes = new HashMap<>();

    @Override
    public int size()
    {
        return promocodes.size();
    }

    @Override
    public void clear()
    {

    }

    public void add(PromocodeTemplate template)
    {
        PromocodeTemplate t = promocodes.put(template.getId(), template);
        if(t != null)
            warn(String.format("dublicate id=%s", template.getId()));
    }

    public Map<String, PromocodeTemplate> getPromocodes()
    {
        return promocodes;
    }

    public PromocodeTemplate get(String id)
    {
        return promocodes.get(id);
    }
}

package l2s.gameserver.data.xml.holder;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.commons.lang.ArrayUtils;
import l2s.gameserver.templates.item.ItemTemplate;

public final class ItemHolder extends AbstractHolder
{
    private static final ItemHolder _instance = new ItemHolder();
    private final TIntObjectMap<ItemTemplate> _items;
    private ItemTemplate[] _allTemplates;

    public static ItemHolder getInstance()
    {
        return _instance;
    }

    private ItemHolder()
    {
        _items = new TIntObjectHashMap<>();
    }

    public void addItem(ItemTemplate template)
    {
        int itemId = template.getItemId();
        if(_items.containsKey(itemId))
        {
            warn("Found duplicate item: " + itemId);
            return;
        }

        _items.put(itemId, template);
    }

    private void buildFastLookupTable()
    {
        int highestId = 0;
        for(int id : _items.keys())
            if(id > highestId)
                highestId = id;
        _allTemplates = new ItemTemplate[highestId + 1];
        TIntObjectIterator<ItemTemplate> iterator = _items.iterator();
        while(iterator.hasNext())
        {
            iterator.advance();
            _allTemplates[iterator.key()] = iterator.value();
        }
    }

    public ItemTemplate getTemplate(int id)
    {
        ItemTemplate item = ArrayUtils.valid(_allTemplates, id);
        if(item == null)
        {
            warn("Not defined item id : " + id + ", or out of range!", new Exception());
            return null;
        }
        return _allTemplates[id];
    }

    public boolean isTemplateExist(int id)
    {
        return _items.containsKey(id);
    }

    public ItemTemplate[] getAllTemplates()
    {
        return _allTemplates;
    }

    @Override
    protected void process()
    {
        buildFastLookupTable();
    }

    @Override
    public int size()
    {
        return _items.size();
    }

    @Override
    public void clear()
    {
        _items.clear();
    }
}

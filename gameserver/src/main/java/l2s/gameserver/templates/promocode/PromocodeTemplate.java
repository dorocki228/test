package l2s.gameserver.templates.promocode;

import l2s.gameserver.templates.item.data.ItemData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mangol
 */
public class PromocodeTemplate
{
    private final String id;
    private final boolean enable;
    private final int reuse;
    private final List<ItemData> list;

    public PromocodeTemplate(String id, boolean enable, int reuse)
    {
        this.id = id;
        this.enable = enable;
        this.reuse = reuse;
        this.list = new ArrayList<>();
    }

    public void addRewards(List<ItemData> list)
    {
        this.list.addAll(list);
    }

    public String getId()
    {
        return id;
    }

    public boolean isEnable()
    {
        return enable;
    }

    public int getReuse()
    {
        return reuse;
    }

    public List<ItemData> getList()
    {
        return list;
    }

    public String toString()
    {
        return "PromocodeTemplate{" +
                "id='" + id + '\'' +
                ", enable=" + enable +
                ", reuse=" + reuse +
                ", list=" + list +
                '}';
    }
}

package l2s.gameserver.model.promocode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mangol
 */
public class Promocode
{
    private final String id;
    private int uses;

    private final Map<Integer, PlayerPromocode> playerPromocodeMap = new HashMap<>();

    public Promocode(String id, int uses)
    {
        this.id = id;
        this.uses = uses;
    }

    public String getId()
    {
        return id;
    }

    public int getUses()
    {
        return uses;
    }

    public void incForUses()
    {
        uses++;
    }

    public Map<Integer, PlayerPromocode> getPlayerPromocodeMap()
    {
        return playerPromocodeMap;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        Promocode promocode = (Promocode) o;

        return id.equals(promocode.id);
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }
}

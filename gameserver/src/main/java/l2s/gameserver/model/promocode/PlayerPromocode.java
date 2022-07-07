package l2s.gameserver.model.promocode;

import l2s.gameserver.network.l2.components.hwid.HwidHolder;

/**
 * @author Mangol
 */
public class PlayerPromocode
{
    private final String id;
    private final int objId;
    private int uses;
    private HwidHolder hwidHolder;

    public PlayerPromocode(String id, int objId, int uses, HwidHolder hwidHolder)
    {
        this.id = id;
        this.objId = objId;
        this.uses = uses;
        this.hwidHolder = hwidHolder;
    }

    public HwidHolder getHwidHolder()
    {
        return hwidHolder;
    }

    public String getId()
    {
        return id;
    }

    public int getObjId()
    {
        return objId;
    }

    public int getUses()
    {
        return uses;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        PlayerPromocode that = (PlayerPromocode) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    public void incForUses()
    {
        uses++;
    }
}

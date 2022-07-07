package l2s.gameserver.logging.message;

import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.logging.ItemLogProcess;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.item.ItemTemplate;

/**
 * @author Java-man
 * @since 22.04.2018
 */
public class ItemLogMessage extends AbstractLogMessage
{
    private final Player activeChar;
    private final ItemLogProcess logType;
    private final ItemInstance item;
    private final int itemId;
    private final long count;
    private final long price;
    private final String parameter;

    public ItemLogMessage(Player activeChar, ItemLogProcess logType, ItemInstance item)
    {
        this(activeChar, logType, item, item.getItemId(), item.getCount(), 0L, 0);
    }

    public ItemLogMessage(Player activeChar, ItemLogProcess logType, ItemInstance item, long count)
    {
        this(activeChar, logType, item, item.getItemId(), count, 0L, 0);
    }

    public ItemLogMessage(Player activeChar, ItemLogProcess logType, ItemInstance item, long count, long price)
    {
        this(activeChar, logType, item, item.getItemId(), count, price, 0);
    }

    public ItemLogMessage(Player activeChar, ItemLogProcess logType, ItemInstance item, long count, long price,
                          String parameter)
    {
        this(activeChar, logType, item, item.getItemId(), count, price, parameter);
    }

    public ItemLogMessage(Player activeChar, ItemLogProcess logType, ItemInstance item, long count, long price,
                          int parameter)
    {
        this(activeChar, logType, item, item.getItemId(), count, price, parameter);
    }

    public ItemLogMessage(Player activeChar, ItemLogProcess logType, int itemId, long count)
    {
        this(activeChar, logType, null, itemId, count, 0L, 0);
    }

    public ItemLogMessage(Player activeChar, ItemLogProcess logType, int itemId, long count, long price)
    {
        this(activeChar, logType, null, itemId, count, price, 0);
    }

    public ItemLogMessage(Player activeChar, ItemLogProcess logType, int itemId, long count, long price, int parameter)
    {
        this(activeChar, logType, null, itemId, count, price, parameter);
    }

    public ItemLogMessage(Player activeChar, ItemLogProcess logType, ItemInstance item, int itemId,
                          long count, long price, int parameter)
    {
        this(activeChar, logType, item, itemId, count, price, String.valueOf(parameter));
    }

    private ItemLogMessage(Player activeChar, ItemLogProcess logType, ItemInstance item, int itemId,
                           long count, long price, String parameter)
    {
        this.activeChar = activeChar;
        this.logType = logType;
        this.item = item;
        this.itemId = itemId;
        this.count = count;
        this.price = price;
        this.parameter = parameter;
    }

    @Override
    protected void formatMessage(StringBuilder builder)
    {
        builder.append(activeChar);
        builder.append(' ');
        builder.append('(')
                .append("IP: ").append(activeChar.getIP()).append(' ')
                .append("Account: ").append(activeChar.getAccountName())
                .append(')').append(' ');
        builder.append('(')
                .append("X: ").append(activeChar.getX()).append(' ')
                .append("Y: ").append(activeChar.getY()).append(' ')
                .append("Z: ").append(activeChar.getZ())
                .append(')');
        builder.append(' ');
        builder.append(logType);
        builder.append(' ');
        if(item != null)
        {
            builder.append(item);
        }
        else
        {
            ItemTemplate it = ItemHolder.getInstance().getTemplate(itemId);
            builder.append(it.getName());
            if(!it.getAdditionalName().isEmpty())
            {
                builder.append(' ');
                builder.append('<').append(it.getAdditionalName()).append('>');
            }
        }
        builder.append(' ');
        builder.append("Count: ").append(count);
        builder.append(' ');

        // Parameter
        switch(logType)
        {
            case CraftCreate:
            case CraftDelete:
                builder.append(' ');
                builder.append("Recipe: ").append(parameter);
                break;
            case PrivateStoreBuy:
            case PrivateStoreSell:
            case RecipeShopBuy:
            case RecipeShopSell:
                builder.append(' ');
                builder.append("Price: ").append(price);
                break;
            case MultiSellIngredient:
            case MultiSellProduct:
                builder.append(' ');
                builder.append("MultiSell: ").append(parameter);
                break;
            case NpcBuy:
                builder.append(' ');
                builder.append("BuyList: ").append(parameter);
                builder.append(' ');
                builder.append("Price: ").append(price);
                break;
            case NpcCreate:
            case NpcDelete:
                builder.append(' ');
                builder.append("NPC: ").append(parameter);
                break;
            case QuestCreate:
            case QuestDelete:
                builder.append(' ');
                builder.append("Quest: ").append(parameter);
                break;
            case EventCreate:
            case EventDelete:
                builder.append(' ');
                builder.append("Event: ").append(parameter);
                break;
        }
    }

    @Override
    public Object[] getParameters()
    {
        return new Object[] {
                activeChar, logType, item, itemId, count, price, parameter
        };
    }
}

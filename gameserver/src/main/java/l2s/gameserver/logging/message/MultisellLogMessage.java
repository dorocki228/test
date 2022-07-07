package l2s.gameserver.logging.message;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.MultiSellIngredient;
import l2s.gameserver.network.l2.c2s.RequestMultiSellChoose;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Java-man
 * @since 25.04.2018
 */
public class MultisellLogMessage extends AbstractLogMessage
{
    private final Player activeChar;
    private final List<RequestMultiSellChoose.ItemData> items;
    private final List<MultiSellIngredient> ingredients;
    private final long amount;
    private final int multisell;

    public MultisellLogMessage(Player activeChar, List<RequestMultiSellChoose.ItemData> items,
                               List<MultiSellIngredient> ingredients, long amount, int multisell)
    {
        this.activeChar = activeChar;
        this.items = items;
        this.ingredients = ingredients;
        this.amount = amount;
        this.multisell = multisell;
    }

    @Override
    public Object[] getParameters()
    {
        return new Object[] {
                activeChar, items, ingredients, amount, multisell
        };
    }

    @Override
    public void formatMessage(StringBuilder builder)
    {
        builder.append(activeChar);
        builder.append(' ');
        builder.append(multisell);
        builder.append("%n");

        String itemsString = items.stream()
                .map(itemData -> itemData.getId() + "," + itemData.getCount())
                .collect(Collectors.joining("  ", "    Items:%n", "%n"));
        builder.append(itemsString);

        String ingredientsString = ingredients.stream()
                .map(ingredient -> ingredient.getItemId() + "," + ingredient.getItemCount() * amount)
                .collect(Collectors.joining("  ", "    Ingredients:%n", "%n"));
        builder.append(ingredientsString);
    }
}

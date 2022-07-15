package l2s.gameserver.network.l2.s2c;

import java.util.Collection;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.templates.item.RecipeTemplate;

public class RecipeBookItemListPacket implements IClientOutgoingPacket
{
	private Collection<RecipeTemplate> _recipes;
	private final boolean _isDwarvenCraft;
	private final int _currentMp;

	public RecipeBookItemListPacket(Player player, boolean isDwarvenCraft)
	{
		_isDwarvenCraft = isDwarvenCraft;
		_currentMp = (int) player.getCurrentMp();
		if(isDwarvenCraft)
			_recipes = player.getDwarvenRecipeBook();
		else
			_recipes = player.getCommonRecipeBook();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.RECIPE_BOOK_ITEM_LIST.writeId(packetWriter);
		packetWriter.writeD(_isDwarvenCraft ? 0x00 : 0x01);
		packetWriter.writeD(_currentMp);

		packetWriter.writeD(_recipes.size());

		for(RecipeTemplate recipe : _recipes)
		{
			packetWriter.writeD(recipe.getId());
			packetWriter.writeD(1); //??
		}

		return true;
	}
}
package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.xml.holder.RecipeHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.RecipeItemMakeInfoPacket;
import l2s.gameserver.templates.item.RecipeTemplate;

public class RequestRecipeItemMakeInfo implements IClientIncomingPacket
{
	private int _id;

	/**
	 * packet type id 0xB7
	 * format:		cd
	 */
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_id = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		RecipeTemplate recipe = RecipeHolder.getInstance().getRecipeByRecipeId(_id);
		if(recipe == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		client.sendPacket(RecipeItemMakeInfoPacket.Companion.info(activeChar, recipe, false));
	}
}
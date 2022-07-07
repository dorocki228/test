package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.templates.item.RecipeTemplate;

public class RecipeItemMakeInfoPacket extends L2GameServerPacket
{
	private final int _id;
	private final boolean _isCommon;
	private final int _status;
	private final int _curMP;
	private final int _maxMP;

	public RecipeItemMakeInfoPacket(Player player, RecipeTemplate recipe, int status)
	{
		_id = recipe.getId();
		_isCommon = recipe.isCommon();
		_status = status;
		_curMP = (int) player.getCurrentMp();
		_maxMP = player.getMaxMp();
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_id);
        writeD(_isCommon ? 1 : 0);
        writeD(_curMP);
        writeD(_maxMP);
        writeD(_status);
	}
}

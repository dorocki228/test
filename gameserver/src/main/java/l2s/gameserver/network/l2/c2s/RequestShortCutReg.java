package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.ShortCut;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ShortCutRegisterPacket;

public class RequestShortCutReg implements IClientIncomingPacket
{
	private ShortCut.ShortCutType _type;
	private int _id, _slot, _page, _lvl, _characterType;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		try {
			_type = ShortCut.ShortCutType.VALUES[packet.readD()];
		} catch (Exception e) {
			return false;
		}

		int slot = packet.readD();
		final int unk1 = packet.readC();
		_id = packet.readD();
		_lvl = packet.readD();
		_characterType = packet.readD();

		final int unk2 = packet.readD();// UNK
		final int unk3 = packet.readD(); // UNK

		_slot = slot % 12;
		_page = slot / 12;
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(_page < 0 || _page > ShortCut.PAGE_MAX)
		{
			activeChar.sendActionFailed();
			return;
		}

		if (_page == ShortCut.PAGE_AUTO_USABLE_ITEMS) {
			ItemInstance item = activeChar.getInventory().getItemByObjectId(_id);
			if (item == null) {
				activeChar.sendActionFailed();
				return;
			}

			if (!item.getTemplate().isAutousable()) {
				activeChar.sendActionFailed();
				return;
			}
		}

		if (_page == ShortCut.PAGE_AUTO_USABLE_MACRO) {
			if (_type != ShortCut.ShortCutType.MACRO) {
				activeChar.sendPacket(SystemMsg.ONLY_MACROS_CAN_BE_REGISTERED);
				return;
			}

			ShortCut shortCut = activeChar.getShortCut(0, ShortCut.PAGE_AUTO_USABLE_MACRO);
			if (shortCut != null && shortCut.isAutoUseEnabled()) {
				activeChar.getAI().stopAutoplay();
			}
		}

		ShortCut shortCut = new ShortCut(activeChar, _slot, _page, _type, _id, _lvl, _characterType);
		activeChar.sendPacket(new ShortCutRegisterPacket(activeChar, shortCut));
		activeChar.registerShortCut(shortCut);
	}
}
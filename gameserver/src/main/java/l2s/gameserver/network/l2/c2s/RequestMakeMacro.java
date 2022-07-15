package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Macro;
import l2s.gameserver.model.actor.instances.player.Macro.L2MacroCmd;
import l2s.gameserver.model.actor.instances.player.ShortCut;
import l2s.gameserver.network.l2.components.SystemMsg;

/**
 * packet type id 0xcd
 *
 * sample
 *
 * cd
 * d // id
 * S // macro name
 * S // unknown  desc
 * S // unknown  acronym
 * c // icon
 * c // count
 *
 * c // entry
 * c // type
 * d // skill id
 * c // shortcut id
 * S // command name
 *
 * format:		cdSSScc (ccdcS)
 */
public class RequestMakeMacro implements IClientIncomingPacket
{
	private Macro _macro;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		int _id = packet.readD();
		String _name = packet.readS(32);
		String _desc = packet.readS(64);
		String _acronym = packet.readS(4);
		int _icon = packet.readD();
		int _count = packet.readC();
		if(_count > 12)
			_count = 12;
		L2MacroCmd[] commands = new L2MacroCmd[_count];
		for(int i = 0; i < _count; i++)
		{
			int entry = packet.readC();
			int type = packet.readC(); // 1 = skill, 3 = action, 4 = shortcut
			int d1 = packet.readD(); // skill or page number for shortcuts
			int d2 = packet.readC();
			String command = packet.readS().replace(";", "").replace(",", "");
			commands[i] = new L2MacroCmd(entry, type, d1, d2, command);
		}
		_macro = new Macro(_id, _icon, _name, _desc, _acronym, commands);
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.getMacroses().getAllMacroses().length > 48)
		{
			activeChar.sendPacket(SystemMsg.YOU_MAY_CREATE_UP_TO_48_MACROS);
			return;
		}

		if(_macro.getName().length() == 0)
		{
			activeChar.sendPacket(SystemMsg.ENTER_THE_NAME_OF_THE_MACRO);
			return;
		}

		if(_macro.getDescr().length() > 32)
		{
			activeChar.sendPacket(SystemMsg.MACRO_DESCRIPTIONS_MAY_CONTAIN_UP_TO_32_CHARACTERS);
			return;
		}

		ShortCut shortCut = activeChar.getShortCut(0, ShortCut.PAGE_AUTO_USABLE_MACRO);
		if (shortCut != null && shortCut.isAutoUseEnabled() && shortCut.getId() == _macro.getId()) {
			activeChar.getAI().stopAutoplay();
		}

		activeChar.registerMacro(_macro);
	}
}
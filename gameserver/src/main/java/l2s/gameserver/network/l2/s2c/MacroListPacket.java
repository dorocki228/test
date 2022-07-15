package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.actor.instances.player.Macro;
import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * packet type id 0xe7
 *
 * sample
 *
 * e7
 * d // unknown change of Macro edit,add,delete
 * c // unknown
 * c //count of Macros
 * c // unknown
 *
 * d // id
 * S // macro name
 * S // desc
 * S // acronym
 * c // icon
 * c // count
 *
 * c // entry
 * c // type
 * d // skill id
 * c // shortcut id
 * S // command name
 *
 * format:		cdccdSSScc (ccdcS)
 */
public class MacroListPacket implements IClientOutgoingPacket
{
	public static enum Action
	{
		DELETE,
		ADD,
		UPDATE
	}

	private final int _macroId, _count;
	private final Action _action;
	private final Macro _macro;

	public MacroListPacket(int macroId, Action action, int count, Macro macro)
	{
		_macroId = macroId;
		_action = action;
		_count = count;
		_macro = macro;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.MACRO_LIST.writeId(packetWriter);
		packetWriter.writeC(_action.ordinal()); //unknown
		packetWriter.writeD(_macroId); //Macro ID
		packetWriter.writeC(_count); //count of Macros

		if(_macro != null)
		{
			packetWriter.writeC(1); //checked
			packetWriter.writeD(_macro.getId()); //Macro ID
			packetWriter.writeS(_macro.getName()); //Macro Name
			packetWriter.writeS(_macro.getDescr()); //Desc
			packetWriter.writeS(_macro.getAcronym()); //acronym
			packetWriter.writeD(_macro.getIcon()); //icon

			packetWriter.writeC(_macro.getCommands().length); //count

			for(int i = 0; i < _macro.getCommands().length; i++)
			{
				Macro.L2MacroCmd cmd = _macro.getCommands()[i];
				packetWriter.writeC(i + 1); //i of count
				packetWriter.writeC(cmd.getType()); //type  1 = skill, 3 = action, 4 = shortcut
				packetWriter.writeD(cmd.getParam1()); // skill id
				packetWriter.writeC(cmd.getParam2()); // shortcut id
				packetWriter.writeS(cmd.getCmd()); // command name
			}
		}
		else
			packetWriter.writeC(0); //checked

		return true;
	}
}
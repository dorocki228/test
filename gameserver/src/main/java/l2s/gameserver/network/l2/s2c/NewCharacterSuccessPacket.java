package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.data.xml.holder.PlayerTemplateHolder;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Sex;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.templates.player.PlayerTemplate;

public class NewCharacterSuccessPacket implements IClientOutgoingPacket
{
	private List<ClassId> _chars = new ArrayList<ClassId>();

	public NewCharacterSuccessPacket()
	{
		for(ClassId classId : ClassId.values())
		{
			if(classId.isOfLevel(ClassLevel.NONE))
				_chars.add(classId);
		}
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.NEW_CHARACTER_SUCCESS.writeId(packetWriter);
		packetWriter.writeD(_chars.size());

		for(ClassId temp : _chars)
		{
			/*На оффе статты атрибутов у М и Ж одинаковы.*/
			PlayerTemplate template = PlayerTemplateHolder.getInstance().getPlayerTemplate(temp.getRace(), temp, Sex.MALE);
			packetWriter.writeD(temp.getRace().ordinal());
			packetWriter.writeD(temp.getId());
			packetWriter.writeD(0x46);
			packetWriter.writeD(template.getBaseSTR());
			packetWriter.writeD(0x0a);
			packetWriter.writeD(0x46);
			packetWriter.writeD(template.getBaseDEX());
			packetWriter.writeD(0x0a);
			packetWriter.writeD(0x46);
			packetWriter.writeD(template.getBaseCON());
			packetWriter.writeD(0x0a);
			packetWriter.writeD(0x46);
			packetWriter.writeD(template.getBaseINT());
			packetWriter.writeD(0x0a);
			packetWriter.writeD(0x46);
			packetWriter.writeD(template.getBaseWIT());
			packetWriter.writeD(0x0a);
			packetWriter.writeD(0x46);
			packetWriter.writeD(template.getBaseMEN());
			packetWriter.writeD(0x0a);
		}

		return true;
	}
}
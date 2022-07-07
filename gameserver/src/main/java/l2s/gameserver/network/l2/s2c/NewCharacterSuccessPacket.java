package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.data.xml.holder.PlayerTemplateHolder;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Sex;
import l2s.gameserver.templates.player.PlayerTemplate;

import java.util.ArrayList;
import java.util.List;

public class NewCharacterSuccessPacket extends L2GameServerPacket
{
	private final List<ClassId> _chars;

	public NewCharacterSuccessPacket()
	{
		_chars = new ArrayList<>();
		for(ClassId classId : ClassId.VALUES)
			if(classId.isOfLevel(ClassLevel.NONE))
				_chars.add(classId);
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_chars.size());
		for(ClassId temp : _chars)
		{
			PlayerTemplate template = PlayerTemplateHolder.getInstance().getPlayerTemplate(temp.getRace(), temp, Sex.MALE);
            writeD(temp.getRace().ordinal());
            writeD(temp.getId());
            writeD(70);
            writeD(template.getBaseSTR());
            writeD(10);
            writeD(70);
            writeD(template.getBaseDEX());
            writeD(10);
            writeD(70);
            writeD(template.getBaseCON());
            writeD(10);
            writeD(70);
            writeD(template.getBaseINT());
            writeD(10);
            writeD(70);
            writeD(template.getBaseWIT());
            writeD(10);
            writeD(70);
            writeD(template.getBaseMEN());
            writeD(10);
		}
	}
}

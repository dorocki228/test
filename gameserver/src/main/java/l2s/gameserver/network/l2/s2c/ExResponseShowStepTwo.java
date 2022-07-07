package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.petition.PetitionMainGroup;
import l2s.gameserver.model.petition.PetitionSubGroup;
import l2s.gameserver.utils.Language;

import java.util.Collection;

public class ExResponseShowStepTwo extends L2GameServerPacket
{
	private final Language _language;
	private final PetitionMainGroup _petitionMainGroup;

	public ExResponseShowStepTwo(Player player, PetitionMainGroup gr)
	{
		_language = player.getLanguage();
		_petitionMainGroup = gr;
	}

	@Override
	protected void writeImpl()
	{
		Collection<PetitionSubGroup> subGroups = _petitionMainGroup.getSubGroups();
        writeD(subGroups.size());
		writeS(_petitionMainGroup.getDescription(_language));
		for(PetitionSubGroup g : subGroups)
		{
            writeC(g.getId());
			writeS(g.getName(_language));
		}
	}
}

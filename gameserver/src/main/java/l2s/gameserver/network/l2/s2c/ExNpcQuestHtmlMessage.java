package l2s.gameserver.network.l2.s2c;

public class ExNpcQuestHtmlMessage extends L2GameServerPacket
{
	private final int _npcObjId;
	private final CharSequence _html;
	private final int _questId;

	public ExNpcQuestHtmlMessage(int npcObjId, CharSequence html, int questId)
	{
		_npcObjId = npcObjId;
		_html = html;
		_questId = questId;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_npcObjId);
		writeS(_html);
        writeD(_questId);
	}
}

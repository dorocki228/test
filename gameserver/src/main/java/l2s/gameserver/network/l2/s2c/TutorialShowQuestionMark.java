package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class TutorialShowQuestionMark implements IClientOutgoingPacket
{
	/**
	 * После клика по знаку вопроса клиент попросит html-ку с этим номером.
	 */
	private boolean _quest;
	private int _tutorialId;

	public TutorialShowQuestionMark(boolean quest, int tutorialId)
	{
		_quest = quest;
		_tutorialId = tutorialId;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.TUTORIAL_SHOW_QUESTION_MARK.writeId(packetWriter);
		packetWriter.writeC(_quest);
		packetWriter.writeD(_tutorialId);

		return true;
	}
}
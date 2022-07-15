package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExSendCostumeList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class RequestExCostumeEvolution implements IClientIncomingPacket {
	private int unk1, costumeId, unk3, unk4;
	private List<int[]> costumesConsume;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet) {
		unk1 = packet.readD();
		costumeId = packet.readD();
		unk3 = packet.readD();
		unk4 = packet.readD();
		int count = packet.readD();
		costumesConsume = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			int[] data = new int[3];
			data[0] = packet.readD();
			data[1] = packet.readD();
			data[2] = packet.readD();
			costumesConsume.add(data);
		}
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client) {
		Player activeChar = client.getActiveChar();
		if (activeChar == null)
			return;

		if (Config.EX_COSTUME_DISABLE) {
			activeChar.sendActionFailed();
			return;
		}

		if (activeChar.isGM())
			activeChar.sendMessage(getClass().getSimpleName() + ": unk1=" + unk1 + ", costumeId=" + costumeId + ", unk3=" + unk3 + ", unk4=" + unk4 + ", costumesConsume.size()=" + costumesConsume.size());

		if (activeChar.getPrivateStoreType() != Player.STORE_PRIVATE_NONE) {
			activeChar.sendPacket(SystemMsg.TRANSFORMATION_EVOLUTION_AND_EXTRACTION_ARE_NOT_AVAILABLE_DURING_PRIVATE_STORE_OR_WORKSHOP_USE);
			return;
		}

		if (activeChar.isMovementDisabled()) {
			activeChar.sendPacket(SystemMsg.TRANSFORMATION_EVOLUTION_AND_EXTRACTION_ARE_NOT_AVAILABLE_IN_THE_FREEZE_STATE);
			return;
		}

		if (activeChar.isDead()) {
			activeChar.sendPacket(SystemMsg.DEAD_CHARACTER_CANNOT_USE_TRANSFORMATION_EVOLUTION_AND_EXTRACTION);
			return;
		}

		if (activeChar.isInTrade()) {
			activeChar.sendPacket(SystemMsg.TRANSFORMATION_EVOLUTION_AND_EXTRACTION_ARE_NOT_AVAILABLE_DURING_EXCHANGE);
			return;
		}

		if (activeChar.isParalyzed()) {
			activeChar.sendPacket(SystemMsg.TRANSFORMATION_EVOLUTION_AND_EXTRACTION_ARE_NOT_AVAILABLE_IN_THE_PETRIFICATION_STATE);
			return;
		}

		if (activeChar.isFishing()) {
			activeChar.sendPacket(SystemMsg.TRANSFORMATION_EVOLUTION_AND_EXTRACTION_ARE_NOT_AVAILABLE_DURING_FISHING);
			return;
		}

		if (activeChar.isSitting()) {
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_USE_TRANSFORMATION_EVOLUTION_AND__EXTRACTION_WHILE_SITTING);
			return;
		}

		if (activeChar.isInCombat()) {
			activeChar.sendPacket(SystemMsg.TRANSFORMATION_EVOLUTION_AND_EXTRACTION_ARE_NOT_AVAILABLE_IN_A_FIGHT);
			return;
		}

		if (!activeChar.getCostumeList().evolutionCostume(costumeId, costumesConsume)) {
			activeChar.sendActionFailed();
			return;
		}

		activeChar.sendPacket(new ExSendCostumeList(activeChar));
	}
}

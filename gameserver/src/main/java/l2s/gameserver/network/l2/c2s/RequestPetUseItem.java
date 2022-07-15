package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.ItemInstance;

public class RequestPetUseItem implements IClientIncomingPacket
{
	private int _objectId;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_objectId = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
			return;

		if(activeChar.isInTrainingCamp())
			return;

		PetInstance pet = activeChar.getPet();
		if(pet == null)
			return;

		final ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);
		if(item == null)
			return;

		pet.useItem(item, false, true);
	}
}
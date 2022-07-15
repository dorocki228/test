package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.features.huntingzones.HuntingZone;
import l2s.gameserver.features.huntingzones.HuntingZoneLocation;
import l2s.gameserver.features.huntingzones.HuntingZonesService;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.TeleportUtils;

public class RequestExRequestTeleport implements IClientIncomingPacket
{
	private int teleportId;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		teleportId = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		HuntingZone zone = HuntingZonesService.INSTANCE.getZone(teleportId);
		if (zone == null) {
			activeChar.sendActionFailed();
			return;
		}

		if (!zone.getEnabled()) {
			activeChar.sendMessage("Can't use this teleport.");
			return;
		}

		if (!TeleportUtils.checkTeleportCond(activeChar)) {
			activeChar.sendMessage("Can't use right now.");
			return;
		}

		if (activeChar.getLevel() >= Config.TELEPORT_FREE_UNTIL_LEVEL) {
			if (!ItemFunctions.deleteItem(activeChar, ItemTemplate.ITEM_ID_ADENA, zone.getPrice())) {
				activeChar.sendMessage(activeChar.isLangRus()
						? "У вас не хватает нужных вещей для выполнение опрации."
						: "You have not enough item to proceed the operation.");
				return;
			}
		}

		HuntingZoneLocation location = zone.getLocation();
		activeChar.teleToLocation(location.getX(), location.getY(), location.getZ());
	}

}
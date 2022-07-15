package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ValidateLocationPacket;
import l2s.gameserver.utils.PositionUtils;

public class ValidatePosition implements IClientIncomingPacket
{
	private static final int MAX_VALID_DIST = 1024;

	/* Fields for storing read data */
	private int _clientX;
	private int _clientY;
	private int _clientZ;
	private int _clientHeading;
	@SuppressWarnings("unused")
	private int _vehicle;
	@SuppressWarnings("unused")
	private boolean _stopMove;

	/**
	 * packet type id 0x48
	 * format:		cddddd
	 */
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_clientX = packet.readD(); // Current client X
		_clientY = packet.readD(); // Current client Y
		_clientZ = packet.readD(); // Current client Z
		_clientHeading = packet.readD(); // Heading
		_vehicle = packet.readD(); // Vehicle OID
		_stopMove = packet.readC() == 1;
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		validatePosition(activeChar, _clientX, _clientY, _clientZ, _clientHeading);
	}

	public static boolean validatePosition(Player player, int clientX, int clientY, int clientZ, int clientHeading)
	{
		if(player.isTeleporting() || player.isInObserverMode() || player.isFalling(clientZ) || player.getMovement().isKeyboardMoving())
			return false;

		// We trust client heading.
		if(clientHeading >= 0)
		{
			player.setHeading(clientHeading);
		}

		// We trust client Z if there is no geodata (not retail-like but we want server to work without geodata too).
		int playerX = player.getX();
		int playerY = player.getY();
		if(!GeoEngine.hasGeo(playerX, playerY, player.getGeoIndex()))
		{
			player.setXYZ(playerX, playerY, clientZ, true);
		}

		// We don't trust the client coordinates so we move client to server coordinates if difference is too big.
		int playerZ = player.getZ();
		if(!PositionUtils.checkIfInRange(MAX_VALID_DIST, playerX, playerY, playerZ, clientX, clientY, clientZ, true))
		{
			if(player.isInBoat())
				player.sendPacket(player.getBoat().validateLocationPacket(player));
			else
				player.sendPacket(new ValidateLocationPacket(player));
			return true;
		}
		return false;
	}
}
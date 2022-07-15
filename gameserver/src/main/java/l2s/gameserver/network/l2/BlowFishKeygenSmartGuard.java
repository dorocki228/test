package l2s.gameserver.network.l2;

import smartguard.spi.SmartGuardSPI;

/**
 * Blowfish keygen for GameServer client connections
 */
public class BlowFishKeygenSmartGuard
{
	public static byte[] getRandomKey()
	{
		return SmartGuardSPI.getSmartGuardService().getCryptManager().getRandomBlowFishKey();
	}
}

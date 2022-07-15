package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.network.l2.OutgoingPackets;
import org.strixplatform.StrixPlatform;
import org.strixplatform.utils.StrixClientData;

public class VersionCheckPacket implements IClientOutgoingPacket
{
	private byte[] _key;

	//TODO[K] - Guard section start
	private final StrixClientData clientData;
	// TODO[K] - Strix section end

	public VersionCheckPacket(byte[] key)
	{
		_key = key;
		//TODO[K] - Guard section start
		this.clientData = null;
		// TODO[K] - Strix section end
	}

	//TODO[K] - Guard section start
	public VersionCheckPacket(final byte[] key, final StrixClientData clientData)
	{
		this._key = key;
		this.clientData = clientData;
	}
	// TODO[K] - Strix section end

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.VERSION_CHECK.writeId(packetWriter);
		if(_key == null || _key.length == 0)
		{
			//TODO[K] - Guard section start
			if(StrixPlatform.getInstance().isBackNotificationEnabled() && clientData != null)
			{
				packetWriter.writeC(clientData.getServerResponse().ordinal() + 1);
			}
			// TODO[K] - Strix section end
			else
			{
				packetWriter.writeC(0x00);
			}
			return true;
		}
		packetWriter.writeC(0x01);
		for(int i = 0; i < 8; i++)
			packetWriter.writeC(_key[i]);
		packetWriter.writeD(0x01);
		packetWriter.writeD(Config.REQUEST_ID);	// Server ID
		packetWriter.writeC(0x01);
		packetWriter.writeD(0x00); // Seed (obfuscation key)
		packetWriter.writeC(0x01);	// Classic
		packetWriter.writeC(0x00);	// Arena
		if(!StrixPlatform.getInstance().isPlatformEnabled())
			packetWriter.writeC(0x00); // Unk

		return true;
	}
}
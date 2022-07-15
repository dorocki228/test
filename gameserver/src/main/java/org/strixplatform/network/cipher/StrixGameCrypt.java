package org.strixplatform.network.cipher;

import io.netty.buffer.ByteBuf;
import l2s.commons.network.ICrypt;
import org.strixplatform.logging.Log;

/**
 * @author ALF
 */
public class StrixGameCrypt implements ICrypt
{
	private final byte[] inKey = new byte[16], outKey = new byte[16];
	private boolean isEnabled = false;

	private final GuardCipher cryptIn = new GuardCipher();
	private final GuardCipher cryptOut = new GuardCipher();

	public StrixGameCrypt()
	{}

	public void setKey(final byte[] key)
	{
		System.arraycopy(key, 0, inKey, 0, 16);
		System.arraycopy(key, 0, outKey, 0, 16);

		cryptIn.setKey(key);
		cryptOut.setKey(key);
	}

	@Override
	public void encrypt(ByteBuf buf) {
		if(!isEnabled)
		{
			isEnabled = true;
			return;
		}

		if(!cryptOut.keySeted)
		{
			Log.audit("Key not setted. Nulled send packet. Maybe used network hook.");
			for(int i = 0; i < buf.writerIndex(); i++)
			{
				buf.setByte(i, (byte) 0x00);
			}
			return;
		}

        cryptOut.chiper(buf);
	}

	@Override
	public void decrypt(ByteBuf buf) {
		if(!isEnabled)
		{
			return;
		}

		if(!cryptIn.keySeted)
		{
			Log.audit("Key not setted. Nulled received packet. Maybe used network hook.");
			for(int i = 0; i < buf.writerIndex(); i++)
			{
				buf.setByte(i, (byte) 0x00);
			}
			return;
		}

        cryptIn.chiper(buf);
	}
}
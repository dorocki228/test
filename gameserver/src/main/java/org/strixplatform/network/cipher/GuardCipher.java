package org.strixplatform.network.cipher;

import io.netty.buffer.ByteBuf;
import org.strixplatform.configs.MainConfig;

import java.nio.ByteBuffer;

/**
 * @author ALF
 */
public class GuardCipher
{
	private byte[] state = new byte[256];
	private int x;
	private int y;
	public boolean keySeted = false;

	public void setKey(final byte[] key) throws NullPointerException
	{
		for(int i = 0; i < 256; i++)
		{
			state[i] = (byte) i;
		}

		x = 0;
		y = 0;

		int index1 = 0;
		int index2 = 0;

		byte tmp;

		if(key == null || key.length == 0)
		{
			throw new NullPointerException();
		}

		final byte[] keyInit = key.clone();
		if(MainConfig.STX_PF_XOR_KEY != null && MainConfig.STX_PF_XOR_KEY.length() > 0)
		{
			final byte[] xorKey = MainConfig.STX_PF_XOR_KEY.getBytes();
			for(int i = 0; i < 8; i++)
			{
				keyInit[i] ^= xorKey[i % 4];
			}
		}
		for(int i = 0; i < 256; i++)
		{
			index2 = (keyInit[index1] & 0xff) + (state[i] & 0xff) + index2 & 0xff;

			tmp = state[i];
			state[i] = state[index2];
			state[index2] = tmp;

			index1 = (index1 + 1) % keyInit.length;
		}
		keySeted = true;
	}

	public void chiper(final byte[] buf, final int offset, final int size)
	{
		int xorIndex;
		byte tmp;

		for(int i = 0; i < size; i++)
		{
			x = x + 1 & 0xff;
			y = (state[x] & 0xff) + y & 0xff;

			tmp = state[x];
			state[x] = state[y];
			state[y] = tmp;

			xorIndex = (state[x] & 0xff) + (state[y] & 0xff) & 0xff;
			buf[offset + i] ^= state[xorIndex];
		}
	}

	public void chiper(final ByteBuffer buffer, final int offset, final int size)
	{
		int xorIndex;
		byte tmp;

		for(int i = 0; i < size; i++)
		{
			x = x + 1 & 0xff;
			y = (state[x] & 0xff) + y & 0xff;

			tmp = state[x];
			state[x] = state[y];
			state[y] = tmp;

			xorIndex = (state[x] & 0xff) + (state[y] & 0xff) & 0xff;
			tmp = (byte) (buffer.get(offset + i) ^ state[xorIndex]);
			buffer.put(offset + i, tmp);
		}
	}

	public void chiper(final ByteBuf buffer)
	{
		int xorIndex;
		byte tmp;

		final int writerIndex = buffer.readableBytes();
		for(int i = 0; i < writerIndex; i++) {
			x = x + 1 & 0xff;
			y = (state[x] & 0xff) + y & 0xff;

			tmp = state[x];
			state[x] = state[y];
			state[y] = tmp;

			xorIndex = (state[x] & 0xff) + (state[y] & 0xff) & 0xff;
			tmp = (byte) (buffer.getByte(i) ^ state[xorIndex]);
			buffer.setByte(i, tmp);
		}
	}
}
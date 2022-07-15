package l2s.gameserver.network.l2;

import io.netty.buffer.ByteBuf;
import l2s.commons.network.ICrypt;
import smartguard.api.entity.IKeyObject;
import smartguard.api.integration.SmartCrypt;

public class GameCryptSmartGuard extends SmartCrypt implements ICrypt
{
	@Override
	public void setKey(byte[] key)
	{
		super.setKey(key);
	}

	@Override
	public void encrypt(ByteBuf buf)
	{
		if(!isEnabled)
		{
			isEnabled = true;
			return;
		}

		crypt(buf, this.nOutKo);
	}

	@Override
	public void decrypt(ByteBuf buf)
	{
		if(!isEnabled) {
			return;
		}

		crypt(buf, this.nInKo);
	}

	private void crypt(ByteBuf buf, IKeyObject ko) {
		int x = ko.getX();
		int y = ko.getY();

		while (buf.isReadable()) {
			x = (x + 1) % 256;
			y = ((ko.getState()[x] & 255) + y) % 256;
			byte tmp = ko.getState()[x];
			ko.getState()[x] = ko.getState()[y];
			ko.getState()[y] = tmp;
			int idx = ((ko.getState()[x] & 255) + (ko.getState()[y] & 255)) % 256;
			final byte value = (byte) (buf.readByte() ^ ko.getState()[idx]);
			buf.setByte(buf.readerIndex() - 1, value);
		}

		ko.setX(x);
		ko.setY(y);
	}
}
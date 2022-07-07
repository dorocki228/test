package l2s.gameserver.network.l2.s2c;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class AdminForgePacket extends L2GameServerPacket
{
	private final List<Part> _parts;

	public AdminForgePacket()
	{
		_parts = new ArrayList<>();
	}

	@Override
	protected boolean writeOpcodes()
	{
		return true;
	}

	@Override
	protected void writeImpl()
	{
		for(Part p : _parts)
			generate(p.b, p.str);
	}

	public boolean generate(byte b, String string)
	{
		if(b == 67 || b == 99)
		{
            writeC(Integer.decode(string));
			return true;
		}
		if(b == 68 || b == 100)
		{
            writeD(Integer.decode(string));
			return true;
		}
		if(b == 72 || b == 104)
		{
            writeH(Integer.decode(string));
			return true;
		}
		if(b == 70 || b == 102)
		{
			writeF(Double.parseDouble(string));
			return true;
		}
		if(b == 83 || b == 115)
		{
			writeS(string);
			return true;
		}
		if(b == 66 || b == 98 || b == 88 || b == 120)
		{
			writeB(new BigInteger(string).toByteArray());
			return true;
		}
		if(b == 81 || b == 113)
		{
			writeQ(Long.decode(string));
			return true;
		}
		return false;
	}

	public void addPart(byte b, String string)
	{
		_parts.add(new Part(b, string));
	}

	private static class Part
	{
		public byte b;
		public String str;

		public Part(byte bb, String string)
		{
			b = bb;
			str = string;
		}
	}
}

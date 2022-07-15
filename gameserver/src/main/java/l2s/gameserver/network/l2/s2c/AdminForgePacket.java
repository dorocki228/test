package l2s.gameserver.network.l2.s2c;

import l2s.commons.network.PacketWriter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is made to create packets with any format
 * @author Maktakien
 */
public class AdminForgePacket implements IClientOutgoingPacket
{
	private List<Part> _parts = new ArrayList<Part>();

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

	public AdminForgePacket()
	{
		//
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		for(Part p : _parts)
		{
			generate(packetWriter, p.b, p.str);
		}
		return true;
	}

	public boolean generate(PacketWriter packet, byte b, String string)
	{
		if((b == 'C') || (b == 'c'))
		{
			packet.writeC(Integer.decode(string));
			return true;
		}
		else if((b == 'D') || (b == 'd'))
		{
			packet.writeD(Integer.decode(string));
			return true;
		}
		else if((b == 'H') || (b == 'h'))
		{
			packet.writeH(Integer.decode(string));
			return true;
		}
		else if((b == 'F') || (b == 'f'))
		{
			packet.writeF(Double.parseDouble(string));
			return true;
		}
		else if((b == 'S') || (b == 's'))
		{
			packet.writeS(string);
			return true;
		}
		else if((b == 'B') || (b == 'b') || (b == 'X') || (b == 'x'))
		{
			packet.writeB(new BigInteger(string).toByteArray());
			return true;
		}
		else if((b == 'Q') || (b == 'q'))
		{
			packet.writeQ(Long.decode(string));
			return true;
		}
		return false;
	}

	public void addPart(byte b, String string)
	{
		_parts.add(new Part(b, string));
	}

}
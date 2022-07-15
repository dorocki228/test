/*
 * Copyright (C) 2004-2014 L2J Unity
 * 
 * This file is part of L2J Unity.
 * 
 * L2J Unity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Unity is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2s.commons.network.codecs;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import l2s.commons.network.IOutgoingPacket;
import l2s.commons.network.NetworkLogger;
import l2s.commons.network.PacketWriter;

/**
 * @author Nos
 */
@Sharable
public class PacketEncoder extends MessageToByteEncoder<IOutgoingPacket>
{
	private final int _maxPacketSize;
	
	public PacketEncoder(int maxPacketSize)
	{
		super();
		_maxPacketSize = maxPacketSize;
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx, IOutgoingPacket packet, ByteBuf out)
	{
		try
		{
			if (packet.canBeWritten() && packet.write(new PacketWriter(out)))
			{
				if (out.writerIndex() > _maxPacketSize)
				{
					throw new IllegalStateException("Packet (" + packet.getType()
							+ "/" + packet + ") size (" + out.writerIndex() + ") is bigger than the limit (" + _maxPacketSize + ")");
				}
			}
			else
			{
				// Avoid sending the packet
				out.clear();
			}
		}
		catch (Throwable e)
		{
			Channel channel = ctx.channel();
			if (channel != null) {
				channel.close();
			}

			NetworkLogger.INSTANCE.getLogger().atWarning().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log("Failed sending Packet(%s/%s)", packet.getType(), packet);
			// Avoid sending the packet if some exception happened
			out.clear();
		}
	}
}
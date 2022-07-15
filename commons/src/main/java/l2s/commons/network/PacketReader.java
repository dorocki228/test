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
package l2s.commons.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.nio.charset.StandardCharsets;

/**
 * @author Nos
 * @author Java-man
 */
public final class PacketReader
{
	private final ByteBuf _buf;
	
	public PacketReader(ByteBuf buf)
	{
		_buf = buf;
	}
	
	/**
	 * Gets the readable bytes.
	 * @return the readable bytes
	 */
	public int getReadableBytes()
	{
		return _buf.readableBytes();
	}
	
	/**
	 * Reads an unsigned byte.
	 * @return the unsigned byte
	 * @throws IndexOutOfBoundsException if {@code readableBytes} is less than {@code 1}
	 */
	public short readC()
	{
		return _buf.readUnsignedByte();
	}
	
	/**
	 * Reads an unsigned short.
	 * @return the unsigned short
	 * @throws IndexOutOfBoundsException if {@code readableBytes} is less than {@code 2}
	 */
	public int readH()
	{
		return _buf.readUnsignedShortLE();
	}
	
	/**
	 * Reads an integer.
	 * @return the integer
	 * @throws IndexOutOfBoundsException if {@code readableBytes} is less than {@code 4}
	 */
	public int readD()
	{
		return _buf.readIntLE();
	}
	
	/**
	 * Reads a long.
	 * @return the long
	 * @throws IndexOutOfBoundsException if {@code readableBytes} is less than {@code 8}
	 */
	public long readQ()
	{
		return _buf.readLongLE();
	}
	
	/**
	 * Reads a float.
	 * @return the float
	 * @throws IndexOutOfBoundsException if {@code readableBytes} is less than {@code 4}
	 */
	public float readE()
	{
		return Float.intBitsToFloat(_buf.readIntLE());
	}
	
	/**
	 * Reads a double.
	 * @return the double
	 * @throws IndexOutOfBoundsException if {@code readableBytes} is less than {@code 8}
	 */
	public double readF()
	{
		return Double.longBitsToDouble(_buf.readLongLE());
	}
	
	/**
	 * Reads a string.
	 * @return the string
	 * @throws IndexOutOfBoundsException if string {@code null} terminator is not found within {@code readableBytes}
	 */
	public String readS()
	{
		final StringBuilder sb = new StringBuilder();
		char chr;
		while ((chr = Character.reverseBytes(_buf.readChar())) != 0)
		{
			sb.append(chr);
		}
		return sb.toString();
	}

	public String readS(int length)
	{
		final StringBuilder sb = new StringBuilder(length);
		int counter = 0;
		char chr;
		while ((chr = Character.reverseBytes(_buf.readChar())) != 0)
		{
			counter++;
			if (counter > length) {
				return sb.toString();
			}

			sb.append(chr);
		}
		return sb.toString();
	}

	/**
	 * Reads a fixed length string.
	 * @return the string
	 * @throws IndexOutOfBoundsException if {@code readableBytes} is less than {@code 2 + String.length * 2}
	 */
	public String readSizedString()
	{
		final int stringLengthInBytes = _buf.readShortLE() * 2;
		if (stringLengthInBytes > getReadableBytes())
		{
			throw new IndexOutOfBoundsException("readerIndex(" + _buf.readerIndex() + ") + length(" + stringLengthInBytes
					+ ") exceeds writerIndex(" + _buf.writerIndex() + "): " + _buf);
		}

		return _buf.readCharSequence(stringLengthInBytes, StandardCharsets.UTF_16LE).toString();
	}
	
	/**
	 * Reads a byte array.
	 * @param length the length
	 * @return the byte array
	 * @throws IndexOutOfBoundsException if {@code readableBytes} is less than {@code length}
	 */
	public byte[] readB(int length)
	{
		byte[] result = new byte[length];
		_buf.readBytes(result);
		return result;
	}
	
	/**
	 * Reads a byte array.
	 * @param dst the destination
	 * @param dstIndex the destination index to start writing the bytes from
	 * @param length the length
	 * @throws IndexOutOfBoundsException if {@code readableBytes} is less than {@code length}, if the specified dstIndex is less than 0 or if {@code dstIndex + length} is greater than {@code dst.length}
	 */
	public void readB(byte[] dst, int dstIndex, int length)
	{
		_buf.readBytes(dst, dstIndex, length);
	}

	@Override
	public String toString() {
		return "PacketReader{" +
				"_buf=" + ByteBufUtil.prettyHexDump(_buf) +
				'}';
	}
}

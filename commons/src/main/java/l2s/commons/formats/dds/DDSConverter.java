package l2s.commons.formats.dds;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DDSConverter
{
	public static ByteBuffer convertToDxt1NoTransparency(BufferedImage bufferedimage)
	{
		if(bufferedimage == null)
			return null;
		int i = 128 + bufferedimage.getWidth() * bufferedimage.getHeight() / 2;
		ByteBuffer bytebuffer = ByteBuffer.allocate(i);
		bytebuffer.order(ByteOrder.LITTLE_ENDIAN);
		buildHeaderDxt1(bytebuffer, bufferedimage.getWidth(), bufferedimage.getHeight());
		int j = bufferedimage.getWidth() / 4;
		int[] ai = new int[16];
		for(int k = bufferedimage.getHeight() / 4, l = 0; l < k; ++l)
			for(int i2 = 0; i2 < j; ++i2)
			{
				BufferedImage bufferedimage2 = bufferedimage.getSubimage(i2 * 4, l * 4, 4, 4);
				bufferedimage2.getRGB(0, 0, 4, 4, ai, 0, 4);
				Color[] acolor = getColors888(ai);
				for(int j2 = 0; j2 < ai.length; ++j2)
				{
					ai[j2] = getPixel565(acolor[j2]);
					acolor[j2] = getColor565(ai[j2]);
				}
				int[] ai2 = determineExtremeColors(acolor);
				if(ai[ai2[0]] < ai[ai2[1]])
				{
					int k2 = ai2[0];
					ai2[0] = ai2[1];
					ai2[1] = k2;
				}
				bytebuffer.putShort((short) ai[ai2[0]]);
				bytebuffer.putShort((short) ai[ai2[1]]);
				long l2 = computeBitMask(acolor, ai2);
				bytebuffer.putInt((int) l2);
			}
		return bytebuffer;
	}

	private static void buildHeaderDxt1(ByteBuffer bytebuffer, int i, int j)
	{
		bytebuffer.rewind();
		bytebuffer.put((byte) 68);
		bytebuffer.put((byte) 68);
		bytebuffer.put((byte) 83);
		bytebuffer.put((byte) 32);
		bytebuffer.putInt(124);
		int k = 659463;
		bytebuffer.putInt(k);
		bytebuffer.putInt(j);
		bytebuffer.putInt(i);
		bytebuffer.putInt(i * j / 2);
		bytebuffer.putInt(0);
		bytebuffer.putInt(0);
		bytebuffer.position(bytebuffer.position() + 44);
		bytebuffer.putInt(32);
		bytebuffer.putInt(4);
		bytebuffer.put((byte) 68);
		bytebuffer.put((byte) 88);
		bytebuffer.put((byte) 84);
		bytebuffer.put((byte) 49);
		bytebuffer.putInt(0);
		bytebuffer.putInt(0);
		bytebuffer.putInt(0);
		bytebuffer.putInt(0);
		bytebuffer.putInt(0);
		bytebuffer.putInt(4096);
		bytebuffer.putInt(0);
		bytebuffer.position(bytebuffer.position() + 12);
	}

	private static int[] determineExtremeColors(Color[] acolor)
	{
		int i = Integer.MIN_VALUE;
		int[] ai = new int[2];
		for(int j = 0; j < acolor.length - 1; ++j)
			for(int k = j + 1; k < acolor.length; ++k)
			{
				int l = distance(acolor[j], acolor[k]);
				if(l > i)
				{
					i = l;
					ai[0] = j;
					ai[1] = k;
				}
			}
		return ai;
	}

	private static long computeBitMask(Color[] acolor, int[] ai)
	{
		Color[] acolor2 = { null, null, new Color(), new Color() };
		acolor2[0] = acolor[ai[0]];
		acolor2[1] = acolor[ai[1]];
		if(acolor2[0].equals(acolor2[1]))
			return 0L;
		acolor2[2].r = (2 * acolor2[0].r + acolor2[1].r + 1) / 3;
		acolor2[2].g = (2 * acolor2[0].g + acolor2[1].g + 1) / 3;
		acolor2[2].b = (2 * acolor2[0].b + acolor2[1].b + 1) / 3;
		acolor2[3].r = (acolor2[0].r + 2 * acolor2[1].r + 1) / 3;
		acolor2[3].g = (acolor2[0].g + 2 * acolor2[1].g + 1) / 3;
		acolor2[3].b = (acolor2[0].b + 2 * acolor2[1].b + 1) / 3;
		long l = 0L;
		for(int i = 0; i < acolor.length; ++i)
		{
			int j = Integer.MAX_VALUE;
			int k = 0;
			for(int i2 = 0; i2 < acolor2.length; ++i2)
			{
				int j2 = distance(acolor[i], acolor2[i2]);
				if(j2 < j)
				{
					j = j2;
					k = i2;
				}
			}
			l |= k << i * 2;
		}
		return l;
	}

	private static int getPixel565(Color color)
	{
		int i = color.r >> 3;
		int j = color.g >> 2;
		int k = color.b >> 3;
		return i << 11 | j << 5 | k;
	}

	private static Color getColor565(int i)
	{
		Color color = new Color();
		color.r = (i & 0xF800) >> 11;
		color.g = (i & 0x7E0) >> 5;
		color.b = i & 0x1F;
		return color;
	}

	private static Color[] getColors888(int[] ai)
	{
		Color[] acolor = new Color[ai.length];
		for(int i = 0; i < ai.length; ++i)
		{
			acolor[i] = new Color();
			acolor[i].r = (ai[i] & 0xFF0000) >> 16;
			acolor[i].g = (ai[i] & 0xFF00) >> 8;
			acolor[i].b = ai[i] & 0xFF;
		}
		return acolor;
	}

	private static int distance(Color color, Color color1)
	{
		return (color1.r - color.r) * (color1.r - color.r) + (color1.g - color.g) * (color1.g - color.g) + (color1.b - color.b) * (color1.b - color.b);
	}

	private static class Color
	{
		protected int r;
		protected int g;
		protected int b;

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
				return true;
			if(obj == null || getClass() != obj.getClass())
				return false;
			Color color = (Color) obj;
			return b == color.b && g == color.g && r == color.r;
		}

		@Override
		public int hashCode()
		{
			int i = r;
			i = 29 * i + g;
			i = 29 * i + b;
			return i;
		}

		public Color()
		{
			boolean r = false;
			b = r ? 1 : 0;
			g = r ? 1 : 0;
			this.r = r ? 1 : 0;
		}

		public Color(int i, int j, int k)
		{
			r = i;
			g = j;
			b = k;
		}
	}
}

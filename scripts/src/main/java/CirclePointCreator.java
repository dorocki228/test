import l2s.gameserver.utils.PositionUtils;

public class CirclePointCreator
{

	public static void main(String[] args)
	{
		int radius = 300;
		int points = 6;
		//		88744 / 62744 / -3696
		int x = 88744;
		int y = 62744;
		int z = -3696;
		int h;
		for(int i = 0; i < points; i++)
		{
			int xi = (int) (Math.cos(2 * Math.PI * i / points) * radius + x);
			int yi = (int) (Math.sin(2 * Math.PI * i / points) * radius + y);
			// ниже текст

			h = PositionUtils.calculateHeadingFrom(x, y, xi, yi);

			System.out.println("<npc id=\"40035\" count=\"1\" respawn=\"60\" pos=\"" + xi + " " + yi + " " + z + " " + h + " \"/>");
			//			System.out.println(xi + " " + yi + " " + z + " " + h);

		}
	}
}

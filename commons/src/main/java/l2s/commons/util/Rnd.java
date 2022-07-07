package l2s.commons.util;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Rnd
{
	public static double get()
	{
		return ThreadLocalRandom.current().nextDouble();
	}

	public static int get(int n)
	{
		return ThreadLocalRandom.current().nextInt(n);
	}

	public static long get(long n)
	{
		return (long) (ThreadLocalRandom.current().nextDouble() * n);
	}

	public static int get(int min, int max)
	{
		return min + get(max - min + 1);
	}

	public static long get(long min, long max)
	{
		return min + get(max - min + 1L);
	}

	public static int nextInt()
	{
		return ThreadLocalRandom.current().nextInt();
	}

	public static double nextDouble()
	{
		return ThreadLocalRandom.current().nextDouble();
	}

	public static double nextGaussian()
	{
		return ThreadLocalRandom.current().nextGaussian();
	}

	public static boolean nextBoolean()
	{
		return ThreadLocalRandom.current().nextBoolean();
	}

	public static boolean chance(int chance)
	{
		return chance >= 1 && (chance > 99 || ThreadLocalRandom.current().nextInt(99) + 1 <= chance);
	}

	public static boolean chance(double chance)
	{
		return ThreadLocalRandom.current().nextDouble() <= chance / 100.0;
	}

	public static <E> E get(E[] list)
	{
		if(list.length == 0)
			return null;
		if(list.length == 1)
			return list[0];
		return list[get(list.length)];
	}

	public static int get(int[] list)
	{
		return list[get(list.length)];
	}

	public static <E> E get(List<E> list)
	{
		if(list.isEmpty())
			return null;
		if(list.size() == 1)
			return list.get(0);
		return list.get(get(list.size()));
	}

	public static <E> List<E> get(List<E> list, int count)
	{
		if(list.isEmpty() || count == 0)
			return Collections.emptyList();

		Collections.shuffle(list);

		if(list.size() <= count)
		{
			return list;
		}

		return list.subList(0, count);
	}
}

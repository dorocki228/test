package l2s.commons.string;

import com.google.common.flogger.FluentLogger;

/**
 * @author Bonux
 * @date 11/24/2013 2:33
**/
public class StringArrayUtils
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	public static int[] stringToIntArray(String text, String separator)
	{
		if(text == null || text.isEmpty())
			return new int[0];

		String[] separatedText = text.split(separator);
		int[] result = new int[separatedText.length];
		try
		{
			for(int i = 0; i < separatedText.length; i++)
				result[i] = Integer.parseInt(separatedText[i]);
		}
		catch(NumberFormatException e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "StringArrayUtils: Error while convert string to int array." );
			return new int[0];
		}

		return result;
	}

	public static int[][] stringToIntArray2X(String text, String separator1, String separator2)
	{
		if(text == null || text.isEmpty())
			return new int[0][];

		String[] separatedText = text.split(separator1);
		int[][] result = new int[separatedText.length][];
		for(int i = 0; i < separatedText.length; i++)
			result[i] = stringToIntArray(separatedText[i], separator2);

		return result;
	}

	public static long[] stringToLongArray(String text, String separator)
	{
		if(text == null || text.isEmpty())
			return new long[0];

		String[] separatedText = text.split(separator);
		long[] result = new long[separatedText.length];
		try
		{
			for(int i = 0; i < separatedText.length; i++)
				result[i] = Long.parseLong(separatedText[i]);
		}
		catch(NumberFormatException e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "StringArrayUtils: Error while convert string to long array." );
			return new long[0];
		}

		return result;
	}

	public static long[][] stringToLong2X(String text, String separator1, String separator2)
	{
		if(text == null || text.isEmpty())
			return new long[0][];

		String[] separatedText = text.split(separator1);
		long[][] result = new long[separatedText.length][];
		for(int i = 0; i < separatedText.length; i++)
			result[i] = stringToLongArray(separatedText[i], separator2);

		return result;
	}

	public static double[] stringToDoubleArray(String text, String separator)
	{
		if(text == null || text.isEmpty())
			return new double[0];

		String[] separatedText = text.split(separator);
		double[] result = new double[separatedText.length];
		try
		{
			for(int i = 0; i < separatedText.length; i++)
				result[i] = Double.parseDouble(separatedText[i]);
		}
		catch(NumberFormatException e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "StringArrayUtils: Error while convert string to double array." );
			return new double[0];
		}

		return result;
	}

	public static double[][] stringToDouble2X(String text, String separator1, String separator2)
	{
		if(text == null || text.isEmpty())
			return new double[0][];

		String[] separatedText = text.split(separator1);
		double[][] result = new double[separatedText.length][];
		for(int i = 0; i < separatedText.length; i++)
			result[i] = stringToDoubleArray(separatedText[i], separator2);

		return result;
	}
}
package l2s.commons.math;

public class SafeMath
{
	public static int addAndCheck(int a, int b) throws ArithmeticException
	{
		return addAndCheck(a, b, "overflow: add", false);
	}

	public static int addAndLimit(int a, int b)
	{
		return addAndCheck(a, b, null, true);
	}

	private static int addAndCheck(int a, int b, String msg, boolean limit)
	{
		int ret;
		if(a > b)
		{
			ret = addAndCheck(b, a, msg, limit);
		}
		else if(a < 0)
		{
			if(b < 0)
			{
				if(Integer.MIN_VALUE - b <= a)
				{
					ret = a + b;
				}
				else
				{
					if(!limit){ throw new ArithmeticException(msg); }
					ret = Integer.MIN_VALUE;
				}
			}
			else
			{
				ret = a + b;
			}
		}
		else if(a <= Integer.MAX_VALUE - b)
		{
			ret = a + b;
		}
		else
		{
			if(!limit){ throw new ArithmeticException(msg); }
			ret = Integer.MAX_VALUE;
		}
		return ret;
	}

	public static long addAndLimit(long a, long b)
	{
		return addAndCheck(a, b, "overflow: add", true);
	}

	public static long addAndCheck(long a, long b) throws ArithmeticException
	{
		return addAndCheck(a, b, "overflow: add", false);
	}

	private static long addAndCheck(long a, long b, String msg, boolean limit)
	{
		long ret;
		if(a > b)
		{
			ret = addAndCheck(b, a, msg, limit);
		}
		else if(a < 0L)
		{
			if(b < 0L)
			{
				if(Long.MIN_VALUE - b <= a)
				{
					ret = a + b;
				}
				else
				{
					if(!limit){ throw new ArithmeticException(msg); }
					ret = Long.MIN_VALUE;
				}
			}
			else
			{
				ret = a + b;
			}
		}
		else if(a <= Long.MAX_VALUE - b)
		{
			ret = a + b;
		}
		else
		{
			if(!limit){ throw new ArithmeticException(msg); }
			ret = Long.MAX_VALUE;
		}
		return ret;
	}

	public static int mulAndCheck(int a, int b) throws ArithmeticException
	{
		return mulAndCheck(a, b, "overflow: mul", false);
	}

	public static int mulAndLimit(int a, int b)
	{
		return mulAndCheck(a, b, "overflow: mul", true);
	}

	private static int mulAndCheck(int a, int b, String msg, boolean limit)
	{
		int ret;
		if(a > b)
		{
			ret = mulAndCheck(b, a, msg, limit);
		}
		else if(a < 0)
		{
			if(b < 0)
			{
				if(a >= Integer.MAX_VALUE / b)
				{
					ret = a * b;
				}
				else
				{
					if(!limit){ throw new ArithmeticException(msg); }
					ret = Integer.MAX_VALUE;
				}
			}
			else if(b > 0)
			{
				if(Integer.MIN_VALUE / b <= a)
				{
					ret = a * b;
				}
				else
				{
					if(!limit){ throw new ArithmeticException(msg); }
					ret = Integer.MIN_VALUE;
				}
			}
			else
			{
				ret = 0;
			}
		}
		else if(a > 0)
		{
			if(a <= Integer.MAX_VALUE / b)
			{
				ret = a * b;
			}
			else
			{
				if(!limit){ throw new ArithmeticException(msg); }
				ret = Integer.MAX_VALUE;
			}
		}
		else
		{
			ret = 0;
		}
		return ret;
	}

	public static long mulAndCheck(long a, long b) throws ArithmeticException
	{
		return mulAndCheck(a, b, "overflow: mul", false);
	}

	public static long mulAndLimit(long a, long b)
	{
		return mulAndCheck(a, b, "overflow: mul", true);
	}

	private static long mulAndCheck(long a, long b, String msg, boolean limit)
	{
		long ret;
		if(a > b)
		{
			ret = mulAndCheck(b, a, msg, limit);
		}
		else if(a < 0L)
		{
			if(b < 0L)
			{
				if(a >= Long.MAX_VALUE / b)
				{
					ret = a * b;
				}
				else
				{
					if(!limit){ throw new ArithmeticException(msg); }
					ret = Long.MAX_VALUE;
				}
			}
			else if(b > 0L)
			{
				if(Long.MIN_VALUE / b <= a)
				{
					ret = a * b;
				}
				else
				{
					if(!limit){ throw new ArithmeticException(msg); }
					ret = Long.MIN_VALUE;
				}
			}
			else
			{
				ret = 0L;
			}
		}
		else if(a > 0L)
		{
			if(a <= Long.MAX_VALUE / b)
			{
				ret = a * b;
			}
			else
			{
				if(!limit){ throw new ArithmeticException(msg); }
				ret = Long.MAX_VALUE;
			}
		}
		else
		{
			ret = 0L;
		}
		return ret;
	}
}

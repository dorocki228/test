package l2s.authserver.crypt;

import com.google.common.flogger.FluentLogger;
import jonelo.jacksum.JacksumAPI;
import jonelo.jacksum.algorithm.AbstractChecksum;

public class PasswordHash
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	private final String name;

	public PasswordHash(String name)
	{
		this.name = name;
	}

	/**
	 * Сравнивает пароль и ожидаемый хеш
	 * @param password
	 * @param hash
	 * @return совпадает или нет
	 */
	public boolean compare(String password, String expected)
	{
		try
		{
			return encrypt(password).equals(expected);
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "%s: encryption error!", name );
			return false;
		}
	}

	/**
	 * Получает пароль и возвращает хеш
	 * @param password
	 * @return hash
	 */
	public String encrypt(String password) throws Exception
	{
		AbstractChecksum checksum = JacksumAPI.getChecksumInstance(name);
		checksum.setEncoding("BASE64");
		checksum.update(password.getBytes());
		return checksum.format("#CHECKSUM");
	}
}
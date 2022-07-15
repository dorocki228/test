package l2s.gameserver.utils;

import com.google.common.flogger.FluentLogger;
import l2s.commons.string.CharsetEncodingDetector;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Deprecated
public class Files
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	public static String readFile(File file, Charset outputEncode) throws IOException
	{
		String content = FileUtils.readFileToString(file, CharsetEncodingDetector.detectEncoding(file.toPath(), outputEncode));
		content = new String(content.getBytes(outputEncode));
		return content;
	}

	public static String readFile(File file) throws IOException
	{
		return readFile(file, StandardCharsets.UTF_8);
	}

	/**
	 * Сохраняет строку в файл в кодировке UTF-8.<br>
	 * Если такой файл существует, то перезаписывает его.
	 * @param path путь к файлу
	 * @param string сохраняемая строка
	 */
	public static void writeFile(String path, String string)
	{
		try
		{
			FileUtils.writeStringToFile(new File(path), string, "UTF-8");
		}
		catch(IOException e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Error while saving file : %s", path );
		}
	}

	public static boolean copyFile(String srcFile, String destFile)
	{
		try
		{
			FileUtils.copyFile(new File(srcFile), new File(destFile), false);
			return true;
		}
		catch(IOException e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Error while copying file : %s to %s", srcFile, destFile );
		}

		return false;
	}
}
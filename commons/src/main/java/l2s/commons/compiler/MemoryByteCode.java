package l2s.commons.compiler;

import org.springframework.util.FastByteArrayOutputStream;

import javax.tools.SimpleJavaFileObject;
import java.io.OutputStream;
import java.net.URI;

public class MemoryByteCode extends SimpleJavaFileObject
{
	private FastByteArrayOutputStream oStream;
	private final String className;

	public MemoryByteCode(String className, URI uri)
	{
		super(uri, Kind.CLASS);
		this.className = className;
	}

	@Override
	public OutputStream openOutputStream()
	{
		oStream = new FastByteArrayOutputStream();
		return oStream;
	}

	public byte[] getBytes()
	{
		return oStream.toByteArray();
	}

	@Override
	public String getName()
	{
		return className;
	}
}

package l2s.commons.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class MemoryClassLoader extends ClassLoader
{
	private final Map<String, MemoryByteCode> classes = new HashMap<>();
	private final Map<String, MemoryByteCode> loaded = new HashMap<>();

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		MemoryByteCode mbc = classes.get(name);
		if(mbc == null)
		{
			mbc = classes.get(name);
			if(mbc == null)
			{
				return super.findClass(name);
			}
		}

		byte[] bytes = mbc.getBytes();
		return defineClass(name, bytes, 0, bytes.length);
	}

	public void addClass(MemoryByteCode mbc)
	{
		String mbcName = mbc.getName();
		classes.put(mbcName, mbc);
		loaded.put(mbcName, mbc);
	}

	public MemoryByteCode getClass(String name)
	{
		return classes.get(name);
	}

	public Stream<String> getLoadedClasses()
	{
		return loaded.keySet().stream();
	}

	public void clear()
	{
		loaded.clear();
	}
}

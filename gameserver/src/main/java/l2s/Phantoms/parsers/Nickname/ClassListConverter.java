package  l2s.Phantoms.parsers.Nickname;

import java.util.Arrays;
import java.util.stream.Stream;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class ClassListConverter implements SingleValueConverter
{
	public Object fromString(String name)
	{
		return Stream.of(name.split(",")).mapToInt(Integer::parseInt).toArray();
	}
	
	public String toString(Object name)
	{
		return Arrays.toString((int[]) name).replaceAll(" ", "").replaceAll("[\\[\\]]", "");
	}
	
	@SuppressWarnings("rawtypes")
	public boolean canConvert(Class type)
	{
		return true;// type.equals(String.class);
	}
	
}

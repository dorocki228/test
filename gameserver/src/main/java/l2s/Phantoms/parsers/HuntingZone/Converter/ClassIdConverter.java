package  l2s.Phantoms.parsers.HuntingZone.Converter;

import java.util.SortedSet;
import java.util.TreeSet;

import com.thoughtworks.xstream.converters.SingleValueConverter;

public class ClassIdConverter implements SingleValueConverter
{
	public Object fromString(String name)
	{
		SortedSet<Integer> classid = new TreeSet<Integer>();
		if (name!= null && !name.isBlank())
		for (String s:name.split(","))
			classid.add(Integer.parseInt(s));
		
		return classid;
	}
	
	@SuppressWarnings("unchecked")
	public String toString(Object name)
	{
		String classid = "";
		for (Integer tmp : ((SortedSet<Integer>) name))
		{
			if (classid.isBlank())
				classid= ""+tmp;
			else
				classid= classid + ","+tmp;
		}
		return classid;
	}
	
	@SuppressWarnings("rawtypes")
	public boolean canConvert(Class type)
	{
		return true;// type.equals(String.class);
	}
	
}

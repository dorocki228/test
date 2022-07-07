package  l2s.Phantoms.parsers.HuntingZone.Converter;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import l2s.gameserver.model.base.Race;


public class RaceEnumConverter implements Converter
{
	public static RaceEnumConverter getInstance()
	{
		return new RaceEnumConverter();
	}
	
	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context)
	{
		writer.setValue(((Race) value).name());
	}
	
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		return Race.valueOf(reader.getValue()); 
	}
	
	@SuppressWarnings("rawtypes")
	public boolean canConvert(Class clazz)
	{
		return Race.class.isAssignableFrom(clazz);
	}
	
	
}
package  l2s.Phantoms.objects.Ai;


import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import  l2s.Phantoms.enums.Condition;
import  l2s.Phantoms.objects.PCondition;

public class MapEntryConverter implements Converter
{
	
	@SuppressWarnings("rawtypes")
	public boolean canConvert(Class clazz)
	{
		return AbstractMap.class.isAssignableFrom(clazz);
	}
	
	@SuppressWarnings("unchecked")
	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context)
	{
  	Map <Condition,PCondition> condition = (Map <Condition,PCondition>) value;
		for(Entry <Condition,PCondition> cond : condition.entrySet())
		{
			writer.startNode(cond.getKey().name());
			 PCondition val = cond.getValue();
			if (null != val)
			{
				writer.setValue(val.toString());
			}
			writer.endNode();
		}
		
	}
	
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		
		Map <Condition,PCondition> map = new HashMap <Condition,PCondition>();
		
		while (reader.hasMoreChildren())
		{
			reader.moveDown();
			
			String key = reader.getNodeName(); 
			String value = reader.getValue();
			map.put(Condition.valueOf(key), new PCondition(Condition.valueOf(key), value));
			
			reader.moveUp();
		}
		
		return map;
	}
}
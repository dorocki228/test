package  l2s.Phantoms.parsers.HuntingZone;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import  l2s.Phantoms.Utils.PhantomUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("polygon")
public class HuntingZonePolygon
{
	@XStreamImplicit(itemFieldName = "coords")
	public List<String> coords = new ArrayList<String>();

	public void removeCords(String string) 
	{
		Iterator<String> numListIter = coords.iterator();
		while (numListIter.hasNext()) 
		{
			String n = numListIter.next();
			if (PhantomUtils.equals(string.trim(), n.trim()))
				numListIter.remove();
		}
	}

	
}
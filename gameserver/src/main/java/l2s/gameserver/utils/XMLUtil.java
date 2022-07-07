package l2s.gameserver.utils;

import com.google.common.collect.Range;
import com.google.common.primitives.Longs;
import org.dom4j.Element;

import java.util.Optional;

public class XMLUtil
{
	public static Optional<Range<Long>> getRange(Element element, String item)
	{
		var parts = element.attributeValue(item).split("-");
		Long min = Longs.tryParse(parts[0]);
		if(min == null)
			return Optional.empty();
		if(parts.length == 1)
			return Optional.of(Range.closed(min, min));
		Long max = Longs.tryParse(parts[1]);
		if(max == null)
			return Optional.of(Range.closed(min, min));

		return Optional.of(Range.closed(min, max));
	}
}

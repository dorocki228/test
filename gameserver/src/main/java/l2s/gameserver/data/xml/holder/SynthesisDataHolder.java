package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.item.support.SynthesisData;

import java.util.ArrayList;
import java.util.List;

public final class SynthesisDataHolder extends AbstractHolder
{
	private static final SynthesisDataHolder _instance = new SynthesisDataHolder();
	private final List<SynthesisData> _data = new ArrayList<>();

	public static SynthesisDataHolder getInstance()
	{
		return _instance;
	}

	public void addData(SynthesisData data)
	{
		_data.add(data);
	}

	public SynthesisData[] getDatas()
	{
		return _data.toArray(new SynthesisData[0]);
	}

	@Override
	public int size()
	{
		return _data.size();
	}

	@Override
	public void clear()
	{
		_data.clear();
	}
}
